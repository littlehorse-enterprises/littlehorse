package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Any;
import com.google.rpc.RetryInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeletePrincipalRequest;
import io.littlehorse.sdk.common.proto.DeleteQuotaRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.PutQuotaRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.QuotaIdList;
import io.littlehorse.sdk.common.proto.SearchQuotaRequest;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@LHTest
@Isolated
public class QuotaTest {

    private LittleHorseBlockingStub client;
    private LHConfig config;
    private WorkflowVerifier verifier;

    @LHWorkflow("quota-test-wf")
    private Workflow quotaWf;

    @LHWorkflow("quota-test-wf")
    public Workflow getQuotaWf() {
        return new WorkflowImpl("quota-test-wf", wf -> {
            wf.execute("quota-test-noop");
            wf.execute("quota-test-noop");
            wf.execute("quota-test-noop");
            wf.execute("quota-test-noop");
        });
    }

    @LHTaskMethod("quota-test-noop")
    public void noopTask() {}

    private String tenantId() {
        return config.getTenantId().getId();
    }

    private QuotaId tenantQuotaId() {
        return QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();
    }

    private void setQuota(int writeRequestsPerSecond) {
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(writeRequestsPerSecond)
                .build());
    }

    private void awaitQuota(int expectedRate) {
        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Thread.sleep(500);
            Quota q = client.getQuota(tenantQuotaId());
            assertThat(q.getWriteRequestsPerSecond()).isEqualTo(expectedRate);
        });
    }

    private void deleteQuotaSilently(QuotaId id) {
        try {
            client.deleteQuota(DeleteQuotaRequest.newBuilder().setId(id).build());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Code.NOT_FOUND) {
                // Ignore if already deleted
            } else {
                System.out.println("hi");
                throw e;
            }
        }
    }

    private LittleHorseBlockingStub createRawClient() {
        String host = config.getApiBootstrapHost();
        int port = config.getApiBootstrapPort();
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return LittleHorseGrpc.newBlockingStub(channel);
    }

    private BurstResult sendBurst(LittleHorseBlockingStub stub, int count) {
        List<StatusRuntimeException> throttled = new ArrayList<>();
        int successes = 0;

        for (int i = 0; i < count; i++) {
            try {
                stub.putTaskDef(PutTaskDefRequest.newBuilder()
                        .setName("quota-burst-" + UUID.randomUUID())
                        .build());
                successes++;
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.RESOURCE_EXHAUSTED) {
                    throttled.add(e);
                } else {
                    throw e;
                }
            }
        }

        return new BurstResult(successes, throttled);
    }

    @Test
    void shouldSearchQuotas() {
        String principalName = "search-principal-" + UUID.randomUUID();
        PrincipalId principalId = PrincipalId.newBuilder().setId(principalName).build();

        client.putPrincipal(PutPrincipalRequest.newBuilder()
                .setId(principalName)
                .setOverwrite(true)
                .build());

        Quota tenantQuota = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(100)
                .build());

        Quota principalQuota = client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setPrincipal(principalId)
                .setWriteRequestsPerSecond(50)
                .build());

        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            QuotaIdList results = client.searchQuota(
                    SearchQuotaRequest.newBuilder().setTenantId(tenantId()).build());
            assertThat(results.getResultsList()).hasSizeGreaterThanOrEqualTo(2);
        });

        // Cleanup
        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(tenantQuota.getId()).build());
        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(principalQuota.getId()).build());
        client.deletePrincipal(
                DeletePrincipalRequest.newBuilder().setId(principalId).build());
    }

    /**
     * Sends a burst via a raw client (no retry interceptor), asserts some are throttled,
     * and verifies the first throttled response contains a valid RetryInfo.
     */
    @Test
    void shouldRejectAndIncludeRetryInfo() {
        setQuota(2);
        LittleHorseBlockingStub rawClient = createRawClient();
        try {
            awaitQuota(2);

            BurstResult result = sendBurst(rawClient, 6);

            assertThat(result.throttled())
                    .as("At least some requests should have been throttled")
                    .isNotEmpty();

            // Inspect the first throttled response for RetryInfo
            StatusRuntimeException first = result.throttled().getFirst();
            com.google.rpc.Status rpcStatus = StatusProto.fromStatusAndTrailers(first.getStatus(), first.getTrailers());

            assertThat(rpcStatus).isNotNull();
            assertThat(rpcStatus.getCode()).isEqualTo(com.google.rpc.Code.RESOURCE_EXHAUSTED.getNumber());

            boolean foundRetryInfo = false;
            for (Any detail : rpcStatus.getDetailsList()) {
                if (detail.is(RetryInfo.class)) {
                    RetryInfo retryInfo;
                    try {
                        retryInfo = detail.unpack(RetryInfo.class);
                    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                        throw new AssertionError("Failed to unpack RetryInfo", e);
                    }
                    long delayMs = retryInfo.getRetryDelay().getSeconds() * 1000
                            + retryInfo.getRetryDelay().getNanos() / 1_000_000;
                    assertThat(delayMs)
                            .as("Retry delay should be positive and a multiple of 500ms")
                            .isGreaterThan(0)
                            .satisfies(d -> assertThat(d % 500).isEqualTo(0));
                    foundRetryInfo = true;
                }
            }

            assertThat(foundRetryInfo)
                    .as("RESOURCE_EXHAUSTED response should contain RetryInfo")
                    .isTrue();
        } finally {
            deleteQuotaSilently(tenantQuotaId());
        }
    }

    /**
     * Verifies no throttling when under a generous quota, and also no throttling
     * after a restrictive quota is deleted.
     */
    @Test
    void shouldNotThrottleWhenUnderQuotaOrAfterDeletion() {
        LittleHorseBlockingStub rawClient = createRawClient();

        // Under a generous quota, nothing should be throttled
        setQuota(1000);
        try {
            awaitQuota(1000);

            BurstResult underQuota = sendBurst(rawClient, 20);
            assertThat(underQuota.throttled())
                    .as("No requests should be throttled under a generous quota")
                    .isEmpty();
            assertThat(underQuota.successes()).isEqualTo(20);
        } finally {
            deleteQuotaSilently(tenantQuotaId());
        }

        // Set a restrictive quota then delete it — nothing should be throttled after
        setQuota(2);
        awaitQuota(2);
        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(tenantQuotaId()).build());

        Awaitility.await().atMost(Duration.ofSeconds(4)).until(() -> {
            try {
                client.getQuota(tenantQuotaId());
                return false;
            } catch (Exception e) {
                return true;
            }
        });

        BurstResult afterDeletion = sendBurst(rawClient, 50);
        assertThat(afterDeletion.throttled())
                .as("No requests should be throttled after quota deletion")
                .isEmpty();
    }

    /**
     * Uses the intercepted client (which retries RESOURCE_EXHAUSTED transparently)
     * to confirm all requests succeed and a workflow completes under throttling.
     */
    @Test
    void shouldCompleteWorkflowWithTransparentRetries() {
        setQuota(2);
        try {
            awaitQuota(2);

            Instant start = Instant.now();
            // Workflow should also complete despite throttling
            verifier.prepareRun(quotaWf)
                    .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(5))
                    .start();

            Instant end = Instant.now();

            // There's 4 tasks, each task counts as 1 request (the reportTask is throttled).
            // And there's also an rpc runWf
            assertThat(Duration.between(start, end)).isGreaterThan(Duration.ofSeconds(2));
        } finally {
            deleteQuotaSilently(tenantQuotaId());
        }
    }

    private record BurstResult(int successes, List<StatusRuntimeException> throttled) {}
}

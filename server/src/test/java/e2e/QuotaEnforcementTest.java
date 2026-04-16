package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Any;
import com.google.rpc.RetryInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteQuotaRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutQuotaRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.test.LHTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class QuotaEnforcementTest {

    private LittleHorseBlockingStub client;
    private LHConfig config;

    private String tenantId() {
        return config.getTenantId().getId();
    }

    /**
     * Creates a raw gRPC blocking stub without the retry interceptor installed,
     * so we can directly observe RESOURCE_EXHAUSTED responses.
     * Uses PutTaskDef as the write operation since it doesn't need a pre-registered WfSpec.
     */
    private LittleHorseBlockingStub createRawClient() {
        String host = config.getApiBootstrapHost();
        int port = config.getApiBootstrapPort();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        return LittleHorseGrpc.newBlockingStub(channel);
    }

    private void deleteQuotaSilently(QuotaId id) {
        try {
            client.deleteQuota(
                    DeleteQuotaRequest.newBuilder().setId(id).build());
        } catch (StatusRuntimeException e) {
            // Ignore if already deleted
        }
    }

    /**
     * Sends a burst of PutTaskDef requests via the given stub and collects results.
     * Uses PutTaskDef because it's a simple write operation that doesn't require
     * any pre-existing metadata (unlike RunWf which needs a WfSpec).
     */
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
    void shouldRejectWhenTenantQuotaExceeded() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        // Set a very low quota: 2 writes/sec across the cluster (1 per server with 2 servers)
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(2)
                .build());

        LittleHorseBlockingStub rawClient = createRawClient();
        try {
            // Wait for the quota to propagate
            Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
                Quota q = client.getQuota(quotaId);
                assertThat(q.getWriteRequestsPerSecond()).isEqualTo(2);
            });

            // Blast 20 requests as fast as possible via the raw client
            BurstResult result = sendBurst(rawClient, 20);

            assertThat(result.throttled())
                    .as("At least some requests should have been throttled")
                    .isNotEmpty();
        } finally {
            deleteQuotaSilently(quotaId);
        }
    }

    @Test
    void shouldNotThrottleWhenUnderQuota() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        // Set a generous quota
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(1000)
                .build());

        LittleHorseBlockingStub rawClient = createRawClient();
        try {
            Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
                Quota q = client.getQuota(quotaId);
                assertThat(q.getWriteRequestsPerSecond()).isEqualTo(1000);
            });

            // Send only a few requests
            BurstResult result = sendBurst(rawClient, 5);

            assertThat(result.throttled())
                    .as("No requests should be throttled under a generous quota")
                    .isEmpty();
            assertThat(result.successes()).isEqualTo(5);
        } finally {
            deleteQuotaSilently(quotaId);
        }
    }

    @Test
    void shouldNotThrottleAfterQuotaDeleted() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        // Create a restrictive quota
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(2)
                .build());

        Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            Quota q = client.getQuota(quotaId);
            assertThat(q.getWriteRequestsPerSecond()).isEqualTo(2);
        });

        // Delete it
        client.deleteQuota(
                DeleteQuotaRequest.newBuilder().setId(quotaId).build());

        // Wait for deletion to propagate
        Awaitility.await().atMost(Duration.ofSeconds(4)).until(() -> {
            try {
                client.getQuota(quotaId);
                return false;
            } catch (Exception e) {
                return true;
            }
        });

        // Now send a burst -- none should be throttled
        LittleHorseBlockingStub rawClient = createRawClient();
        BurstResult result = sendBurst(rawClient, 10);

        assertThat(result.throttled())
                .as("No requests should be throttled after quota deletion")
                .isEmpty();
    }

    @Test
    void shouldIncludeRetryInfoInResponse() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(2)
                .build());

        LittleHorseBlockingStub rawClient = createRawClient();
        try {
            Awaitility.await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
                Quota q = client.getQuota(quotaId);
                assertThat(q.getWriteRequestsPerSecond()).isEqualTo(2);
            });

            // Send enough requests to trigger at least one rejection
            StatusRuntimeException throttleException = null;
            for (int i = 0; i < 30; i++) {
                try {
                    rawClient.putTaskDef(PutTaskDefRequest.newBuilder()
                            .setName("quota-retryinfo-" + UUID.randomUUID())
                            .build());
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.RESOURCE_EXHAUSTED) {
                        throttleException = e;
                        break;
                    }
                    throw e;
                }
            }

            assertThat(throttleException)
                    .as("Should have received at least one RESOURCE_EXHAUSTED")
                    .isNotNull();

            // Verify it contains a RetryInfo
            com.google.rpc.Status rpcStatus =
                    StatusProto.fromStatusAndTrailers(throttleException.getStatus(), throttleException.getTrailers());

            assertThat(rpcStatus).isNotNull();
            assertThat(rpcStatus.getCode())
                    .isEqualTo(com.google.rpc.Code.RESOURCE_EXHAUSTED.getNumber());

            boolean foundRetryInfo = false;
            for (Any detail : rpcStatus.getDetailsList()) {
                if (detail.is(RetryInfo.class)) {
                    try {
                        RetryInfo retryInfo = detail.unpack(RetryInfo.class);
                        long delayMs = retryInfo.getRetryDelay().getSeconds() * 1000
                                + retryInfo.getRetryDelay().getNanos() / 1_000_000;
                        assertThat(delayMs)
                                .as("Retry delay should be positive and a multiple of 500ms")
                                .isGreaterThan(0)
                                .satisfies(d -> assertThat(d % 500).isEqualTo(0));
                        foundRetryInfo = true;
                    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                        throw new AssertionError("Failed to unpack RetryInfo from RESOURCE_EXHAUSTED details", e);
                    }
                }
            }

            assertThat(foundRetryInfo)
                    .as("RESOURCE_EXHAUSTED response should contain RetryInfo")
                    .isTrue();
        } finally {
            deleteQuotaSilently(quotaId);
        }
    }

    private record BurstResult(int successes, List<StatusRuntimeException> throttled) {}
}

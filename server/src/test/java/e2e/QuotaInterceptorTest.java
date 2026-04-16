package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteQuotaRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutQuotaRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.sdk.common.proto.QuotaId;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class QuotaInterceptorTest {

    private LittleHorseBlockingStub client;
    private LHConfig config;
    private WorkflowVerifier verifier;

    @LHWorkflow("quota-interceptor-wf")
    private Workflow interceptorWf;

    @LHWorkflow("quota-interceptor-wf")
    public Workflow getInterceptorWf() {
        return new WorkflowImpl("quota-interceptor-wf", wf -> {
            wf.execute("quota-interceptor-noop");
        });
    }

    @LHTaskMethod("quota-interceptor-noop")
    public void noopTask() {
        // Does nothing
    }

    private String tenantId() {
        return config.getTenantId().getId();
    }

    /**
     * Tests that the Java SDK's ResourceExhaustedRetryInterceptor transparently
     * retries when the server returns RESOURCE_EXHAUSTED.
     *
     * With a quota of 2 req/s across 2 servers (1 per server), sending 10 PutTaskDef
     * requests should take noticeably longer than without throttling, but all should
     * succeed because the interceptor retries transparently.
     */
    @Test
    void shouldTransparentlyRetryOnQuotaExceeded() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        // Set a restrictive quota: 2 writes/sec
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(2)
                .build());

        try {
            // Wait for the quota to propagate
            Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                Quota q = client.getQuota(quotaId);
                assertThat(q.getWriteRequestsPerSecond()).isEqualTo(2);
            });

            // Send 10 PutTaskDef requests through the normal client (which has the interceptor).
            // The interceptor should transparently retry any RESOURCE_EXHAUSTED responses.
            long startTime = System.currentTimeMillis();
            int successes = 0;

            for (int i = 0; i < 10; i++) {
                client.putTaskDef(PutTaskDefRequest.newBuilder()
                        .setName("quota-interceptor-" + UUID.randomUUID())
                        .build());
                successes++;
            }

            long elapsedMs = System.currentTimeMillis() - startTime;

            // All 10 requests should have succeeded (the interceptor retries transparently)
            assertThat(successes).isEqualTo(10);

            // With 2 req/s (split across 2 servers = ~1/server), processing 10 requests
            // should take noticeably longer than without throttling due to retry delays.
            assertThat(elapsedMs)
                    .as("Throttled requests should take noticeably longer due to retry delays")
                    .isGreaterThan(1000);
        } finally {
            client.deleteQuota(
                    DeleteQuotaRequest.newBuilder().setId(quotaId).build());
        }
    }

    /**
     * Tests that a workflow completes successfully even when the task worker's
     * ReportTask calls are throttled by quotas.
     */
    @Test
    void shouldCompleteWorkflowUnderThrottling() {
        QuotaId quotaId = QuotaId.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .build();

        // Set a restrictive quota
        client.putQuota(PutQuotaRequest.newBuilder()
                .setTenant(TenantId.newBuilder().setId(tenantId()))
                .setWriteRequestsPerSecond(4)
                .build());

        try {
            Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                Quota q = client.getQuota(quotaId);
                assertThat(q.getWriteRequestsPerSecond()).isEqualTo(4);
            });

            // Run a workflow via the verifier (which uses the intercepted client)
            // and confirm it completes despite throttling.
            verifier.prepareRun(interceptorWf)
                    .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(15))
                    .start();
        } finally {
            client.deleteQuota(
                    DeleteQuotaRequest.newBuilder().setId(quotaId).build());
        }
    }
}

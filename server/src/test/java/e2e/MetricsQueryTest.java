package e2e;

import static org.assertj.core.api.Assertions.*;

import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.MetricsList;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class MetricsQueryTest {

    private WorkflowVerifier workflowVerifier;
    private LittleHorseBlockingStub client;

    @LHWorkflow("metrics-test-workflow")
    private Workflow workflow;

    @LHWorkflow("metrics-error-workflow")
    private Workflow errorWorkflow;

    @Test
    void shouldQueryMetricsForMultipleWorkflows() throws InterruptedException {
        for (int i = 0; i < 33; i++) {
            workflowVerifier
                    .prepareRun(workflow)
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        }
        for (int i = 0; i < 5; i++) {
            workflowVerifier
                    .prepareRun(errorWorkflow)
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        Thread.sleep(35000);

        long now = System.currentTimeMillis();
        long oneHourAgo = now - (60 * 60 * 1000);

        ListWfMetricsRequest completedWfRequest = ListWfMetricsRequest.newBuilder()
                .setWfSpec(WfSpecId.newBuilder()
                        .setName("metrics-test-workflow")
                        .setMajorVersion(0)
                        .build())
                .setWindowStart(Timestamp.newBuilder()
                        .setSeconds(oneHourAgo / 1000)
                        .setNanos((int) ((oneHourAgo % 1000) * 1_000_000))
                        .build())
                .setWindowEnd(Timestamp.newBuilder()
                        .setSeconds(now / 1000)
                        .setNanos((int) ((now % 1000) * 1_000_000))
                        .build())
                .build();
        ListWfMetricsRequest errorWfRequest = ListWfMetricsRequest.newBuilder()
                .setWfSpec(WfSpecId.newBuilder()
                        .setName("metrics-error-workflow")
                        .setMajorVersion(0)
                        .build())
                .setWindowStart(Timestamp.newBuilder()
                        .setSeconds(oneHourAgo / 1000)
                        .setNanos((int) ((oneHourAgo % 1000) * 1_000_000))
                        .build())
                .setWindowEnd(Timestamp.newBuilder()
                        .setSeconds(now / 1000)
                        .setNanos((int) ((now % 1000) * 1_000_000))
                        .build())
                .build();

        MetricsList response = client.listWfMetrics(completedWfRequest);
        MetricsList errorResponse = client.listWfMetrics(errorWfRequest);
        assertThat(response.getWindowsList()).isNotEmpty();

        int totalErrorCount = 0;
        for (var window : errorResponse.getWindowsList()) {
            if (window.getMetricsMap().containsKey("running_to_error")) {
                totalErrorCount +=
                        window.getMetricsMap().get("running_to_error").getCount();
            }
        }

        int totalCompletedCount = 0;
        long totalLatencyMs = 0;
        long minLatencyMs = 0;
        long maxLatencyMs = 0;

        for (var window : response.getWindowsList()) {
            if (window.getMetricsMap().containsKey("running_to_completed")) {
                var metric = window.getMetricsMap().get("running_to_completed");
                totalCompletedCount += metric.getCount();
                totalLatencyMs += metric.getTotalLatencyMs();
                minLatencyMs += metric.getMinLatencyMs();
                maxLatencyMs += metric.getMaxLatencyMs();
            }
        }

        assertThat(totalErrorCount).isGreaterThanOrEqualTo(5);

        assertThat(totalCompletedCount).isGreaterThanOrEqualTo(33);
        assertThat(minLatencyMs).isGreaterThan(0);
        assertThat(maxLatencyMs).isGreaterThan(0);
        assertThat(totalLatencyMs).isGreaterThan(0);
    }

    @LHWorkflow("metrics-error-workflow")
    public Workflow buildErrorWorkflow() {
        return new WorkflowImpl("metrics-error-workflow", thread -> {
            thread.execute("metrics-failing-task");
        });
    }

    @LHWorkflow("metrics-test-workflow")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("metrics-test-workflow", thread -> {
            thread.execute("metrics-simple-task");
        });
    }

    @LHTaskMethod("metrics-simple-task")
    public String simpleTask() {
        return "completed";
    }

    @LHTaskMethod("metrics-failing-task")
    public String failingTask() {
        throw new RuntimeException("Intentional failure for metrics testing");
    }
}

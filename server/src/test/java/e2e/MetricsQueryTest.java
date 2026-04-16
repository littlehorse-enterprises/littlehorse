package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowId;
import io.littlehorse.sdk.common.proto.MetricWindowIdList;
import io.littlehorse.sdk.common.proto.MetricsList;
import io.littlehorse.sdk.common.proto.SearchWfMetricWindowRequest;
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

        Thread.sleep(65000);

        ListWfMetricsRequest completedWfRequest = ListWfMetricsRequest.newBuilder()
                .setWfSpec(WfSpecId.newBuilder()
                        .setName("metrics-test-workflow")
                        .setMajorVersion(0)
                        .setRevision(0)
                        .build())
                .build();
        ListWfMetricsRequest errorWfRequest = ListWfMetricsRequest.newBuilder()
                .setWfSpec(WfSpecId.newBuilder()
                        .setName("metrics-error-workflow")
                        .setMajorVersion(0)
                        .setRevision(0)
                        .build())
                .build();

        MetricsList response = client.listWfMetrics(completedWfRequest);
        MetricsList errorResponse = client.listWfMetrics(errorWfRequest);
        assertThat(response.getWindowsList()).isNotEmpty();

        int totalErrorCount = 0;
        for (var window : errorResponse.getWindowsList()) {
            if (window.hasWorkflow() && window.getWorkflow().hasRunningToError()) {
                totalErrorCount += window.getWorkflow().getRunningToError().getCount();
            }
        }

        int totalCompletedCount = 0;
        long totalLatencyMs = 0;
        long minLatencyMs = 0;
        long maxLatencyMs = 0;

        for (var window : response.getWindowsList()) {
            if (window.hasWorkflow() && window.getWorkflow().hasRunningToCompleted()) {
                var metric = window.getWorkflow().getRunningToCompleted();
                totalCompletedCount += metric.getCount();
                totalLatencyMs += metric.getTotalLatencyMs();
                minLatencyMs += metric.getMinLatencyMs();
                maxLatencyMs += metric.getMaxLatencyMs();
            }
        }

        assertThat(totalErrorCount).isEqualTo(5);
        assertThat(totalCompletedCount).isEqualTo(33);
        assertThat(minLatencyMs).isGreaterThan(0);
        assertThat(maxLatencyMs).isGreaterThan(0);
        assertThat(totalLatencyMs).isGreaterThan(0);

        // Verify search by wfSpecName returns the windows that were just aggregated
        MetricWindowIdList searchResult = client.searchWfMetricWindow(SearchWfMetricWindowRequest.newBuilder()
                .setWfSpecName("metrics-test-workflow")
                .build());
        assertThat(searchResult.getResultsList()).isNotEmpty();

        // Verify get-by-id returns the correct MetricWindow
        MetricWindowId firstId = searchResult.getResultsList().get(0);
        MetricWindow metricWindowById = client.getMetricWindow(firstId);
        assertThat(metricWindowById).isNotNull();
        assertThat(metricWindowById.getId().getWfSpecId()).isEqualTo(firstId.getWfSpecId());
        assertThat(metricWindowById.getId().getWindowStart()).isEqualTo(firstId.getWindowStart());

        assertThat(metricWindowById.hasWorkflow()).isTrue();

        // Verify that searching with latestOnly=true returns only the most recent window
        MetricWindowIdList latestMetricResult = client.searchWfMetricWindow(SearchWfMetricWindowRequest.newBuilder()
                .setWfSpecName("metrics-test-workflow")
                .setLatestOnly(true)
                .build());
        assertEquals(1, latestMetricResult.getResultsList().size());
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

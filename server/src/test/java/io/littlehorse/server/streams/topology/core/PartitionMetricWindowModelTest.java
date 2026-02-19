package io.littlehorse.server.streams.topology.core;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.getable.core.metrics.CountAndTimingModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PartitionMetricWindowModelTest {

    private WfSpecIdModel wfSpecId;
    private TenantIdModel tenantId;
    private Date windowStart;

    @BeforeEach
    public void setup() {
        wfSpecId = new WfSpecIdModel("test-wfspec", 1, 0);
        tenantId = new TenantIdModel("test-tenant");
        windowStart = new Date(1000000000L); // Fixed timestamp for testing
    }

    @Test
    public void shouldHandleNullWhenMergingFrom() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model.incrementCount("test-metric");

        model.mergeFrom(null);

        assertThat(model.getMetrics()).hasSize(1);
        assertThat(model.getMetrics().get("test-metric").getCount()).isEqualTo(1);
    }

    @Test
    public void shouldMergeMetricsFromOtherModel() {
        PartitionMetricWindowModel model1 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model1.incrementCountAndLatency("metric1", 100L);
        model1.incrementCountAndLatency("metric2", 200L);

        PartitionMetricWindowModel model2 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model2.incrementCountAndLatency("metric1", 150L);
        model2.incrementCountAndLatency("metric3", 300L);

        PartitionMetricWindowModel model3 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model3.incrementCountAndLatency("metric1", 150L);
        model3.incrementCountAndLatency("metric3", 300L);

        model1.mergeFrom(model2);
        model1.mergeFrom(model3);

        assertThat(model1.getMetrics()).hasSize(3);
        assertThat(model1.getMetrics().get("metric1").getCount()).isEqualTo(3);
        assertThat(model1.getMetrics().get("metric2").getCount()).isEqualTo(1);
        assertThat(model1.getMetrics().get("metric3").getCount()).isEqualTo(2);
        assertThat(model1.getMetrics().get("metric1").getTotalLatencyMs()).isEqualTo(400L);
        assertThat(model1.getMetrics().get("metric2").getTotalLatencyMs()).isEqualTo(200L);
        assertThat(model1.getMetrics().get("metric3").getTotalLatencyMs()).isEqualTo(600L);
    }

    @Test
    public void shouldMergeEmptyMetrics() {
        PartitionMetricWindowModel model1 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model1.incrementCount("metric1");

        PartitionMetricWindowModel model2 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);

        model1.mergeFrom(model2);

        assertThat(model1.getMetrics()).hasSize(1);
        assertThat(model1.getMetrics().get("metric1").getCount()).isEqualTo(1);
    }

    @Test
    public void shouldMergeIntoEmptyMetrics() {
        PartitionMetricWindowModel model1 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);

        PartitionMetricWindowModel model2 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model2.incrementCount("metric1");
        model2.incrementCountAndLatency("metric2", 500L);

        model1.mergeFrom(model2);

        assertThat(model1.getMetrics()).hasSize(2);
        assertThat(model1.getMetrics().get("metric1").getCount()).isEqualTo(1);
        assertThat(model1.getMetrics().get("metric2").getCount()).isEqualTo(1);
        assertThat(model1.getMetrics().get("metric2").getTotalLatencyMs()).isEqualTo(500L);
    }

    @Test
    public void shouldTrackWfRunStarted() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        Date startTime = new Date(1000L);

        model.incrementWfCount(null, LHStatus.RUNNING, startTime, null);

        assertThat(model.getMetrics()).hasSize(1);
        assertThat(model.getMetrics()).containsKey("started");
        assertThat(model.getMetrics().get("started").getCount()).isEqualTo(1);
    }

    @Test
    public void shouldTrackWfRunTransition() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        Date startTime = new Date(1000L);
        Date endTime = new Date(6000L); // 5 seconds later

        model.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, startTime, endTime);

        assertThat(model.getMetrics()).hasSize(1);
        String expectedKey = "running_to_completed";
        assertThat(model.getMetrics()).containsKey(expectedKey);
        CountAndTimingModel timing = model.getMetrics().get(expectedKey);
        assertThat(timing.getCount()).isEqualTo(1);
        assertThat(timing.getTotalLatencyMs()).isEqualTo(5000L);
        assertThat(timing.getMinLatencyMs()).isEqualTo(5000L);
        assertThat(timing.getMaxLatencyMs()).isEqualTo(5000L);
    }

    @Test
    public void shouldUseCurrentTimeWhenEndTimeIsNull() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        Date startTime = new Date(System.currentTimeMillis() - 1000L); // 1 second ago

        model.incrementWfCount(LHStatus.RUNNING, LHStatus.HALTED, startTime, null);

        assertThat(model.getMetrics()).hasSize(1);
        String expectedKey = "running_to_halted";
        assertThat(model.getMetrics()).containsKey(expectedKey);
        CountAndTimingModel timing = model.getMetrics().get(expectedKey);
        assertThat(timing.getCount()).isEqualTo(1);
        assertThat(timing.getTotalLatencyMs()).isGreaterThanOrEqualTo(1000L);
    }

    @Test
    public void shouldTrackMultipleTransitions() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);

        model.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, new Date(1000L), new Date(3000L));
        model.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, new Date(2000L), new Date(5000L));
        model.incrementWfCount(LHStatus.RUNNING, LHStatus.ERROR, new Date(1000L), new Date(2000L));

        assertThat(model.getMetrics()).hasSize(2);

        CountAndTimingModel completedTiming = model.getMetrics().get("running_to_completed");
        assertThat(completedTiming.getCount()).isEqualTo(2);
        assertThat(completedTiming.getTotalLatencyMs()).isEqualTo(5000L); // 2000 + 3000
        assertThat(completedTiming.getMinLatencyMs()).isEqualTo(2000L);
        assertThat(completedTiming.getMaxLatencyMs()).isEqualTo(3000L);

        CountAndTimingModel errorTiming = model.getMetrics().get("running_to_error");
        assertThat(errorTiming.getCount()).isEqualTo(1);
        assertThat(errorTiming.getTotalLatencyMs()).isEqualTo(1000L);
    }

    @Test
    public void shouldTrackDifferentStatusTransitions() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);

        model.incrementWfCount(LHStatus.STARTING, LHStatus.RUNNING, new Date(1000L), new Date(2000L));
        model.incrementWfCount(LHStatus.RUNNING, LHStatus.HALTED, new Date(2000L), new Date(3000L));
        model.incrementWfCount(LHStatus.HALTED, LHStatus.RUNNING, new Date(3000L), new Date(4000L));
        model.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, new Date(4000L), new Date(5000L));

        assertThat(model.getMetrics()).hasSize(4);
        assertThat(model.getMetrics()).containsKey("starting_to_running");
        assertThat(model.getMetrics()).containsKey("running_to_halted");
        assertThat(model.getMetrics()).containsKey("halted_to_running");
        assertThat(model.getMetrics()).containsKey("running_to_completed");
    }

    @Test
    public void shouldCalculateZeroLatencyForSameTime() {
        PartitionMetricWindowModel model = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        Date time = new Date(1000L);

        model.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, time, time);

        CountAndTimingModel timing = model.getMetrics().get("running_to_completed");
        assertThat(timing.getTotalLatencyMs()).isEqualTo(0L);
        assertThat(timing.getMinLatencyMs()).isEqualTo(0L);
        assertThat(timing.getMaxLatencyMs()).isEqualTo(0L);
    }

    @Test
    public void shouldCombineTrackingAndMerging() {
        PartitionMetricWindowModel model1 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model1.incrementWfCount(null, LHStatus.RUNNING, new Date(1000L), null);
        model1.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, new Date(1000L), new Date(3000L));

        PartitionMetricWindowModel model2 = new PartitionMetricWindowModel(wfSpecId, tenantId, windowStart);
        model2.incrementWfCount(null, LHStatus.RUNNING, new Date(2000L), null);
        model2.incrementWfCount(LHStatus.RUNNING, LHStatus.COMPLETED, new Date(2000L), new Date(5000L));

        model1.mergeFrom(model2);

        assertThat(model1.getMetrics()).hasSize(2);
        assertThat(model1.getMetrics().get("started").getCount()).isEqualTo(2);

        CountAndTimingModel completedTiming = model1.getMetrics().get("running_to_completed");
        assertThat(completedTiming.getCount()).isEqualTo(2);
        assertThat(completedTiming.getTotalLatencyMs()).isEqualTo(5000L);
    }
}

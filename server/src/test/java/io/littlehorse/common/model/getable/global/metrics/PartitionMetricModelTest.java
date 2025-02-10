package io.littlehorse.common.model.getable.global.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.server.TestTenantScopedStore;
import io.littlehorse.server.streams.store.StoredGetable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PartitionMetricModelTest {

    private final MetricIdModel workflowRunningMetricId =
            new MetricIdModel(MeasurableObject.WORKFLOW, MetricType.COUNT);
    private final TestTenantScopedStore tenantScopedStore = new TestTenantScopedStore();

    @Test
    void shouldGroupSamplesInASingleWindow() {
        PartitionMetricModel partitionMetric = new PartitionMetricModel(workflowRunningMetricId, Duration.ofMinutes(1));
        partitionMetric.incrementCurrentWindow(LocalDateTime.now());
        partitionMetric.incrementCurrentWindow(LocalDateTime.now());
        partitionMetric.incrementCurrentWindow(LocalDateTime.now());
        Set<PartitionWindowedMetricModel> activeWindows = partitionMetric.getActiveWindowedMetrics();
        assertThat(activeWindows).hasSize(1).allSatisfy(windowedMetric -> assertThat(windowedMetric.getValue())
                .isEqualTo(3.0));
    }

    @Test
    void shouldGroupSamplesInASecondWindow() {
        PartitionMetricModel partitionMetric = new PartitionMetricModel(workflowRunningMetricId, Duration.ofMinutes(1));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusMinutes(2);
        partitionMetric.incrementCurrentWindow(now);
        partitionMetric.incrementCurrentWindow(now);
        partitionMetric.incrementCurrentWindow(now);
        partitionMetric.incrementCurrentWindow(later);
        partitionMetric.incrementCurrentWindow(later);
        StoredGetable<PartitionMetric, PartitionMetricModel> storedGetable = new StoredGetable<>(partitionMetric);
        tenantScopedStore.put(storedGetable);
        partitionMetric = (PartitionMetricModel) tenantScopedStore
                .get(storedGetable.getStoreKey(), StoredGetable.class)
                .getStoredObject();
        Iterator<PartitionWindowedMetricModel> activeWindows =
                partitionMetric.getActiveWindowedMetrics().iterator();
        assertThat(activeWindows.next()).satisfies(windowedMetric -> assertThat(windowedMetric.getValue())
                .isEqualTo(2.0));
        assertThat(activeWindows.next()).satisfies(windowedMetric -> assertThat(windowedMetric.getValue())
                .isEqualTo(3.0));
    }
}

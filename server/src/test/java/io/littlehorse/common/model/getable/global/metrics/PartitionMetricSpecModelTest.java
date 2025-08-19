package io.littlehorse.common.model.getable.global.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.RepartitionWindowedMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.NodeReferenceModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.server.TestTenantScopedStore;
import io.littlehorse.server.streams.store.StoredGetable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PartitionMetricSpecModelTest {

    private final MetricSpecIdModel workflowRunningMetricId = new MetricSpecIdModel(new NodeReferenceModel("TaskNode"));
    private final TestTenantScopedStore tenantScopedStore = new TestTenantScopedStore();
    private final TenantIdModel testTenantId = new TenantIdModel("test");
    private final PartitionMetricIdModel partitionMetricId =
            new PartitionMetricIdModel(workflowRunningMetricId, testTenantId, AggregationType.AVG);

    @Test
    void shouldGroupSamplesInASingleWindow() {
        PartitionMetricModel partitionMetric = new PartitionMetricModel(partitionMetricId, Duration.ofMinutes(1));
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        Set<PartitionWindowedMetricModel> activeWindows = partitionMetric.getActiveWindowedMetrics();
        assertThat(activeWindows).hasSize(1).allSatisfy(windowedMetric -> assertThat(windowedMetric.getValue())
                .isEqualTo(3.0));
    }

    @Test
    void shouldGroupSamplesInASecondWindow() {
        PartitionMetricModel partitionMetric = new PartitionMetricModel(partitionMetricId, Duration.ofMinutes(1));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusMinutes(2);
        partitionMetric.incrementCurrentWindow(now, 1);
        partitionMetric.incrementCurrentWindow(now, 1);
        partitionMetric.incrementCurrentWindow(now, 1);
        partitionMetric.incrementCurrentWindow(later, 1);
        partitionMetric.incrementCurrentWindow(later, 1);
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

    @Test
    void shouldBuildRepartitionCommand() {
        PartitionMetricModel partitionMetric = new PartitionMetricModel(partitionMetricId, Duration.ofMinutes(1));
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        partitionMetric.incrementCurrentWindow(LocalDateTime.now(), 1);
        List<RepartitionWindowedMetricModel> windowedMetrics =
                partitionMetric.buildRepartitionCommand(LocalDateTime.now());
        assertThat(windowedMetrics).isNotEmpty();
        assertThat(windowedMetrics).hasSize(1).allSatisfy(windowedMetric -> {
            PartitionWindowedMetricModel partitionWindowed =
                    partitionMetric.getActiveWindowedMetrics().iterator().next();
            assertThat(windowedMetric.getValue()).isEqualTo(partitionWindowed.getValue());
            assertThat(windowedMetric.getWindowStart()).isEqualTo(partitionWindowed.getWindowStart());
        });
    }

    @Test
    void shouldRemoveClosedWindowsAfterRepartition() {
        LocalDateTime instant1 = LocalDateTime.now();
        LocalDateTime instant2 = instant1.plusDays(2);
        LocalDateTime instant3 = instant2.plusDays(2);
        PartitionMetricModel partitionMetric = new PartitionMetricModel(partitionMetricId, Duration.ofMinutes(1));
        partitionMetric.incrementCurrentWindow(instant1, 1);
        partitionMetric.incrementCurrentWindow(instant1, 1);
        List<RepartitionWindowedMetricModel> windowedMetrics1 = partitionMetric.buildRepartitionCommand(instant1);
        partitionMetric.incrementCurrentWindow(instant2, 1);
        partitionMetric.incrementCurrentWindow(instant2, 1);
        List<RepartitionWindowedMetricModel> windowedMetrics2 = partitionMetric.buildRepartitionCommand(instant2);
        partitionMetric.incrementCurrentWindow(instant3, 1);
        partitionMetric.incrementCurrentWindow(instant3, 1);
        List<RepartitionWindowedMetricModel> windowedMetrics3 = partitionMetric.buildRepartitionCommand(instant3);

        assertThat(windowedMetrics1).hasSize(1);
        assertThat(windowedMetrics2).hasSize(2);
        assertThat(windowedMetrics3).hasSize(2);
    }
}

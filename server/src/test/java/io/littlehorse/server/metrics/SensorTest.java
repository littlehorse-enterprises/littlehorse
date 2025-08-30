package io.littlehorse.server.metrics;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricInventoryModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.NodeReferenceModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.storeinternals.InMemoryGetableManager;
import io.littlehorse.storeinternals.InMemoryMetadataManager;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SensorTest {

    private final CoreProcessorContext executionContext = mock(CoreProcessorContext.class);
    private final PartitionMetricInventoryModel inventory = mock(PartitionMetricInventoryModel.class);
    private final InMemoryGetableManager inMemoryGetableManager = new InMemoryGetableManager(executionContext);
    private final InMemoryMetadataManager inMemoryMetadataManager = new InMemoryMetadataManager();
    private final AuthorizationContext auth = mock(AuthorizationContextImpl.class);
    private final TenantIdModel tenantId = new TenantIdModel("test-tenant");
    private final ArgumentCaptor<PartitionMetricIdModel> addedMetricsCaptor =
            ArgumentCaptor.forClass(PartitionMetricIdModel.class);

    @BeforeEach
    public void setup() {
        when(auth.tenantId()).thenReturn(tenantId);
        when(executionContext.metadataManager()).thenReturn(inMemoryMetadataManager);
        when(executionContext.getableManager()).thenReturn(inMemoryGetableManager);
        when(executionContext.authorization()).thenReturn(auth);
        when(executionContext.metricsInventory()).thenReturn(inventory);
    }

    @Nested
    public final class TaskRunMetrics {
        private final TaskDefIdModel taskDef1 = new TaskDefIdModel("task1");
        private final TaskDefIdModel taskDef2 = new TaskDefIdModel("task2");
        private final NodeRunIdModel nodeRun = mock();
        private final MetricSpecIdModel metricTaskId = new MetricSpecIdModel(new NodeReferenceModel("TaskNode"));
        private final MetricSpecModel countTask =
                new MetricSpecModel(metricTaskId, Duration.ofMinutes(1), Set.of(AggregationType.COUNT));

        @Test
        public void shouldIncrementTaskRunCounter() {
            TaskRunStatusUpdate update1 = new TaskRunStatusUpdate(taskDef1, TaskStatus.TASK_SCHEDULED, nodeRun);
            TaskRunStatusUpdate update2 = new TaskRunStatusUpdate(taskDef1, TaskStatus.TASK_SCHEDULED, nodeRun);
            storeMetric(countTask);
            Sensor sensor = new Sensor(Set.of(metricTaskId), executionContext);
            sensor.record(update1);
            sensor.record(update2);
            Set<PartitionMetricIdModel> addedMetrics = getAddedMetrics(2);
            PartitionMetricIdModel partitionMetricIdModel = addedMetrics.toArray(new PartitionMetricIdModel[0])[0];
            assertThat(partitionMetricIdModel.getAggregationType()).isEqualTo(AggregationType.COUNT);
            PartitionMetricModel partitionMetric = inMemoryGetableManager.get(partitionMetricIdModel);
            assertThat(partitionMetric.getActiveWindowedMetrics()).hasSize(1).allSatisfy(Objects::requireNonNull);
        }
    }

    private Set<PartitionMetricIdModel> getAddedMetrics(int expectedNumberOfMetrics) {
        verify(inventory, times(expectedNumberOfMetrics)).addMetric(addedMetricsCaptor.capture());
        List<PartitionMetricIdModel> addedMetrics = addedMetricsCaptor.getAllValues();
        assertThat(addedMetrics)
                .hasSize(expectedNumberOfMetrics)
                .withFailMessage("Expected %s metrics to be added", expectedNumberOfMetrics);
        return new HashSet<>(addedMetricsCaptor.getAllValues());
    }

    private void storeMetric(MetricSpecModel metric) {
        inMemoryMetadataManager.put(metric);
    }
}

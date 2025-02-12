package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.PartitionMetricsModel;
import io.littlehorse.common.model.TaskStatusChangedModel;
import io.littlehorse.common.model.getable.global.metrics.MetricModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricInventoryModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.MeasurableObject;
import io.littlehorse.sdk.common.proto.Metric;
import io.littlehorse.sdk.common.proto.MetricType;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.GetableUpdates.GetableStatusUpdate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsUpdater implements GetableUpdates.GetableStatusListener {
    // decide when to persist partition metrics
    private boolean dirtyState = false;
    private final ReadOnlyTenantScopedStore metadataStore;
    private final TenantScopedStore tenantStore;
    private PartitionMetricsModel aggregateModel;
    private ClusterScopedStore clusterScopedCoreStore;

    public MetricsUpdater(
            ReadOnlyTenantScopedStore metadataStore,
            TenantScopedStore tenantStore,
            ClusterScopedStore clusterScopedCoreStore) {
        this.metadataStore = metadataStore;
        this.tenantStore = tenantStore;
        this.clusterScopedCoreStore = clusterScopedCoreStore;
    }

    @Override
    public void listen(GetableStatusUpdate statusUpdate) {
        if (statusUpdate instanceof GetableUpdates.WfRunStatusUpdate wfRunEvent) {
            StoredGetable<Metric, MetricModel> storedGetable = metadataStore.get(
                    new MetricIdModel(MeasurableObject.WORKFLOW, MetricType.COUNT).getStoreableKey(),
                    StoredGetable.class);
            if (storedGetable != null) {
                MetricModel metric = storedGetable.getStoredObject();
                if (metric != null) {
                    if (wfRunEvent.getNewStatus().equals(LHStatus.RUNNING)) {
                        StoredGetable<PartitionMetric, PartitionMetricModel> getable = tenantStore.get(
                                new PartitionMetricIdModel(metric.getObjectId(), statusUpdate.getTenantId())
                                        .getStoreableKey(),
                                StoredGetable.class);
                        if (getable == null) {
                            getable = new StoredGetable<>(new PartitionMetricModel(
                                    metric.getObjectId(), metric.getWindowLength(), statusUpdate.getTenantId()));
                        }
                        PartitionMetricInventoryModel partitionMetricInventory = clusterScopedCoreStore.get(
                                PartitionMetricInventoryModel.METRIC_INVENTORY_STORE_KEY,
                                PartitionMetricInventoryModel.class);
                        if (partitionMetricInventory == null) {
                            partitionMetricInventory = new PartitionMetricInventoryModel();
                        }
                        clusterScopedCoreStore.put(new PartitionMetricInventoryModel());
                        PartitionMetricModel partitionMetric = getable.getStoredObject();
                        partitionMetric.incrementCurrentWindow(LocalDateTime.now());
                        boolean added = partitionMetricInventory.addMetric(partitionMetric.getObjectId());
                        if (added) {
                            clusterScopedCoreStore.put(partitionMetricInventory);
                        }
                        tenantStore.put(new StoredGetable<>(partitionMetric));
                    }
                }
            }

        } else if (statusUpdate instanceof GetableUpdates.TaskRunStatusUpdate taskUpdate) {
            TaskStatusChangedModel taskStatusChanged =
                    new TaskStatusChangedModel(taskUpdate.getPreviousStatus(), taskUpdate.getNewStatus());
            currentAggregateCommand()
                    .addMetric(
                            taskUpdate.getTaskDefId(),
                            taskUpdate.getTenantId(),
                            taskStatusChanged,
                            taskUpdate.getCreationDate(),
                            taskUpdate.getFirstEventLatency());
        } else {
            throw new IllegalArgumentException("Status Update %s not supported yet"
                    .formatted(statusUpdate.getClass().getSimpleName()));
        }
        dirtyState = true;
    }

    private PartitionMetricsModel currentAggregateCommand() {
        if (aggregateModel == null) {
            aggregateModel = Optional.ofNullable(
                            metadataStore.get(LHConstants.PARTITION_METRICS_KEY, PartitionMetricsModel.class))
                    .orElse(new PartitionMetricsModel());
        }
        return aggregateModel;
    }

    public void maybePersistState() {}
}

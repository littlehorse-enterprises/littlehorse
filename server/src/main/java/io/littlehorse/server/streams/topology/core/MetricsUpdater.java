package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.model.getable.global.metrics.MetricSpecModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricInventoryModel;
import io.littlehorse.common.model.getable.global.metrics.PartitionMetricModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.PartitionMetricIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.PartitionMetric;
import io.littlehorse.server.metrics.GetableStatusListener;
import io.littlehorse.server.metrics.GetableStatusUpdate;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsUpdater implements GetableStatusListener {
    // decide when to persist partition metrics
    private boolean dirtyState = false;
    private final ReadOnlyTenantScopedStore metadataStore;
    private final TenantScopedStore tenantStore;
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
        for (MetricSpecIdModel metricSpecId : statusUpdate.toMetricId()) {
            StoredGetable<MetricSpec, MetricSpecModel> stored =
                    metadataStore.get(metricSpecId.getStoreableKey(), StoredGetable.class);
            if (stored != null) {
                MetricSpecModel metricSpec = stored.getStoredObject();
                for (AggregationType aggregationType : metricSpec.getAggregateAs()) {
                    for (Duration windowLength : metricSpec.getWindowLengths()) {
                        StoredGetable<PartitionMetric, PartitionMetricModel> getable = tenantStore.get(
                                new PartitionMetricIdModel(
                                                metricSpec.getObjectId(), statusUpdate.getTenantId(), aggregationType)
                                        .getStoreableKey(),
                                StoredGetable.class);
                        if (getable == null) {
                            getable = new StoredGetable<>(new PartitionMetricModel(
                                    metricSpec.getObjectId(),
                                    windowLength,
                                    statusUpdate.getTenantId(),
                                    aggregationType));
                        }
                        PartitionMetricInventoryModel partitionMetricInventory = clusterScopedCoreStore.get(
                                PartitionMetricInventoryModel.METRIC_INVENTORY_STORE_KEY,
                                PartitionMetricInventoryModel.class);
                        if (partitionMetricInventory == null) {
                            partitionMetricInventory = new PartitionMetricInventoryModel();
                        }
                        PartitionMetricModel partitionMetric = getable.getStoredObject();
                        partitionMetric.incrementCurrentWindow(
                                LocalDateTime.now(), statusUpdate.getMetricIncrementValue(aggregationType));
                        boolean added = partitionMetricInventory.addMetric(partitionMetric.getObjectId());
                        if (added) {
                            log.info("added partition metric");
                            clusterScopedCoreStore.put(partitionMetricInventory);
                        }
                        tenantStore.put(new StoredGetable<>(partitionMetric));
                    }
                }
            }
        }
        dirtyState = true;
    }

    public void maybePersistState() {}
}

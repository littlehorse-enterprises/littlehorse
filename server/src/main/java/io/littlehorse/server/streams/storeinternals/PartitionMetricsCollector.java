package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.model.PartitionMetricWindowModel;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionAccumulator;
import java.util.Date;

/**
 * Encapsulates partition-local metric collection: looking up or creating
 * metric windows, incrementing counters, and persisting to both RocksDB
 * and the in-memory accumulator.
 */
public class PartitionMetricsCollector {

    private final ClusterScopedStore store;
    private final PartitionAccumulator<PartitionMetricWindowModel> accumulator;
    private final TenantIdModel tenantId;

    public PartitionMetricsCollector(
            ClusterScopedStore store,
            PartitionAccumulator<PartitionMetricWindowModel> accumulator,
            TenantIdModel tenantId) {
        this.store = store;
        this.accumulator = accumulator;
        this.tenantId = tenantId;
    }

    public void trackWorkflow(
            WfSpecIdModel wfSpecId, LHStatus previousStatus, LHStatus newStatus, Date startTime, Date endTime) {
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, wfSpecId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel window = getOrCreate(id);
        window.incrementWfCount(previousStatus, newStatus, startTime, endTime);
        persist(window);
    }

    public void trackTaskAttempt(
            TaskDefIdModel taskDefId, TaskStatus previousStatus, TaskStatus newStatus, Date phaseStart, Date phaseEnd) {
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel window = getOrCreate(id);
        window.incrementTaskAttemptCount(previousStatus, newStatus, phaseStart, phaseEnd);
        persist(window);
    }

    public void trackTaskRun(TaskDefIdModel taskDefId, TaskStatus terminalStatus, Date createdAt, Date endTime) {
        MetricWindowIdModel id = new MetricWindowIdModel(tenantId, taskDefId, LHUtil.getCurrentWindowDate());
        PartitionMetricWindowModel window = getOrCreate(id);
        window.incrementTaskRunCount(terminalStatus, createdAt, endTime);
        persist(window);
    }

    private PartitionMetricWindowModel getOrCreate(MetricWindowIdModel id) {
        String storeKey = id.getPartitionMetricStoreKey();
        PartitionMetricWindowModel window = accumulator.get(storeKey);
        if (window == null) {
            window = store.get(storeKey, PartitionMetricWindowModel.class);
        }
        if (window == null) {
            window = new PartitionMetricWindowModel(id);
        }
        return window;
    }

    private void persist(PartitionMetricWindowModel window) {
        store.put(window);
        accumulator.put(window);
    }
}

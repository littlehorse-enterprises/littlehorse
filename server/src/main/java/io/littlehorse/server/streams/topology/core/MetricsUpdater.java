package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHStatusChangedModel;
import io.littlehorse.common.model.PartitionMetricsModel;
import io.littlehorse.common.model.TaskStatusChangedModel;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.topology.core.GetableUpdates.GetableStatusUpdate;
import java.util.Optional;

public class MetricsUpdater implements GetableUpdates.GetableStatusListener {
    // decide when to persist partition metrics
    private boolean dirtyState = false;
    private final ClusterScopedStore modelStore;
    private PartitionMetricsModel aggregateModel;

    public MetricsUpdater(ClusterScopedStore modelStore) {
        this.modelStore = modelStore;
    }

    @Override
    public void listen(GetableStatusUpdate statusUpdate) {
        if (statusUpdate instanceof GetableUpdates.WfRunStatusUpdate wfRunEvent) {
            LHStatusChangedModel statusChanged =
                    new LHStatusChangedModel(wfRunEvent.getPreviousStatus(), wfRunEvent.getNewStatus());
            currentAggregateCommand()
                    .addMetric(
                            wfRunEvent.getWfSpecId(),
                            wfRunEvent.getTenantId(),
                            statusChanged,
                            wfRunEvent.getCreationDate(),
                            wfRunEvent.getFirstEventLatency());
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
                            modelStore.get(LHConstants.PARTITION_METRICS_KEY, PartitionMetricsModel.class))
                    .orElse(new PartitionMetricsModel());
        }
        return aggregateModel;
    }

    public void maybePersistState() {
        if (dirtyState) {
            this.modelStore.put(currentAggregateCommand());
        }
    }
}

package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.TaskStatus;
import java.util.List;
import lombok.Getter;

@Getter
public class TaskRunStatusUpdate extends GetableNodeStatusUpdate {
    private final TaskDefIdModel taskDefId;
    private final TaskStatus newStatus;

    public TaskRunStatusUpdate(TaskDefIdModel taskDefId, TaskStatus newStatus, NodeRunIdModel nodeRunId) {
        super(nodeRunId);
        this.taskDefId = taskDefId;
        this.newStatus = newStatus;
    }

    @Override
    public List<MetricSpecIdModel> toMetricId() {
        return List.of();
    }

    @Override
    public double getMetricIncrementValue(AggregationType aggregationType) {
        return 1;
    }
}

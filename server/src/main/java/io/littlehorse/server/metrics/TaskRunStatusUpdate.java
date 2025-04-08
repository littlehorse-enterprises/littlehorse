package io.littlehorse.server.metrics;

import io.littlehorse.common.model.getable.core.taskrun.TaskNodeReferenceModel;
import io.littlehorse.common.model.getable.objectId.MetricSpecIdModel;
import io.littlehorse.common.model.getable.objectId.NodeReferenceModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.ThreadSpecReferenceModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.TaskStatus;
import java.util.List;
import lombok.Getter;

@Getter
public class TaskRunStatusUpdate extends GetableStatusUpdate {
    private final TaskDefIdModel taskDefId;
    private final TaskStatus previousStatus;
    private final TaskStatus newStatus;
    private final WfSpecIdModel wfSpecId;


    public TaskRunStatusUpdate(
            TaskDefIdModel taskDefId, WfSpecIdModel wfSpecId, TaskStatus previousStatus, TaskStatus newStatus) {
        this.taskDefId = taskDefId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.wfSpecId = wfSpecId;
    }

    @Override
    public List<MetricSpecIdModel> toMetricId() {
        return List.of(new MetricSpecIdModel(wfSpecId));
    }

    @Override
    public double getMetricIncrementValue(AggregationType aggregationType) {
        return 1;
    }
}

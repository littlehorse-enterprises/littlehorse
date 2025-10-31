package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;

public class TaskRunIdModel extends CoreObjectId<TaskRunId, TaskRun, TaskRunModel> {

    @Getter
    public WfRunIdModel wfRunId;

    public String taskGuid;

    public TaskRunIdModel() {}

    public TaskRunIdModel(WfRunIdModel wfRunId, String guid) {
        this.wfRunId = wfRunId;
        this.taskGuid = guid;
    }

    public TaskRunIdModel(NodeRunIdModel nodeRunId, CoreProcessorContext processorContext) {
        this(nodeRunId.getWfRunId(), nodeRunId.getThreadRunNumber() + "-" + nodeRunId.getPosition());
    }

    public TaskRunIdModel(UserTaskRunModel userTaskRun) {
        this(
                userTaskRun.getId().getWfRunId(),
                "ut-" + userTaskRun.getNodeRunId().getThreadRunNumber() + "-"
                        + userTaskRun.getNodeRunId().getPosition() + "-"
                        + userTaskRun.getEvents().size());
    }

    @Override
    public Class<TaskRunId> getProtoBaseClass() {
        return TaskRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskRunId p = (TaskRunId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        taskGuid = p.getTaskGuid();
    }

    @Override
    public TaskRunId.Builder toProto() {
        TaskRunId.Builder out =
                TaskRunId.newBuilder().setWfRunId(wfRunId.toProto()).setTaskGuid(taskGuid);
        return out;
    }

    @Override
    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.of(wfRunId);
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), taskGuid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        taskGuid = split[1];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_RUN;
    }
}

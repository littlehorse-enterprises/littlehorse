package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class TaskWorkerGroupIdModel extends CoreObjectId<TaskWorkerGroupId, TaskWorkerGroup, TaskWorkerGroupModel> {

    public String taskDefName;

    public TaskWorkerGroupIdModel() {}

    public TaskWorkerGroupIdModel(String taskDefName) {
        this.taskDefName = taskDefName;
    }

    @Override
    public Class<TaskWorkerGroupId> getProtoBaseClass() {
        return TaskWorkerGroupId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(taskDefName);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskWorkerGroupId p = (TaskWorkerGroupId) proto;
        taskDefName = p.getTaskDefName();
    }

    @Override
    public TaskWorkerGroupId.Builder toProto() {
        TaskWorkerGroupId.Builder out = TaskWorkerGroupId.newBuilder().setTaskDefName(taskDefName);
        return out;
    }

    @Override
    public String toString() {
        return taskDefName;
    }

    @Override
    public void initFromString(String storeKey) {
        taskDefName = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_WORKER_GROUP;
    }
}

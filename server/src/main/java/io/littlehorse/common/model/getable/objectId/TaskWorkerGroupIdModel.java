package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class TaskWorkerGroupIdModel extends CoreObjectId<TaskWorkerGroupId, TaskWorkerGroup, TaskWorkerGroupModel> {

    public TaskDefIdModel taskDefId;

    public TaskWorkerGroupIdModel() {}

    public TaskWorkerGroupIdModel(TaskDefIdModel taskDefId) {
        this.taskDefId = taskDefId;
    }

    @Override
    public Class<TaskWorkerGroupId> getProtoBaseClass() {
        return TaskWorkerGroupId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        // taskDefId does not have a partition key, so we need to create
        // our own rather than just do taskDefId.getPartitionKey().
        return Optional.of(taskDefId.getName());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskWorkerGroupId p = (TaskWorkerGroupId) proto;
        taskDefId = LHSerializable.fromProto(p.getTaskDefId(), TaskDefIdModel.class);
    }

    @Override
    public TaskWorkerGroupId.Builder toProto() {
        TaskWorkerGroupId.Builder out = TaskWorkerGroupId.newBuilder().setTaskDefId(taskDefId.toProto());
        return out;
    }

    @Override
    public String toString() {
        return taskDefId.toString();
    }

    @Override
    public void initFromString(String storeKey) {
        taskDefId = (TaskDefIdModel) ObjectIdModel.fromString(storeKey, TaskDefIdModel.class);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_WORKER_GROUP;
    }
}

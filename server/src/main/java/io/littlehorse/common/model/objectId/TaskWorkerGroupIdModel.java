package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskWorkerGroupModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.TaskWorkerGroup;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupId;

public class TaskWorkerGroupIdModel extends ObjectId<TaskWorkerGroupId, TaskWorkerGroup, TaskWorkerGroupModel> {

    public String taskDefName;

    public TaskWorkerGroupIdModel() {}

    public TaskWorkerGroupIdModel(String taskDefName) {
        this.taskDefName = taskDefName;
    }

    public Class<TaskWorkerGroupId> getProtoBaseClass() {
        return TaskWorkerGroupId.class;
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public void initFrom(Message proto) {
        TaskWorkerGroupId p = (TaskWorkerGroupId) proto;
        taskDefName = p.getTaskDefName();
    }

    public TaskWorkerGroupId.Builder toProto() {
        TaskWorkerGroupId.Builder out = TaskWorkerGroupId.newBuilder().setTaskDefName(taskDefName);
        return out;
    }

    public String getStoreKey() {
        return taskDefName;
    }

    public void initFrom(String storeKey) {
        taskDefName = storeKey;
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_WORKER_GROUP;
    }
}

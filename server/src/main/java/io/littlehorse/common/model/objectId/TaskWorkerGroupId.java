package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupIdPb;
import io.littlehorse.sdk.common.proto.TaskWorkerGroupPb;

public class TaskWorkerGroupId
    extends ObjectId<TaskWorkerGroupIdPb, TaskWorkerGroupPb, TaskWorkerGroup> {

    public String taskDefName;

    public TaskWorkerGroupId() {}

    public TaskWorkerGroupId(String taskDefName) {
        this.taskDefName = taskDefName;
    }

    public Class<TaskWorkerGroupIdPb> getProtoBaseClass() {
        return TaskWorkerGroupIdPb.class;
    }

    public String getPartitionKey() {
        return taskDefName;
    }

    public void initFrom(Message proto) {
        TaskWorkerGroupIdPb p = (TaskWorkerGroupIdPb) proto;
        taskDefName = p.getTaskDefName();
    }

    public TaskWorkerGroupIdPb.Builder toProto() {
        TaskWorkerGroupIdPb.Builder out = TaskWorkerGroupIdPb
            .newBuilder()
            .setTaskDefName(taskDefName);
        return out;
    }

    public String getStoreKey() {
        return taskDefName;
    }

    public void initFrom(String storeKey) {
        taskDefName = storeKey;
    }

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.TASK_WORKER_GROUP;
    }
}

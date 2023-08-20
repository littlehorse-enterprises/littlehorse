package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;

import io.littlehorse.common.model.getable.ObjectId;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;

public class TaskRunIdModel extends ObjectId<TaskRunId, TaskRun, TaskRunModel> {

    public String wfRunId;
    public String taskGuid;

    public TaskRunIdModel() {
    }

    public TaskRunIdModel(String partitionKey, String guid) {
        this.wfRunId = partitionKey;
        this.taskGuid = guid;
    }

    public TaskRunIdModel(String partitionKey) {
        this(partitionKey, LHUtil.generateGuid());
    }

    public Class<TaskRunId> getProtoBaseClass() {
        return TaskRunId.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        TaskRunId p = (TaskRunId) proto;
        wfRunId = p.getWfRunId();
        taskGuid = p.getTaskGuid();
    }

    public TaskRunId.Builder toProto() {
        TaskRunId.Builder out = TaskRunId.newBuilder().setWfRunId(wfRunId).setTaskGuid(taskGuid);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(wfRunId, taskGuid);
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        taskGuid = split[1];
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_RUN;
    }
}

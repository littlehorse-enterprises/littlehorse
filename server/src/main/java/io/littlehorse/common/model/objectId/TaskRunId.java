package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;

public class TaskRunId extends ObjectId<TaskRunIdPb, TaskRunPb, TaskRun> {

    public String wfRunId;
    public String taskGuid;

    public TaskRunId() {}

    public TaskRunId(String partitionKey, String guid) {
        this.wfRunId = partitionKey;
        this.taskGuid = guid;
    }

    public TaskRunId(String partitionKey) {
        this(partitionKey, LHUtil.generateGuid());
    }

    public Class<TaskRunIdPb> getProtoBaseClass() {
        return TaskRunIdPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        TaskRunIdPb p = (TaskRunIdPb) proto;
        wfRunId = p.getWfRunId();
        taskGuid = p.getTaskGuid();
    }

    public TaskRunIdPb.Builder toProto() {
        TaskRunIdPb.Builder out = TaskRunIdPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setTaskGuid(taskGuid);
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

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.TASK_RUN;
    }
}

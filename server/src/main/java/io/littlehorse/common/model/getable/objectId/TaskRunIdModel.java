package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import java.util.Optional;

public class TaskRunIdModel extends CoreObjectId<TaskRunId, TaskRun, TaskRunModel> {

    public String wfRunId;
    public String taskGuid;

    public TaskRunIdModel() {}

    public TaskRunIdModel(String partitionKey, String guid) {
        this.wfRunId = partitionKey;
        this.taskGuid = guid;
    }

    public TaskRunIdModel(String partitionKey) {
        this(partitionKey, LHUtil.generateGuid());
    }

    @Override
    public Class<TaskRunId> getProtoBaseClass() {
        return TaskRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFrom(Message proto) {
        TaskRunId p = (TaskRunId) proto;
        wfRunId = p.getWfRunId();
        taskGuid = p.getTaskGuid();
    }

    @Override
    public TaskRunId.Builder toProto() {
        TaskRunId.Builder out = TaskRunId.newBuilder().setWfRunId(wfRunId).setTaskGuid(taskGuid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId, taskGuid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        taskGuid = split[1];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.TASK_RUN;
    }
}

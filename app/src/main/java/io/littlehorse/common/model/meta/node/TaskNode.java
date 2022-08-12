package io.littlehorse.common.model.meta.node;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.wfspec.TaskNodePb;
import io.littlehorse.common.proto.wfspec.TaskNodePbOrBuilder;

public class TaskNode extends LHSerializable<TaskNodePb> {
    public String taskDefName;
    public Integer timeoutSeconds;
    public int retries;

    public Class<TaskNodePb> getProtoBaseClass() {
        return TaskNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskNodePbOrBuilder p = (TaskNodePbOrBuilder) proto;
        taskDefName = p.getTaskDefName();
        retries = p.getRetries();
        if (p.hasTimeoutSeconds()) timeoutSeconds = p.getTimeoutSeconds();
    }

    public TaskNodePb.Builder toProto() {
        TaskNodePb.Builder out = TaskNodePb.newBuilder()
            .setTaskDefName(taskDefName)
            .setRetries(retries);

        if (timeoutSeconds != null) {
            out.setTimeoutSeconds(timeoutSeconds);
        }
        return out;
    }
}

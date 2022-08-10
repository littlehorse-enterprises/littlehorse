package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.proto.observability.TaskStartOePb;

public class TaskStartOe extends LHSerializable<TaskStartOePb> {
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public String nodeName;

    public TaskStartOePb.Builder toProto() {
        TaskStartOePb.Builder out = TaskStartOePb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setNodeName(nodeName);

        return out;
    }

    public Class<TaskStartOePb> getProtoBaseClass() {
        return TaskStartOePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskStartOePb p = (TaskStartOePb) proto;
        threadRunNumber = p.getThreadRunNumber();
        taskRunNumber = p.getTaskRunNumber();
        taskRunPosition = p.getTaskRunPosition();
        nodeName = p.getNodeName();
    }

    public TaskStartOe() {}

    public static TaskStartOe fromProto(TaskStartOePb proto) {
        TaskStartOe out = new TaskStartOe();
        out.initFrom(proto);
        return out;
    }

    public TaskStartOe(TaskStartedEvent se, String nodename) {
        threadRunNumber = se.threadRunNumber;
        taskRunNumber = se.taskRunNumber;
        taskRunPosition = se.taskRunPosition;
        this.nodeName = nodename;
    }
}

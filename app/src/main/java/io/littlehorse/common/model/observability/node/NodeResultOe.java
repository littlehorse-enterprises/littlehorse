package io.littlehorse.common.model.observability.node;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.proto.NodeResultOePb;
import io.littlehorse.common.proto.NodeResultOePb.NodeTypeCase;
import io.littlehorse.common.proto.TaskResultCodePb;

public class NodeResultOe extends LHSerializable<NodeResultOePb> {

    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;

    public TaskResultCodePb resultCode;
    public String errorMessage;

    public NodeTypeCase type;
    public ExternalEventRunOe externalEvent;
    public TaskResultOe taskResult;

    public Class<NodeResultOePb> getProtoBaseClass() {
        return NodeResultOePb.class;
    }

    public NodeResultOePb.Builder toProto() {
        NodeResultOePb.Builder out = NodeResultOePb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setResultCode(resultCode);

        if (errorMessage != null) {
            out.setErrorMessage(errorMessage);
        }

        switch (type) {
            case TASK:
                out.setTask(taskResult.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEvent.toProto());
                break;
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        NodeResultOePb p = (NodeResultOePb) proto;
        threadRunNumber = p.getThreadRunNumber();
        taskRunNumber = p.getTaskRunNumber();
        taskRunPosition = p.getTaskRunPosition();

        resultCode = p.getResultCode();
        if (p.hasErrorMessage()) errorMessage = p.getErrorMessage();

        type = p.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskResult = TaskResultOe.fromProto(p.getTaskOrBuilder());
                break;
            case EXTERNAL_EVENT:
                externalEvent =
                    ExternalEventRunOe.fromProto(p.getExternalEventOrBuilder());
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public NodeResultOe() {}

    public NodeResultOe(TaskResultEvent evt, String nodeName) {
        threadRunNumber = evt.threadRunNumber;
        taskRunNumber = evt.taskRunNumber;
        taskRunPosition = evt.taskRunPosition;

        resultCode = evt.resultCode;
        type = NodeTypeCase.TASK;
        taskResult = new TaskResultOe();
        taskResult.logOutput = evt.stderr;
        taskResult.result = evt.stdout;
    }

    public static NodeResultOe fromProto(NodeResultOePb proto) {
        NodeResultOe out = new NodeResultOe();
        out.initFrom(proto);
        return out;
    }
}

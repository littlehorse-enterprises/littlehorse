package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.NodeRunState;
import io.littlehorse.common.proto.NodeReachedOePb;
import io.littlehorse.common.proto.NodeReachedOePb.NodeTypeCase;
import io.littlehorse.common.proto.NodeReachedOePbOrBuilder;

public class NodeReachedOe extends LHSerializable<NodeReachedOePb> {

    public String wfRunId;
    public int threadRunNumber;
    public int nodeRunPosition;

    public int nodeRunNumber;
    public String nodeName;

    public NodeTypeCase type;
    public WaitForEvtOe evt;
    public TaskScheduledOe task;

    public Class<NodeReachedOePb> getProtoBaseClass() {
        return NodeReachedOePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        NodeReachedOePbOrBuilder p = (NodeReachedOePbOrBuilder) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();

        nodeRunNumber = p.getNodeRunNumber();
        nodeName = p.getNodeName();

        type = p.getNodeTypeCase();

        switch (type) {
            case TASK:
                task = TaskScheduledOe.fromProto(p.getTask());
                break;
            case EVT:
                evt = WaitForEvtOe.fromProto(p.getEvt());
                break;
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
    }

    public NodeReachedOePb.Builder toProto() {
        NodeReachedOePb.Builder out = NodeReachedOePb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setNodeRunPosition(nodeRunPosition)
            .setNodeRunNumber(nodeRunNumber)
            .setNodeName(nodeName);

        switch (type) {
            case TASK:
                out.setTask(task.toProto());
                break;
            case EVT:
                out.setEvt(evt.toProto());
                break;
            case NODETYPE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    public static NodeReachedOe fromProto(NodeReachedOePbOrBuilder proto) {
        NodeReachedOe out = new NodeReachedOe();
        out.initFrom(proto);
        return out;
    }

    public NodeReachedOe() {}

    public NodeReachedOe(NodeRunState nrs) {
        wfRunId = nrs.threadRun.wfRunId;
        threadRunNumber = nrs.threadRun.number;
        nodeRunNumber = nrs.position;

        nodeRunNumber = nrs.number;
        nodeName = nrs.nodeName;
        switch (nrs.getNodeType()) {
            case TASK:
                type = NodeTypeCase.TASK;
                break;
            case EXTERNAL_EVENT:
                type = NodeTypeCase.EVT;
                break;
            default:
                throw new RuntimeException("Not possible");
        }
    }
}

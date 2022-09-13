package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.NodeRunStatePb;
import io.littlehorse.common.proto.NodeRunStatePbOrBuilder;

public class NodeRunState {

    public String nodeName;
    public int attemptNumber;
    public int position;
    public int number;
    public LHStatusPb status;

    public NodeRunState() {}

    // Below are implementation details
    @JsonIgnore
    public ThreadRun threadRun;

    public NodeRunStatePb.Builder toProto() {
        NodeRunStatePb.Builder b = NodeRunStatePb
            .newBuilder()
            .setNumber(number)
            .setNodeName(nodeName)
            .setAttemptNumber(attemptNumber)
            .setStatus(status)
            .setNodeName(nodeName)
            .setPosition(position);

        return b;
    }

    public static NodeRunState fromProto(NodeRunStatePbOrBuilder proto) {
        NodeRunState out = new NodeRunState();
        out.number = proto.getNumber();
        out.nodeName = proto.getNodeName();
        out.attemptNumber = proto.getAttemptNumber();
        out.status = proto.getStatus();
        out.position = proto.getPosition();
        return out;
    }
}

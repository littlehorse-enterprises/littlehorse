package io.littlehorse.scheduler.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.scheduler.NodeRunStatePb;
import io.littlehorse.common.proto.scheduler.NodeRunStatePbOrBuilder;

public class NodeRunState {
    public String nodeName;
    public int attemptNumber;
    public int position;
    public int number;
    public LHStatusPb status;
    public List<String> timerKeys;

    public NodeRunState() {
        timerKeys = new ArrayList<>();
    }

    // Below are implementation details
    @JsonIgnore public ThreadRunState threadRun;

    public NodeRunStatePb.Builder toProtoBuilder() {
        NodeRunStatePb.Builder b = NodeRunStatePb.newBuilder()
            .setNumber(number)
            .setNodeName(nodeName)
            .setAttemptNumber(attemptNumber)
            .setStatus(status)
            .setNodeName(nodeName)
            .setPosition(position);

        for (String tk: timerKeys) {
            b.addTimerKeys(tk);
        }
        return b;
    }

    public static NodeRunState fromProto(NodeRunStatePbOrBuilder proto) {
        NodeRunState out = new NodeRunState();
        out.number = proto.getNumber();
        out.nodeName = proto.getNodeName();
        out.attemptNumber = proto.getAttemptNumber();
        out.status = proto.getStatus();
        out.position = proto.getPosition();
        for (String tk: proto.getTimerKeysList()) {
            out.timerKeys.add(tk);
        }
        return out;
    }
}

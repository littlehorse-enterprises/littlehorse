package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.EdgePb;
import java.util.HashSet;
import java.util.Set;

public class Edge extends LHSerializable<EdgePb> {

    public String sinkNodeName;
    public EdgeCondition condition;

    public Class<EdgePb> getProtoBaseClass() {
        return EdgePb.class;
    }

    public EdgePb.Builder toProto() {
        EdgePb.Builder out = EdgePb.newBuilder().setSinkNodeName(sinkNodeName);

        if (condition != null) {
            out.setCondition(condition.toProto());
        }
        return out;
    }

    public void initFrom(Message p) {
        EdgePb proto = (EdgePb) p;
        sinkNodeName = proto.getSinkNodeName();
        if (proto.hasCondition()) {
            condition = EdgeCondition.fromProto(proto.getCondition());
            condition.edge = this;
        }
    }

    public static Edge fromProto(EdgePb proto) {
        Edge out = new Edge();
        out.initFrom(proto);
        return out;
    }

    // Implementation details below

    public ThreadSpec threadSpec;

    private Node sinkNode;

    public Node getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpec.nodes.get(sinkNodeName);
        }
        return sinkNode;
    }

    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();

        if (condition != null) {
            out.addAll(condition.getRequiredVariableNames());
        }

        return out;
    }
}

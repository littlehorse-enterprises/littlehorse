package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.Edge;
import java.util.HashSet;
import java.util.Set;

public class EdgeModel extends LHSerializable<Edge> {

    public String sinkNodeName;
    public EdgeConditionModel condition;

    public Class<Edge> getProtoBaseClass() {
        return Edge.class;
    }

    public Edge.Builder toProto() {
        Edge.Builder out = Edge.newBuilder().setSinkNodeName(sinkNodeName);

        if (condition != null) {
            out.setCondition(condition.toProto());
        }
        return out;
    }

    public void initFrom(Message p) {
        Edge proto = (Edge) p;
        sinkNodeName = proto.getSinkNodeName();
        if (proto.hasCondition()) {
            condition = EdgeConditionModel.fromProto(proto.getCondition());
            condition.edge = this;
        }
    }

    public static EdgeModel fromProto(Edge proto) {
        EdgeModel out = new EdgeModel();
        out.initFrom(proto);
        return out;
    }

    // Implementation details below

    public ThreadSpecModel threadSpecModel;

    private NodeModel sinkNode;

    public NodeModel getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpecModel.nodes.get(sinkNodeName);
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

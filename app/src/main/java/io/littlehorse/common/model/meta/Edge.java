package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.EdgePbOrBuilder;
import java.util.HashSet;
import java.util.Set;

public class Edge extends LHSerializable<EdgePbOrBuilder> {

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

    public void initFrom(MessageOrBuilder p) {
        EdgePbOrBuilder proto = (EdgePbOrBuilder) p;
        sinkNodeName = proto.getSinkNodeName();
        if (proto.hasCondition()) {
            condition = EdgeCondition.fromProto(proto.getConditionOrBuilder());
            condition.edge = this;
        }
    }

    public static Edge fromProto(EdgePbOrBuilder proto) {
        Edge out = new Edge();
        out.initFrom(proto);
        return out;
    }

    // Implementation details below
    @JsonIgnore
    public ThreadSpec threadSpec;

    @JsonIgnore
    private Node sinkNode;

    @JsonIgnore
    public Node getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpec.nodes.get(sinkNodeName);
        }
        return sinkNode;
    }

    @JsonIgnore
    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();

        if (condition != null) {
            out.addAll(condition.getRequiredVariableNames());
        }

        return out;
    }
}

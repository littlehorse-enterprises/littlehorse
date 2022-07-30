package io.littlehorse.common.model.meta;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.proto.NodeTypePb;

public class Node extends LHSerializable<NodePbOrBuilder> {
    public String taskDefName;
    public NodeTypePb type;

    public Class<NodePb> getProtoBaseClass() {
        return NodePb.class;
    }

    public NodePb.Builder toProto() {
        NodePb.Builder out = NodePb.newBuilder()
            .setTaskDefName(taskDefName)
            .setType(type);

        return out;
    }

    public void initFrom(NodePbOrBuilder proto) {
        taskDefName = proto.getTaskDefName();
        type = proto.getType();

        for (EdgePb e: proto.getOutgoingEdgesList()) {
            outgoingEdges.add(Edge.fromProto(e));
        }
    }

    public static Node fromProto(NodePbOrBuilder proto) {
        Node n = new Node();
        n.initFrom(proto);
        return n;
    }

    // Implementation details below

    public Node() {
        outgoingEdges = new HashSet<>();
    }

    @JsonIgnore public Set<Edge> outgoingEdges;
    @JsonIgnore public String name;
    @JsonIgnore public ThreadSpec threadSpec;
}

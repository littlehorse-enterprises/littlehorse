package io.littlehorse.common.model.meta;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.proto.NodeTypePb;

public class Node {
    public String taskDefName;
    public NodeTypePb type;

    public NodePb.Builder toProtoBuilder() {
        NodePb.Builder out = NodePb.newBuilder()
            .setTaskDefName(taskDefName)
            .setType(type);

        return out;
    }

    public static Node fromProto(NodePbOrBuilder proto) {
        Node n = new Node();
        n.taskDefName = proto.getTaskDefName();
        n.type = proto.getType();

        for (EdgePb e: proto.getOutgoingEdgesList()) {
            n.outgoingEdges.add(Edge.fromProto(e));
        }
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

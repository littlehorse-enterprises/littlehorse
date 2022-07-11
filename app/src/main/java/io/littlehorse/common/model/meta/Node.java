package io.littlehorse.common.model.meta;

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
        return n;
    }
}

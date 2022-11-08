package io.littlehorse.common.model.meta;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.VariableMutationPb.NodeOutputSourcePb;
import io.littlehorse.common.proto.VariableMutationPb.NodeOutputSourcePbOrBuilder;

public class NodeOutputSource extends LHSerializable<NodeOutputSourcePb> {

    String jsonPath;

    public Class<NodeOutputSourcePb> getProtoBaseClass() {
        return NodeOutputSourcePb.class;
    }

    public NodeOutputSourcePb.Builder toProto() {
        NodeOutputSourcePb.Builder out = NodeOutputSourcePb.newBuilder();
        if (jsonPath != null) out.setJsonpath(this.jsonPath);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        NodeOutputSourcePbOrBuilder p = (NodeOutputSourcePbOrBuilder) proto;
        if (p.hasJsonpath()) {
            this.jsonPath = p.getJsonpath();
        }
    }

    public static NodeOutputSource fromProto(NodeOutputSourcePbOrBuilder p) {
        NodeOutputSource out = new NodeOutputSource();
        out.initFrom(p);
        return out;
    }
}

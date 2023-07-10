package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableMutationPb.NodeOutputSourcePb;

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

    public void initFrom(Message proto) {
        NodeOutputSourcePb p = (NodeOutputSourcePb) proto;
        if (p.hasJsonpath()) {
            this.jsonPath = p.getJsonpath();
        }
    }

    public static NodeOutputSource fromProto(NodeOutputSourcePb p) {
        NodeOutputSource out = new NodeOutputSource();
        out.initFrom(p);
        return out;
    }
}

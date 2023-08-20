package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource;

public class NodeOutputSourceModel extends LHSerializable<NodeOutputSource> {

    String jsonPath;

    public Class<NodeOutputSource> getProtoBaseClass() {
        return NodeOutputSource.class;
    }

    public NodeOutputSource.Builder toProto() {
        NodeOutputSource.Builder out = NodeOutputSource.newBuilder();
        if (jsonPath != null)
            out.setJsonpath(this.jsonPath);
        return out;
    }

    public void initFrom(Message proto) {
        NodeOutputSource p = (NodeOutputSource) proto;
        if (p.hasJsonpath()) {
            this.jsonPath = p.getJsonpath();
        }
    }

    public static NodeOutputSourceModel fromProto(NodeOutputSource p) {
        NodeOutputSourceModel out = new NodeOutputSourceModel();
        out.initFrom(p);
        return out;
    }
}

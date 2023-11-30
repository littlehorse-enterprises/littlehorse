package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class NodeOutputSourceModel extends LHSerializable<NodeOutputSource> {

    String jsonPath;

    public Class<NodeOutputSource> getProtoBaseClass() {
        return NodeOutputSource.class;
    }

    public NodeOutputSource.Builder toProto() {
        NodeOutputSource.Builder out = NodeOutputSource.newBuilder();
        if (jsonPath != null) out.setJsonpath(this.jsonPath);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeOutputSource p = (NodeOutputSource) proto;
        if (p.hasJsonpath()) {
            this.jsonPath = p.getJsonpath();
        }
    }

    public static NodeOutputSourceModel fromProto(NodeOutputSource p, ExecutionContext context) {
        NodeOutputSourceModel out = new NodeOutputSourceModel();
        out.initFrom(p, context);
        return out;
    }
}

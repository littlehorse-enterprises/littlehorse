package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource.PathCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class NodeOutputSourceModel extends LHSerializable<NodeOutputSource> {

    private PathCase pathCase;
    private String jsonPath;
    private LHPathModel lhPath;

    public Class<NodeOutputSource> getProtoBaseClass() {
        return NodeOutputSource.class;
    }

    public NodeOutputSource.Builder toProto() {
        NodeOutputSource.Builder out = NodeOutputSource.newBuilder();

        switch (pathCase) {
            case JSONPATH:
                out.setJsonpath(this.jsonPath);
                break;
            case LH_PATH:
                out.setLhPath(lhPath.toProto());
                break;
            case PATH_NOT_SET:
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        NodeOutputSource p = (NodeOutputSource) proto;

        pathCase = p.getPathCase();

        switch (pathCase) {
            case JSONPATH:
                this.jsonPath = p.getJsonpath();
                break;
            case LH_PATH:
                this.lhPath = LHPathModel.fromProto(p.getLhPath(), context);
                break;
            case PATH_NOT_SET:
        }
    }

    public static NodeOutputSourceModel fromProto(NodeOutputSource p, ExecutionContext context) {
        NodeOutputSourceModel out = new NodeOutputSourceModel();
        out.initFrom(p, context);
        return out;
    }
}

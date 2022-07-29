package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.EdgePbOrBuilder;

public class Edge {
    public String sinkNodeName;

    public EdgePb.Builder toProtoBuilder() {
        EdgePb.Builder out = EdgePb.newBuilder()
            .setSinkNodeName(sinkNodeName);

        return out;
    }

    public static Edge fromProto(EdgePbOrBuilder proto) {
        Edge out = new Edge();
        out.sinkNodeName = proto.getSinkNodeName();
        return out;
    }

    // Implementation details below
    @JsonIgnore public ThreadSpec threadSpec;

    @JsonIgnore private Node sinkNode;

    @JsonIgnore public Node getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpec.nodes.get(sinkNodeName);
        }
        return sinkNode;
    }
}

package io.littlehorse.common.model.meta;

import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.EdgePbOrBuilder;

public class Edge {
    public String sourceNodeName;
    public String sinkNodeName;

    public EdgePb.Builder toProtoBuilder() {
        EdgePb.Builder out = EdgePb.newBuilder()
            .setSourceNodeName(sourceNodeName)
            .setSinkNodeName(sinkNodeName);

        return out;
    }

    public static Edge fromProto(EdgePbOrBuilder proto) {
        Edge out = new Edge();
        out.sourceNodeName = proto.getSourceNodeName();
        out.sinkNodeName = proto.getSinkNodeName();
        return out;
    }
}

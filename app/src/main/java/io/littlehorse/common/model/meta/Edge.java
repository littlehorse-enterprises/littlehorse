package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Implementation details below
    @JsonIgnore public ThreadSpec threadSpec;

    @JsonIgnore private Node sourceNode;
    @JsonIgnore private Node sinkNode;

    @JsonIgnore public Node getSourceNode() {
        if (sourceNode == null) {
            sourceNode = threadSpec.nodes.get(sourceNodeName);
        }
        return sourceNode;
    }

    @JsonIgnore public Node getSinkNode() {
        if (sinkNode == null) {
            sinkNode = threadSpec.nodes.get(sinkNodeName);
        }
        return sinkNode;
    }
}

package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.EdgePbOrBuilder;

public class Edge extends LHSerializable<EdgePbOrBuilder> {
    public String sinkNodeName;

    public Class<EdgePb> getProtoBaseClass() {
        return EdgePb.class;
    }

    public EdgePb.Builder toProto() {
        EdgePb.Builder out = EdgePb.newBuilder()
            .setSinkNodeName(sinkNodeName);

        return out;
    }

    public void initFrom(EdgePbOrBuilder proto) {
        sinkNodeName = proto.getSinkNodeName();
    }

    public static Edge fromProto(EdgePbOrBuilder proto) {
        Edge out = new Edge();
        out.initFrom(proto);
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

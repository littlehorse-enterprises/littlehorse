package io.littlehorse.common.model.meta;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodeTypePb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.ThreadSpecPbOrBuilder;

public class ThreadSpec {
    public String name;

    public Map<String, Node> nodes;

    public ThreadSpec() {
        nodes = new HashMap<>();
    }

    // Below is Serde
    public ThreadSpecPb.Builder toProtoBuilder() {
        ThreadSpecPb.Builder out = ThreadSpecPb.newBuilder();

        for (Map.Entry<String, Node> e: nodes.entrySet()) {
            out.putNodes(e.getKey(), e.getValue().toProtoBuilder().build());
        }
        return out;
    }

    public static ThreadSpec fromProto(ThreadSpecPbOrBuilder proto) {
        ThreadSpec out = new ThreadSpec();

        for (Map.Entry<String, NodePb> p: proto.getNodesMap().entrySet()) {
            Node n = Node.fromProto(p.getValue());
            n.threadSpec = out;
            n.name = p.getKey();
            out.nodes.put(p.getKey(), n);
            if (n.type == NodeTypePb.ENTRYPOINT) {
                out.entrypointNodeName = n.name;
            }
        }

        return out;
    }

    // Below is Implementation
    @JsonIgnore public String entrypointNodeName;
    @JsonIgnore public WfSpec wfSpec;
}

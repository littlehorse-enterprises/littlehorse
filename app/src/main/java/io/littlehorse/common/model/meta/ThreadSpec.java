package io.littlehorse.common.model.meta;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodeTypePb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.ThreadSpecPbOrBuilder;

public class ThreadSpec extends LHSerializable<ThreadSpecPbOrBuilder> {
    public String name;

    public Map<String, Node> nodes;

    public ThreadSpec() {
        nodes = new HashMap<>();
    }

    public Class<ThreadSpecPb> getProtoBaseClass() {
        return ThreadSpecPb.class;
    }

    // Below is Serde
    public ThreadSpecPb.Builder toProto() {
        ThreadSpecPb.Builder out = ThreadSpecPb.newBuilder();

        for (Map.Entry<String, Node> e: nodes.entrySet()) {
            out.putNodes(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(ThreadSpecPbOrBuilder proto) {
        for (Map.Entry<String, NodePb> p: proto.getNodesMap().entrySet()) {
            Node n = Node.fromProto(p.getValue());
            n.threadSpec = this;
            n.name = p.getKey();
            this.nodes.put(p.getKey(), n);
            if (n.type == NodeTypePb.ENTRYPOINT) {
                this.entrypointNodeName = n.name;
            }
        }
    }

    public static ThreadSpec fromProto(ThreadSpecPbOrBuilder proto) {
        ThreadSpec out = new ThreadSpec();
        out.initFrom(proto);
        return out;
    }

    // Below is Implementation
    @JsonIgnore public String entrypointNodeName;
    @JsonIgnore public WfSpec wfSpec;
}

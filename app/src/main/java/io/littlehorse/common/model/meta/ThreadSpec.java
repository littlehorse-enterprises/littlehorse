package io.littlehorse.common.model.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodeTypePb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.ThreadSpecPbOrBuilder;

public class ThreadSpec {
    public String name;

    public Map<String, Node> nodes;
    public List<Edge> edges;

    public ThreadSpec() {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
    }

    // Below is Serde
    public ThreadSpecPb.Builder toProtoBuilder() {
        ThreadSpecPb.Builder out = ThreadSpecPb.newBuilder();

        for (Map.Entry<String, Node> e: nodes.entrySet()) {
            out.putNodes(e.getKey(), e.getValue().toProtoBuilder().build());
        }
        for (Edge e : edges) {
            out.addEdges(e.toProtoBuilder());
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
        for (EdgePb e: proto.getEdgesList()) {
            Edge edge = Edge.fromProto(e);
            edge.threadSpec = out;
            out.edges.add(edge);
            out.nodes.get(edge.sourceNodeName).outgoingEdges.add(edge);
            out.nodes.get(edge.sinkNodeName).incomingEdges.add(edge);
        }

        return out;
    }

    // Below is Implementation
    @JsonIgnore public String entrypointNodeName;
    @JsonIgnore public WfSpec wfSpec;
}

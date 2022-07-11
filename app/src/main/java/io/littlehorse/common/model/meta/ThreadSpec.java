package io.littlehorse.common.model.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
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
            out.nodes.put(p.getKey(), Node.fromProto(p.getValue()));
        }
        for (EdgePb e: proto.getEdgesList()) {
            out.edges.add(Edge.fromProto(e));
        }

        return out;
    }
}

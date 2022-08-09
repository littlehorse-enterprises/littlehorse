package io.littlehorse.common.model.meta;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.wfspec.EdgePb;
import io.littlehorse.common.proto.wfspec.NodePb;
import io.littlehorse.common.proto.wfspec.NodePbOrBuilder;
import io.littlehorse.common.proto.wfspec.NodeTypePb;

public class Node extends LHSerializable<NodePbOrBuilder> {
    public String taskDefName;
    public NodeTypePb type;

    @JsonIgnore public Class<NodePb> getProtoBaseClass() {
        return NodePb.class;
    }

    @JsonIgnore public NodePb.Builder toProto() {
        NodePb.Builder out = NodePb.newBuilder()
            .setType(type);
        if (taskDefName != null) out.setTaskDefName(taskDefName);

        for (Edge o: outgoingEdges) {
            out.addOutgoingEdges(o.toProto());
        }

        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        NodePbOrBuilder proto = (NodePbOrBuilder) p;
        if (proto.hasTaskDefName()) taskDefName = proto.getTaskDefName();
        type = proto.getType();

        for (EdgePb epb: proto.getOutgoingEdgesList()) {
            Edge edge = Edge.fromProto(epb);
            edge.threadSpec = threadSpec;
            outgoingEdges.add(edge);
        }
    }

    public static Node fromProto(NodePbOrBuilder proto) {
        Node n = new Node();
        n.initFrom(proto);
        return n;
    }

    // Implementation details below

    public Node() {
        outgoingEdges = new ArrayList<>();
    }

    public List<Edge> outgoingEdges;
    public String name;
    @JsonIgnore public ThreadSpec threadSpec;

    public void validate(LHDatabaseClient client)
    throws LHValidationError, LHConnectionError {
        for (Edge e: outgoingEdges) {
            Node sink = threadSpec.nodes.get(e.sinkNodeName);
            if (sink == null) {
                throw new LHValidationError(
                    null,
                    "Node " + name + " on thread " + threadSpec.name + " has edge"
                    + " referring to nonexistent node " + e.sinkNodeName
                );
            }
            if (sink.type == NodeTypePb.ENTRYPOINT) {
                throw new LHValidationError(
                    null,
                    "Thread " + threadSpec.name + " has entrypoint node with "
                    + " incoming edge from node " + name + "."
                );
            }
        }

        if (type == NodeTypePb.TASK) {
            validateTask(client);
        }
    }

    private void validateTask(LHDatabaseClient client)
    throws LHConnectionError, LHValidationError {
        TaskDef task = client.getTaskDef(taskDefName);
        if (task == null) {
            throw new LHValidationError(
                null,
                "Node " + name + " on thread " + threadSpec.name + " refers to "
                + "nonexistent TaskDef " + taskDefName
            );
        }
    }
}

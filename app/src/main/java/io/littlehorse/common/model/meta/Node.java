package io.littlehorse.common.model.meta;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.proto.NodeTypePb;

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

        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        NodePbOrBuilder proto = (NodePbOrBuilder) p;
        if (proto.hasTaskDefName()) taskDefName = proto.getTaskDefName();
        type = proto.getType();

        for (EdgePb e: proto.getOutgoingEdgesList()) {
            outgoingEdges.add(Edge.fromProto(e));
        }
    }

    public static Node fromProto(NodePbOrBuilder proto) {
        Node n = new Node();
        n.initFrom(proto);
        return n;
    }

    // Implementation details below

    public Node() {
        outgoingEdges = new HashSet<>();
    }

    @JsonIgnore public Set<Edge> outgoingEdges;
    @JsonIgnore public String name;
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

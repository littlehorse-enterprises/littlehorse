package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.subnode.EntrypointNode;
import io.littlehorse.common.model.meta.subnode.ExitNode;
import io.littlehorse.common.model.meta.subnode.ExternalEventNode;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.proto.VariableMutationPb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node extends LHSerializable<NodePbOrBuilder> {

    public NodeCase type;
    public TaskNode taskNode;
    public ExternalEventNode externalEventNode;
    public EntrypointNode entrypointNode;
    public ExitNode exitNode;
    public List<VariableMutation> variableMutations;
    public OutputSchema outputSchema;

    @JsonIgnore
    public Class<NodePb> getProtoBaseClass() {
        return NodePb.class;
    }

    @JsonIgnore
    public NodePb.Builder toProto() {
        NodePb.Builder out = NodePb
            .newBuilder()
            .setOutputSchema(outputSchema.toProto());

        for (Edge o : outgoingEdges) {
            out.addOutgoingEdges(o.toProto());
        }

        for (VariableMutation v : variableMutations) {
            out.addVariableMutations(v.toProto());
        }

        switch (type) {
            case TASK:
                out.setTask(taskNode.toProto());
                break;
            case ENTRYPOINT:
                out.setEntrypoint(entrypointNode.toProto());
                break;
            case EXIT:
                out.setExit(exitNode.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventNode.toProto());
                break;
            case NODE_NOT_SET:
            // nothing to do.
        }

        return out;
    }

    public void initFrom(MessageOrBuilder p) throws LHSerdeError {
        NodePbOrBuilder proto = (NodePbOrBuilder) p;
        type = proto.getNodeCase();
        outputSchema = OutputSchema.fromProto(proto.getOutputSchemaOrBuilder());

        for (EdgePb epb : proto.getOutgoingEdgesList()) {
            Edge edge = Edge.fromProto(epb);
            edge.threadSpec = threadSpec;
            outgoingEdges.add(edge);
        }

        for (VariableMutationPb vmpb : proto.getVariableMutationsList()) {
            VariableMutation vm = new VariableMutation();
            vm.initFrom(vmpb);
            variableMutations.add(vm);
        }

        switch (type) {
            case TASK:
                taskNode = new TaskNode();
                taskNode.initFrom(proto.getTask());
                break;
            case ENTRYPOINT:
                entrypointNode = new EntrypointNode();
                entrypointNode.initFrom(proto.getEntrypoint());
                break;
            case EXIT:
                exitNode = new ExitNode();
                exitNode.initFrom(proto.getExit());
                break;
            case EXTERNAL_EVENT:
                externalEventNode = new ExternalEventNode();
                externalEventNode.initFrom(proto.getExternalEvent());
                break;
            case NODE_NOT_SET:
            default:
                throw new LHSerdeError(
                    null,
                    "Node " + name + " on thread " + threadSpec.name + " is unset!"
                );
        }
        getSubNode().setNode(this);
    }

    // Implementation details below

    public Node() {
        outgoingEdges = new ArrayList<>();
        variableMutations = new ArrayList<>();
        outputSchema = new OutputSchema();
    }

    public List<Edge> outgoingEdges;
    public String name;

    @JsonIgnore
    public ThreadSpec threadSpec;

    @JsonIgnore
    public Set<String> neededVariableNames() {
        Set<String> out = new HashSet<>();

        for (VariableMutation mut : variableMutations) {
            out.add(mut.lhsName);
            if (mut.rhsSourceVariable != null) {
                if (mut.rhsSourceVariable.rhsVariableName != null) {
                    out.add(mut.rhsSourceVariable.rhsVariableName);
                }
            }
        }

        out.addAll(getSubNode().getNeededVariableNames());

        return out;
    }

    public void validate(LHGlobalMetaStores client, LHConfig config)
        throws LHValidationError {
        for (Edge e : outgoingEdges) {
            Node sink = threadSpec.nodes.get(e.sinkNodeName);
            if (sink == null) {
                throw new LHValidationError(
                    null,
                    String.format(
                        "Node %s on thread %s has edge referring to missing node %s!",
                        name,
                        threadSpec.name,
                        e.sinkNodeName
                    )
                );
            }
            if (sink.type == NodeCase.ENTRYPOINT) {
                throw new LHValidationError(
                    null,
                    String.format(
                        "Thread %s has entrypoint node with incoming edge from node %s.",
                        threadSpec.name,
                        name
                    )
                );
            }
            if (e.condition != null) {
                e.condition.validate();
            }
        }

        if (!outgoingEdges.isEmpty()) {
            Edge last = outgoingEdges.get(outgoingEdges.size() - 1);
            if (last.condition != null) {
                throw new LHValidationError(
                    null,
                    "Node " +
                    name +
                    " on thread " +
                    threadSpec.name +
                    " last edge has non-null condition!"
                );
            }
        }

        try {
            getSubNode().validate(client, config);
        } catch (LHValidationError exn) {
            // Decorate the exception with contextual info
            exn.addPrefix("Thread " + threadSpec.name + " node " + name);
            throw exn;
        }
    }

    public SubNode<?> getSubNode() {
        if (type == NodeCase.TASK) {
            return taskNode;
        } else if (type == NodeCase.ENTRYPOINT) {
            return entrypointNode;
        } else if (type == NodeCase.EXIT) {
            return exitNode;
        } else if (type == NodeCase.EXTERNAL_EVENT) {
            return externalEventNode;
        } else {
            throw new RuntimeException("Unhandled node type " + type);
        }
    }

    /**
     * Returns the set of all thread variable names referred to by this
     * Node. Used internally for validation of the WfSpec.
     */
    @JsonIgnore
    public Set<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        for (VariableMutation mut : variableMutations) {
            out.addAll(mut.getRequiredVariableNames());
        }

        // TODO: When we add input variables, we need to add those...

        for (Edge edge : outgoingEdges) {
            out.addAll(edge.getRequiredVariableNames());
        }

        return out;
    }
}

package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.node.EntrypointNode;
import io.littlehorse.common.model.meta.node.ExitNode;
import io.littlehorse.common.model.meta.node.ExternalEventNode;
import io.littlehorse.common.model.meta.node.TaskNode;
import io.littlehorse.common.proto.EdgePb;
import io.littlehorse.common.proto.NodePb;
import io.littlehorse.common.proto.NodePb.NodeCase;
import io.littlehorse.common.proto.NodePbOrBuilder;
import io.littlehorse.common.proto.VariableAssignmentPb.SourceCase;
import io.littlehorse.common.proto.VariableMutationPb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

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

        if (type == NodeCase.TASK) {
            if (taskNode.timeoutSeconds.rhsSourceType == SourceCase.VARIABLE_NAME) {
                out.add(taskNode.timeoutSeconds.rhsVariableName);
            }
        }

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

        if (type == NodeCase.TASK) {
            validateTask(client, config);
        } else if (type == NodeCase.ENTRYPOINT) {
            validateEntrypoint(client, config);
        } else if (type == NodeCase.EXIT) {
            validateExit(client, config);
        } else if (type == NodeCase.EXTERNAL_EVENT) {
            validateExternalEvent(client, config);
        } else {
            throw new RuntimeException("Unhandled node type " + type);
        }
    }

    private void validateEntrypoint(LHGlobalMetaStores stores, LHConfig config) {
        outputSchema = new OutputSchema();
        outputSchema.outputType = VariableTypePb.VOID;
    }

    private void validateExit(LHGlobalMetaStores stores, LHConfig config) {
        outputSchema = new OutputSchema();
        outputSchema.outputType = VariableTypePb.VOID;
    }

    private void validateExternalEvent(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        ExternalEventDef eed = stores.getExternalEventDef(
            externalEventNode.externalEventDefName
        );

        if (eed == null) {
            throw new LHValidationError(
                null,
                "Node " +
                name +
                " on thread " +
                threadSpec.name +
                " refers to " +
                "nonexistent ExternalEventDef " +
                externalEventNode.externalEventDefName
            );
        }
    }

    private void validateTask(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        TaskDef taskDef = stores.getTaskDef(taskNode.taskDefName);
        if (taskDef == null) {
            throw new LHValidationError(
                null,
                "Node " +
                name +
                " on thread " +
                threadSpec.name +
                " refers to " +
                "nonexistent TaskDef " +
                taskNode.taskDefName
            );
        }
        if (taskNode.timeoutSeconds == null) {
            taskNode.timeoutSeconds = config.getDefaultTaskTimeout();
        }
        if (taskNode.retries < 0) {
            throw new LHValidationError(
                null,
                "Node " +
                name +
                " on thread " +
                threadSpec.name +
                "has negative " +
                "number of retries!"
            );
        }

        if (taskNode.timeoutSeconds.rhsSourceType == SourceCase.VARIABLE_NAME) {
            Pair<String, VariableDef> defPair = threadSpec.lookupVarDef(
                taskNode.timeoutSeconds.rhsVariableName
            );
            if (defPair == null) {
                throw new LHValidationError(
                    null,
                    "Timeout on node " +
                    name +
                    " refers to missing variable " +
                    taskNode.timeoutSeconds.rhsVariableName
                );
            }

            if (defPair.getValue().type != VariableTypePb.INT) {
                throw new LHValidationError(
                    null,
                    "Timeout on node " +
                    name +
                    " refers to non INT variable " +
                    taskNode.timeoutSeconds.rhsVariableName
                );
            }
        }

        // Now need to validate that all of the variables are provided.
        for (Map.Entry<String, VariableDef> e : taskDef.requiredVars.entrySet()) {
            VariableDef varDef = e.getValue();
            if (varDef.defaultValue == null) {
                // Then we NEED the value.
                if (!taskNode.variables.containsKey(e.getKey())) {
                    throw new LHValidationError(
                        null,
                        "TASK node " +
                        name +
                        " on thread " +
                        threadSpec.name +
                        " is missing required input variable " +
                        e.getKey()
                    );
                }
            }
            // TODO: May want to do some validation of types.
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

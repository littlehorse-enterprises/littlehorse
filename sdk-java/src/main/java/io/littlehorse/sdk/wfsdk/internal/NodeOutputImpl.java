package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableMutation;
import io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import java.util.Optional;

class NodeOutputImpl implements NodeOutput {

    public String nodeName;
    public WorkflowThreadImpl parent;
    public String jsonPath;

    private boolean addedInternalVariable;

    public NodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        this.nodeName = nodeName;
        this.parent = parent;
        this.parent.registerReturnedNodeOutput(this);
    }

    public NodeOutputImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same node!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.jsonPath = path;
        return out;
    }

    public NodeOutputImpl timeout(int timeoutSeconds) {
        parent.addTimeoutToExtEvt(this, timeoutSeconds);
        return this;
    }

    public VariableAssignment getInternalVariableAssignment() {
        String variableName = getInternalVarName();
        if (!addedInternalVariable) {
            addedInternalVariable = true;
            parent.addVariable(variableName, null /* TODO: how do we know the type? */);
            // Options:
            // 1. For each NodeType, we make a call to the API to get the output type
            // 2. We relax variable typing (dangerous)
        }
        // Now add the mutation on the parent.

        VariableAssignment.Builder result = VariableAssignment.newBuilder().setVariableName(variableName);
        if (jsonPath != null) {
            result.setJsonPath(jsonPath);
        }
        return result.build();
    }

    public Optional<VariableMutation> getMutationForInternalVariable() {
        if (!addedInternalVariable) return Optional.empty();

        return Optional.of(VariableMutation.newBuilder()
                .setLhsName(getInternalVarName())
                .setOperation(VariableMutationType.ASSIGN)
                .setNodeOutput(NodeOutputSource.newBuilder())
                .build());
    }

    private String getInternalVarName() {
        return nodeName + "-INTERNAL";
    }
}

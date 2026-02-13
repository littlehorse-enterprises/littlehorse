package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.io.Serializable;

class UserTaskOutputImpl extends NodeOutputImpl implements UserTaskOutput {

    private VariableAssignment notes;

    public UserTaskOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
    }

    public VariableAssignment getNotes() {
        return notes;
    }

    private void addNotes(Object notes) {
        VariableAssignment assn = parent.assignVariable(notes);

        // get the Node
        Node.Builder node = parent.getSpec().getNodesOrThrow(nodeName).toBuilder();
        node.getUserTaskBuilder().setNotes(assn);
        parent.getSpec().putNodes(nodeName, node.build());
    }

    private void addOnCancellationException(Object exceptionName) {
        VariableAssignment assn = parent.assignVariable(exceptionName);

        // get the Node
        Node.Builder node = parent.getSpec().getNodesOrThrow(nodeName).toBuilder();
        node.getUserTaskBuilder().setOnCancellationExceptionName(assn);
        parent.getSpec().putNodes(nodeName, node.build());
    }

    public UserTaskOutputImpl withNotes(String notes) {
        addNotes(notes);
        return this;
    }

    public UserTaskOutputImpl withNotes(WfRunVariable notes) {
        addNotes(notes);
        return this;
    }

    public UserTaskOutputImpl withNotes(LHFormatString notes) {
        addNotes(notes);
        return this;
    }

    @Override
    public UserTaskOutput withOnCancellationException(String exceptionName) {
        addOnCancellationException(exceptionName);
        return this;
    }

    @Override
    public UserTaskOutput withOnCancellationException(WfRunVariable exceptionName) {
        addOnCancellationException(exceptionName);
        return this;
    }

    @Override
    public LHExpression isLessThan(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isGreaterThan(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isEqualTo(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isNotEqualTo(Serializable other) {
        return null;
    }

    @Override
    public LHExpression doesContain(Serializable other) {
        return null;
    }

    @Override
    public LHExpression doesNotContain(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isIn(Serializable other) {
        return null;
    }

    @Override
    public LHExpression isNotIn(Serializable other) {
        return null;
    }
}

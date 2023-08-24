package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Node;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;

public class UserTaskOutputImpl extends NodeOutputImpl implements UserTaskOutput {

    private VariableAssignment notes;

    public UserTaskOutputImpl(String nodeName, ThreadBuilderImpl parent) {
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
}

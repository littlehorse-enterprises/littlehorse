package io.littlehorse.jlib.wfsdk.internal;

import io.littlehorse.jlib.common.proto.NodePb;
import io.littlehorse.jlib.common.proto.VariableAssignmentPb;
import io.littlehorse.jlib.wfsdk.LHFormatString;
import io.littlehorse.jlib.wfsdk.UserTaskOutput;
import io.littlehorse.jlib.wfsdk.WfRunVariable;

public class UserTaskOutputImpl extends NodeOutputImpl implements UserTaskOutput {

    private VariableAssignmentPb notes;

    public UserTaskOutputImpl(String nodeName, ThreadBuilderImpl parent) {
        super(nodeName, parent);
    }

    public VariableAssignmentPb getNotes() {
        return notes;
    }

    private void addNotes(Object notes) {
        VariableAssignmentPb assn = parent.assignVariable(notes);

        // get the Node
        NodePb.Builder node = parent.getSpec().getNodesOrThrow(nodeName).toBuilder();
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

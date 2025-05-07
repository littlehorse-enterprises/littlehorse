package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.IfElseBody;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import io.littlehorse.sdk.wfsdk.WorkflowIfStatement;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import lombok.Getter;

@Getter
public class WorkflowIfStatementImpl implements WorkflowIfStatement {

    private WorkflowThreadImpl parentWorkflowThread;
    private String firstNopNodeName;
    private String lastNopNodeName;
    private boolean wasElseExecuted;

    public WorkflowIfStatementImpl(
            WorkflowThread parentWorkflowThread, String firstNopNodeName, String lastNopNodeName) {
        this.parentWorkflowThread = (WorkflowThreadImpl) parentWorkflowThread;
        this.firstNopNodeName = firstNopNodeName;
        this.lastNopNodeName = lastNopNodeName;
        this.wasElseExecuted = false;
    }

    @Override
    public WorkflowIfStatement doElseIf(WorkflowCondition condition, IfElseBody body) {
        return this.parentWorkflowThread.doElseIf(this, condition, body);
    }

    @Override
    public void doElse(IfElseBody body) {
        if (this.wasElseExecuted) {
            throw new IllegalStateException("doElse() can only be called once per WorkflowIfStatement.");
        }

        this.wasElseExecuted = true;
        this.parentWorkflowThread.doElseIf(this, null, body);
    }
}

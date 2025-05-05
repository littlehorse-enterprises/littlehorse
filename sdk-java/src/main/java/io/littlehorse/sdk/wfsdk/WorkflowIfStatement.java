package io.littlehorse.sdk.wfsdk;

public interface WorkflowIfStatement {
    WorkflowIfStatement doElseIf(WorkflowCondition condition, IfElseBody body);

    void doElse(IfElseBody thread);
}

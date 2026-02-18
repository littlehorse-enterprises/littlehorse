package io.littlehorse.sdk.wfsdk;

/**
 * This interface allows you to chain 'else if' and 'else' statements on your If Statements.
 */
public interface WorkflowIfStatement {
    /**
     * After checking the previous condition(s) of the If Statement,
     * conditionally executes some workflow code.
     *
     * Equivalent to an 'else if' statement in programming.
     *
     * @param condition is the LHExpression to be satisfied.
     * @param body is the block of ThreadSpec code to be executed if the provided WorkflowCondition
     *             is satisfied.
     * @return Returns a WorkflowIfStatement object that allows you to chain
     *         {@link WorkflowIfStatement#doElseIf(WorkflowCondition, IfElseBody)} and
     *         {@link WorkflowIfStatement#doElse(IfElseBody)} method calls.
     */
    WorkflowIfStatement doElseIf(LHExpression condition, IfElseBody body);

    /**
     * After checking the ALL previous condition(s) of the If Statement,
     * executes some workflow code.
     *
     * Equivalent to an 'else' statement in programming.
     *
     * @param body is the block of ThreadSpec code to be executed if all previous conditions
     *             are not satisfied.
     */
    void doElse(IfElseBody body);
}

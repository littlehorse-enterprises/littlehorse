package io.littlehorse.tests;

public class LogicTestFailure extends Exception {

    WorkflowLogicTest testCase;
    String message;

    public LogicTestFailure(WorkflowLogicTest testCase, String message) {
        this.message = message;
        this.testCase = testCase;
    }

    @Override
    public String getMessage() {
        return ("Test case " + testCase.getWorkflowName() + " failed: " + message);
    }
}

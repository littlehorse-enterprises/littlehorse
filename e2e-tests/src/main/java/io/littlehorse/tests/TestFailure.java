package io.littlehorse.tests;

public class TestFailure extends Exception {

    Test testCase;
    String message;

    public TestFailure(Test testCase, String message) {
        this.message = message;
        this.testCase = testCase;
    }

    @Override
    public String getMessage() {
        return (
            "Test case " + testCase.getClass().getSimpleName() + " failed: " + message
        );
    }
}

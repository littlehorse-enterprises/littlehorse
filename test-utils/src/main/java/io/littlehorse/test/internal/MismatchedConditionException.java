package io.littlehorse.test.internal;

public class MismatchedConditionException extends LHTestException {
    private final Object evaluatedValue;
    private final Object expectedValue;
    private final int stepId;

    public MismatchedConditionException(Object expectedValue, Object evaluatedValue, int stepId) {
        this.evaluatedValue = evaluatedValue;
        this.expectedValue = expectedValue;
        this.stepId = stepId;
    }

    @Override
    public String toString() {
        return "Expected value %s but got %s on step %s".formatted(expectedValue, evaluatedValue, stepId);
    }
}

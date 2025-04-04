package io.littlehorse.test.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.WfRunId;

public class MismatchedConditionException extends LHTestException {
    private final Object evaluatedValue;
    private final Object expectedValue;
    private final int stepId;
    private final WfRunId wfRunId;

    public MismatchedConditionException(Object expectedValue, Object evaluatedValue, WfRunId wfRunId, int stepId) {
        this.evaluatedValue = evaluatedValue;
        this.expectedValue = expectedValue;
        this.wfRunId = wfRunId;
        this.stepId = stepId;
    }

    public Object getEvaluatedValue() {
        return evaluatedValue;
    }

    public Object getExpectedValue() {
        return expectedValue;
    }

    public int getStepId() {
        return stepId;
    }

    @Override
    public String toString() {
        return String.format(
                "Expected value %s but got %s on step %s, WfRun %s",
                expectedValue, evaluatedValue, stepId, LHLibUtil.wfRunIdToString(wfRunId));
    }
}

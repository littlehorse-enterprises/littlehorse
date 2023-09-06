package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.LHClientTestWrapper;
import java.util.function.Consumer;

public class VerifyVariableStep implements Step {

    private final String variableName;
    private final Consumer<VariableValue> variableMatcher;
    private final int threadRunNumber;

    public VerifyVariableStep(int threadRunNumber, String variableName, Consumer<VariableValue> variableMatcher) {
        this.variableName = variableName;
        this.variableMatcher = variableMatcher;
        this.threadRunNumber = threadRunNumber;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        String wfRunId = context.toString();
        VariableValue variableValue = lhClientWrapper.getVariableValue(wfRunId, threadRunNumber, variableName);
        variableMatcher.accept(variableValue);
    }
}

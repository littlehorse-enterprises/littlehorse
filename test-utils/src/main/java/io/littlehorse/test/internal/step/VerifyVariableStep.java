package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.function.Consumer;

public class VerifyVariableStep extends AbstractStep {

    private final String variableName;
    private final Consumer<VariableValue> variableMatcher;
    private final int threadRunNumber;

    public VerifyVariableStep(
            int threadRunNumber, String variableName, Consumer<VariableValue> variableMatcher, int id) {
        super(id);
        this.variableName = variableName;
        this.variableMatcher = variableMatcher;
        this.threadRunNumber = threadRunNumber;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        WfRunId wfRunId = context.getWfRunId();
        VariableId variableId = VariableId.newBuilder()
                .setName(variableName)
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .build();
        VariableValue variableValue = lhClient.getVariable(variableId).getValue();
        variableMatcher.accept(variableValue);
    }
}

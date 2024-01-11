package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.function.Consumer;

public class VerifyWfRunStep extends AbstractStep {

    private final Consumer<WfRun> wfRunMatcher;

    public VerifyWfRunStep(Consumer<WfRun> wfRunMatcher, int id) {
        super(id);
        this.wfRunMatcher = wfRunMatcher;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub lhClient) {
        WfRunId wfRunId = context.getWfRunId();
        WfRun wfRun = lhClient.getWfRun(wfRunId);
        wfRunMatcher.accept(wfRun);
    }
}

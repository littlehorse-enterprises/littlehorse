package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.test.LHClientTestWrapper;
import java.util.function.Consumer;

public class VerifyWfRunStep implements Step {

    private final Consumer<WfRun> wfRunMatcher;

    public VerifyWfRunStep(Consumer<WfRun> wfRunMatcher) {
        this.wfRunMatcher = wfRunMatcher;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        String wfRunId = context.toString();
        WfRun wfRun = lhClientWrapper.getWfRun(wfRunId);
        wfRunMatcher.accept(wfRun);
    }
}

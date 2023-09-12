package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.function.Consumer;

public class VerifyWfRunStep extends AbstractStep {

    private final Consumer<WfRun> wfRunMatcher;

    public VerifyWfRunStep(Consumer<WfRun> wfRunMatcher, int id) {
        super(id);
        this.wfRunMatcher = wfRunMatcher;
    }

    @Override
    public void tryExecute(Object context, LHPublicApiBlockingStub lhClient) {
        WfRunId wfRunId = WfRunId.newBuilder().setId(context.toString()).build();
        WfRun wfRun = lhClient.getWfRun(wfRunId);
        wfRunMatcher.accept(wfRun);
    }
}

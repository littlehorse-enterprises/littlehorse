package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.internal.TestContext;
import java.util.List;

public class WorkflowVerifier {

    private final LittleHorseBlockingStub lhClient;
    private final TestContext context;

    public WorkflowVerifier(TestContext context) {
        this.lhClient = context.getLhClient();
        this.context = context;
    }

    public WfRunVerifier prepareRun(Workflow workflow, Arg... args) {
        return new WfRunVerifier(context, workflow, List.of(args));
    }

    public LittleHorseBlockingStub getLhClient() {
        return lhClient;
    }
}

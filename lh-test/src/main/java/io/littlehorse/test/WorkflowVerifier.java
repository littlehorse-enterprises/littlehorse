package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.List;

public class WorkflowVerifier {

    private final LHPublicApiBlockingStub lhClient;

    public WorkflowVerifier(LHPublicApiBlockingStub lhClient) {
        this.lhClient = lhClient;
    }

    public WfRunVerifier prepareRun(Workflow workflow, Arg... args) {
        return new WfRunVerifier(lhClient, workflow, List.of(args));
    }
}

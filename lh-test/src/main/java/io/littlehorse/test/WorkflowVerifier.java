package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;

import java.util.List;

public class WorkflowVerifier {

    private final LHClient lhClient;
    private Workflow workflow;


    public WorkflowVerifier(LHClient lhClient) {
        this.lhClient = lhClient;
    }

    public WfRunVerifier prepare(Workflow workflow, Arg... args) {
        this.workflow = workflow;
        return new WfRunVerifier(lhClient, workflow, List.of(args));
    }


}

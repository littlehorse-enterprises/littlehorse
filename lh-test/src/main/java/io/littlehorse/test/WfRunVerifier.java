package io.littlehorse.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.common.proto.WfSpecPb;
import io.littlehorse.sdk.wfsdk.Workflow;

import java.util.List;
import java.util.UUID;

public class WfRunVerifier {

    private final LHClient lhClient;
    private final Workflow workflow;
    private final List<WorkflowExecutor.Step> steps;
    public WfRunVerifier(LHClient lhClient, Workflow workflow, List<WorkflowExecutor.Step> steps){
        this.lhClient = lhClient;
        this.workflow = workflow;
        this.steps = steps;
    }

    public void start(){
        try {
            for (PutTaskDefPb compileTaskDef : workflow.compileTaskDefs()) {
                lhClient.putTaskDef(compileTaskDef, true);
            }
            workflow.registerWfSpec(lhClient);
            WfSpecPb wfSpec = lhClient.getWfSpec(workflow.getName());
            String wfId = UUID.randomUUID().toString();
            lhClient.runWf(wfSpec.getName(), wfSpec.getVersion(), wfId);
            WfRunPb wfRun = lhClient.getWfRun(wfId);
            steps.forEach(step -> step.execute(wfRun));
        } catch (LHApiError e) {
            throw new RuntimeException(e);
        }
    }

}

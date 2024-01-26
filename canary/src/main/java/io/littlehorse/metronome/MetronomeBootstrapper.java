package io.littlehorse.metronome;

import io.littlehorse.common.app.Bootstrapper;
import io.littlehorse.sdk.wfsdk.Workflow;

public class MetronomeBootstrapper implements Bootstrapper {
    @Override
    public void initialize() {
        Workflow canaryWorkflow = Workflow.newWorkflow("canary-workflow", thread -> {
            thread.execute("my-task");
        });
        //        canaryWorkflow.registerWfSpec();
    }
}

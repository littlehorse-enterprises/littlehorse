package io.littlehorse.test.internal;

import io.littlehorse.sdk.wfsdk.Workflow;

public class DiscoveredWorkflowDefinition {

    private final String name;
    private final Workflow workflow;

    public DiscoveredWorkflowDefinition(String name, Workflow workflow) {
        this.name = name;
        this.workflow = workflow;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public String getName() {
        return name;
    }
}

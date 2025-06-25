package io.littlehorse.quickstart;

import java.util.List;
import java.util.Map;

import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class DemoDashboardWorkflow {
    private WfRunVariable searchableOptional;

    public void demoDashboardWf(WorkflowThread wf) {
        WfRunVariable searchableRequired = wf.declareStr("searchable-required").searchable().required();
        searchableOptional = wf.declareStr("searchable-optional").searchable();
        wf.declareInt("masked-required").masked().required();
        wf.declareInt("masked-optional").masked();
        wf.declareStr("public").asPublic();
        WfRunVariable optional = wf.declareBool("optional");

        // Task
        wf.execute("demo-dashboard-task", searchableRequired)
                .withRetries(3)
                .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder().setBaseIntervalMs(100).setMultiplier(2).setMaxDelayMs(600).build());

        // ExternalEvent
        NodeOutput demoDashboardExternalEvent = wf.waitForEvent("demo-dashboard-external-event")
                .timeout(60 * 60);

        // Assign output of ExternalEvent to a variable
        searchableOptional.assign(demoDashboardExternalEvent);

        // NOP and Sleep
        wf.doIf(optional.isEqualTo(true), ifBody -> ifBody.sleepSeconds(10));

        wf.assignUserTask("demo-dashboard-user-task", "demo-dashboard-user-id", null);

        // Launch a child thread and then sleep for 15 seconds
        wf.spawnThread(this::childThreadLogic, "demo-dashboard-child-thread",
                Map.of());

        WfRunVariable arrayForThreads = wf.declareJsonArr("array-for-threads").withDefault(List.of("a", "b", "c"));
        SpawnedThreads spawnedThreads = wf.spawnThreadForEach(arrayForThreads, "demo-dashboard-child-thread-for-each",
                this::childThreadLogic);
        wf.waitForThreads(spawnedThreads);
    }

    public void childThreadLogic(WorkflowThread wf) {
        // Child threads can take in input variables too! You must set them when
        // starting the child thread in the calll to spawnThread()
        wf.execute("demo-dashboard-task", searchableOptional);

        // Child threads can use the parents' variables
        wf.execute("demo-dashboard-task", searchableOptional);

        // Child threads can also mutate the parents' variables:
        wf.mutate(
                searchableOptional,
                VariableMutationType.ASSIGN,
                "This is the value of the parent variable set by the child.");

        // Child will sleep before finishing
        wf.sleepSeconds(5);
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("demo-dashboard", this::demoDashboardWf);
    }
}

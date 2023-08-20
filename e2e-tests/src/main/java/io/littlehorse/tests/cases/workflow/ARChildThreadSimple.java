package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class ARChildThreadSimple extends WorkflowLogicTest {

    public ARChildThreadSimple(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests that we can run a workflow with a simple SPAWN_THREAD.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable sharedVar = thread.addVariable("shared-var", VariableType.INT);

            SpawnedThread child = thread.spawnThread(
                    subthread -> {
                        NodeOutput echoOutput = subthread.execute("ar-echo", sharedVar);
                        subthread.mutate(sharedVar, VariableMutationType.ADD, echoOutput);
                        subthread.execute("ar-obiwan");
                    },
                    "first-thread",
                    null);

            thread.execute("ar-obiwan");
            thread.waitForThreads(child);

            thread.execute("ar-echo", sharedVar);
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ARSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client) throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("shared-var", 5));
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        // The parent's variable should be equal to the
        assertVarEqual(client, wfRunId, 0, "shared-var", 10);

        assertTaskOutputsMatch(client, wfRunId, 0, "hello there", 10);
        assertTaskOutputsMatch(client, wfRunId, 1, 5, "hello there");

        return Arrays.asList(wfRunId);
    }
}

class ARSimpleTask {

    @LHTaskMethod("ar-echo")
    public int echo(int toEcho) {
        return toEcho;
    }

    @LHTaskMethod("ar-obiwan")
    public String obiwan() {
        return "hello there";
    }
}

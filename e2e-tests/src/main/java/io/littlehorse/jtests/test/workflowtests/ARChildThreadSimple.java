package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.VariableMutationTypePb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.common.util.Arg;
import io.littlehorse.jlib.wfsdk.NodeOutput;
import io.littlehorse.jlib.wfsdk.SpawnedThread;
import io.littlehorse.jlib.wfsdk.WfRunVariable;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
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
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable sharedVar = thread.addVariable(
                    "shared-var",
                    VariableTypePb.INT
                );

                SpawnedThread child = thread.spawnThread(
                    subthread -> {
                        NodeOutput echoOutput = subthread.execute(
                            "ar-echo",
                            sharedVar
                        );
                        subthread.mutate(
                            sharedVar,
                            VariableMutationTypePb.ADD,
                            echoOutput
                        );
                        subthread.execute("ar-obiwan");
                    },
                    "first-thread",
                    null
                );

                thread.execute("ar-obiwan");
                thread.waitForThread(child);

                thread.execute("ar-echo", sharedVar);
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ARSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("shared-var", 5));
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);

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

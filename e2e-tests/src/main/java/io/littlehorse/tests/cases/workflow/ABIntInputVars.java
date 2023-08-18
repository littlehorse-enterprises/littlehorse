package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class ABIntInputVars extends WorkflowLogicTest {

    public ABIntInputVars(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Ensures that we can pass input variables to Task Runs properly.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
                getWorkflowName(),
                thread -> {
                    WfRunVariable myVar = thread.addVariable("my-var", VariableType.INT);
                    thread.execute("ab-double-it", myVar);
                    thread.execute("ab-subtract", 10, 8);
                });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ABDoubler());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
            throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("my-var", 5));
        Thread.sleep(500);

        assertStatus(client, wfRunId, LHStatus.COMPLETED);
        assertTaskOutput(client, wfRunId, 0, 1, new ABDoubler().doubleIt(5));
        assertTaskOutput(client, wfRunId, 0, 2, 10 - 8);

        return Arrays.asList(wfRunId);
    }
}

class ABDoubler {

    @LHTaskMethod("ab-double-it")
    public int doubleIt(int toDouble) {
        return toDouble * 2;
    }

    @LHTaskMethod("ab-subtract")
    public Long subtract(long first, Integer second) {
        return first - second;
    }
}

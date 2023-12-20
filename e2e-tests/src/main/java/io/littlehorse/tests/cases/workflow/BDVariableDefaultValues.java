package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BDVariableDefaultValues extends WorkflowLogicTest {

    public BDVariableDefaultValues(LittleHorseBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Simple test with workflow that executes two tasks sequentially";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable myVar = thread.addVariable("my-var", 123);
            thread.execute("bd-the-task", myVar);
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new BDSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String withVals = runWf(client, Arg.of("my-var", 321));
        String withDefault = runWf(client);
        Thread.sleep(200);
        assertStatus(client, withVals, LHStatus.COMPLETED);
        assertStatus(client, withDefault, LHStatus.COMPLETED);
        assertTaskOutput(client, withVals, 0, 1, 321);
        assertTaskOutput(client, withDefault, 0, 1, 123);
        return Arrays.asList(withVals, withDefault);
    }
}

class BDSimpleTask {

    @LHTaskMethod("bd-the-task")
    public int obiWan(int input) {
        return input;
    }
}

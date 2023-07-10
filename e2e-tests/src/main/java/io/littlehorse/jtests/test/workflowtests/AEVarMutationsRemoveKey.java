package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AEVarMutationsRemoveKey extends WorkflowLogicTest {

    public AEVarMutationsRemoveKey(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Ensures that we can remove a key from a JSON_OBJ variable.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable myObj = thread.addVariable(
                    "my-obj",
                    VariableTypePb.JSON_OBJ
                );

                thread.execute("ae-simple");
                thread.mutate(myObj, VariableMutationTypePb.REMOVE_KEY, "foo");
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AESimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfWithFoo = runWf(
            client,
            Arg.of("my-obj", Map.of("foo", "bar", "baz", 2))
        );
        String wfWithoutFoo = runWf(client, Arg.of("my-obj", Map.of("baz", 2)));

        Thread.sleep(500);

        assertStatus(client, wfWithFoo, LHStatusPb.COMPLETED);
        assertStatus(client, wfWithoutFoo, LHStatusPb.COMPLETED);

        Map<?, ?> removed = getVarAsObj(client, wfWithFoo, 0, "my-obj", Map.class);
        if (removed.containsKey("foo")) {
            throw new LogicTestFailure(
                this,
                "failed to remove the key 'foo' from myObj"
            );
        }

        if (((Integer) removed.get("baz")) != 2) {
            throw new LogicTestFailure(this, "myObj got corrupted");
        }

        removed = getVarAsObj(client, wfWithoutFoo, 0, "my-obj", Map.class);
        if (((Integer) removed.get("baz")) != 2) {
            throw new LogicTestFailure(this, "myObj got corrupted");
        }

        return Arrays.asList(wfWithFoo, wfWithoutFoo);
    }
}

class AESimpleTask {

    @LHTaskMethod("ae-simple")
    public String simpleTask() {
        return "hello there";
    }
}

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
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;

public class ADVarMutationsNumbers extends WorkflowLogicTest {

    public ADVarMutationsNumbers(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests various Variable Mutations with INT and DOUBLE, including " +
            "ADD, SUBTRACT, DIVIDE, and ASSIGN."
        );
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable myInt = thread.addVariable(
                    "my-int",
                    VariableTypePb.INT
                );

                WfRunVariable myDouble = thread.addVariable(
                    "my-double",
                    VariableTypePb.DOUBLE
                );

                WfRunVariable myOtherInt = thread.addVariable(
                    "my-other-int",
                    VariableTypePb.INT
                );

                NodeOutput output = thread.execute("ad-simple");
                thread.mutate(myInt, VariableMutationTypePb.ADD, output);
                thread.mutate(myInt, VariableMutationTypePb.SUBTRACT, 2);

                // ensure that we can cast from double to int, and that the
                // original type is respected
                thread.mutate(myOtherInt, VariableMutationTypePb.ASSIGN, myDouble);

                // Do some math, and divide by zero to show that failures work
                thread.mutate(myOtherInt, VariableMutationTypePb.DIVIDE, myInt);
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ADSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String happyWf = runWf(
            client,
            Arg.of("my-int", 5),
            Arg.of("my-double", 24.2)
        );

        // this should fail with divide by zero
        String sadWf = runWf(client, Arg.of("my-int", -8), Arg.of("my-double", 10.0));
        Thread.sleep(500);
        assertStatus(client, happyWf, LHStatusPb.COMPLETED);

        assertVarEqual(client, happyWf, 0, "my-int", 13);
        // the my-double var isn't mutated
        assertVarEqual(client, happyWf, 0, "my-double", 24.2);
        assertVarEqual(client, happyWf, 0, "my-other-int", (int) (24.2 / 13));

        // Should fail due to division by zero
        assertStatus(client, sadWf, LHStatusPb.ERROR);

        // Since the mutations failed, all should be rolled back.
        assertVarEqual(client, sadWf, 0, "my-int", -8);
        assertVarEqual(client, sadWf, 0, "my-double", 10.0);

        return Arrays.asList(happyWf, sadWf);
    }
}

class ADSimpleTask {

    @LHTaskMethod("ad-simple")
    public Integer obiWan() {
        return 10;
    }
}

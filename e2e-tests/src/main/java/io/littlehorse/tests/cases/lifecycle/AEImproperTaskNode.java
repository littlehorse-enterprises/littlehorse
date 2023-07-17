package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.FailurePb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.proto.WfRunPb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.tests.Test;
import java.util.Map;

public class AEImproperTaskNode extends Test {

    public static final String TASK_DEF_NAME = "ae-taskdef-improper-test";
    public static final String VALID_WF_SPEC_NAME = "ad-taskdef-deleted";
    private String failWfRun;
    private String successWfRun;
    private LHTaskWorker worker;

    public AEImproperTaskNode(LHClient client, LHWorkerConfig config) {
        super(client, config);
    }

    public String getDescription() {
        return """
    Tests behavior when a Task Node in a WfSpec doesn't have the right variable
    types to match with the TaskDef. Two cases:
    1. Primitive type mismatches. This causes VALIDATION_ERROR.
    2. JSON_OBJ with jsonpath, and the WfRun variable has a value which doesn't
       match with the TaskDef. The WfSpec should be created fine, but the WfRun
       should fail.
    """;
    }

    public void test() throws LHApiError, InterruptedException {
        worker =
            new LHTaskWorker(
                new AETaskNodeValidationWorker(),
                TASK_DEF_NAME,
                workerConfig
            );
        worker.registerTaskDef(true);

        // First, verify that we get an error when trying to create a WfRun that
        // has a definitive variable mismatch.
        LHApiError caught = null;
        try {
            new WorkflowImpl(
                "ae-invalid-asdf",
                thread -> {
                    thread.execute(TASK_DEF_NAME, "not-an-int");
                }
            )
                .registerWfSpec(client);
        } catch (LHApiError exn) {
            caught = exn;
        }
        if (
            caught == null ||
            caught.getCode() != LHResponseCodePb.VALIDATION_ERROR ||
            !caught.getMessage().contains("needs to be INT")
        ) {
            throw new RuntimeException("Should have got task input var type error!");
        }
        // check to ensure the WfSpec wasn't actually saved
        if (client.getWfSpec("ae-invalid-adf", null) != null) {
            throw new RuntimeException("shouldn't have saved invalid wfSpec!");
        }

        // Now deploy a valid WfSpec and cause it to crash (because JSON_OBJ vars
        // aren't strongly typed)
        new WorkflowImpl(
            VALID_WF_SPEC_NAME,
            thread -> {
                WfRunVariable var = thread.addVariable(
                    "var",
                    VariableTypePb.JSON_OBJ
                );
                // This ensures the RunWf request succeeds, since it's the first
                // node that actually gets executed.
                thread.execute(TASK_DEF_NAME, 12345);

                // This one either fails or succeeds.
                thread.execute(TASK_DEF_NAME, var.jsonPath("$.theField"));
            }
        )
            .registerWfSpec(client);

        Thread.sleep(200); // Wait for the data to propagate
        worker.start();

        this.failWfRun =
            client.runWf(
                VALID_WF_SPEC_NAME,
                null,
                null,
                Arg.of("var", Map.of("theField", "not-an-int"))
            );
        this.successWfRun =
            client.runWf(
                VALID_WF_SPEC_NAME,
                null,
                null,
                Arg.of("var", Map.of("theField", 1776))
            );
        Thread.sleep(120);

        WfRunPb wfRun = client.getWfRun(failWfRun);
        if (wfRun.getStatus() != LHStatusPb.ERROR) {
            throw new RuntimeException("Wf " + failWfRun + " hould have failed!");
        }
        NodeRunPb nodeRun = client.getNodeRun(failWfRun, 0, 2);
        FailurePb failure = nodeRun.getFailures(0);
        if (!failure.getFailureName().equals("VAR_SUB_ERROR")) {
            throw new RuntimeException("Expected VAR_SUB_ERROR!");
        }
        if (nodeRun.getTask().hasTaskRunId()) {
            throw new RuntimeException("The TaskRun shoudln't have been created.");
        }

        // Now verify the other one succeeded.
        if (client.getWfRun(successWfRun).getStatus() != LHStatusPb.COMPLETED) {
            throw new RuntimeException(
                "Wf " + successWfRun + " should have succeeded!"
            );
        }
    }

    public void cleanup() throws LHApiError {
        try {
            client.deleteWfRun(successWfRun);
            client.deleteWfRun(failWfRun);
            client.deleteTaskDef(TASK_DEF_NAME);
            client.deleteWfSpec(VALID_WF_SPEC_NAME, 0);
            worker.close();
        } catch (Exception exn) {}
    }
}

class AETaskNodeValidationWorker {

    @LHTaskMethod(AEImproperTaskNode.TASK_DEF_NAME)
    public String foo(int input) {
        return "hi";
    }
}

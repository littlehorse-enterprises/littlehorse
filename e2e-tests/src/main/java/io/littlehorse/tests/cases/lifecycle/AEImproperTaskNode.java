package io.littlehorse.tests.cases.lifecycle;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.tests.Test;
import java.io.IOException;
import java.util.Map;

public class AEImproperTaskNode extends Test {

    public static final String TASK_DEF_NAME = "ae-taskdef-improper-test";
    public static final String VALID_WF_SPEC_NAME = "ad-taskdef-deleted";
    private String failWfRun;
    private String successWfRun;
    private LHTaskWorker worker;

    public AEImproperTaskNode(LittleHorseBlockingStub client, LHConfig config) {
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

    public void test() throws InterruptedException, IOException {
        worker = new LHTaskWorker(new AETaskNodeValidationWorker(), TASK_DEF_NAME, workerConfig);
        worker.registerTaskDef(true);

        // First, verify that we get an error when trying to create a WfRun that
        // has a definitive variable mismatch.
        StatusRuntimeException caught = null;
        try {
            new WorkflowImpl("ae-invalid-asdf", thread -> {
                        thread.execute(TASK_DEF_NAME, "not-an-int");
                    })
                    .registerWfSpec(client);
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }
        if (caught == null
                || caught.getStatus().getCode() != Code.INVALID_ARGUMENT
                || !caught.getMessage().contains("needs to be INT")) {
            throw new RuntimeException("Should have got task input var type error!");
        }

        // check to ensure the WfSpec wasn't actually saved
        caught = null;
        try {
            client.getLatestWfSpec(GetLatestWfSpecRequest.newBuilder()
                    .setName("ae-invalid-adf")
                    .build());
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }
        if (caught == null) {
            throw new RuntimeException("shouldn't have saved invalid wfSpec!");
        }

        // Now deploy a valid WfSpec and cause it to crash (because JSON_OBJ vars
        // aren't strongly typed)
        new WorkflowImpl(VALID_WF_SPEC_NAME, thread -> {
                    WfRunVariable var = thread.addVariable("var", VariableType.JSON_OBJ);
                    // This ensures the RunWf request succeeds, since it's the first
                    // node that actually gets executed.
                    thread.execute(TASK_DEF_NAME, 12345);

                    // This one either fails or succeeds.
                    thread.execute(TASK_DEF_NAME, var.jsonPath("$.theField"));
                })
                .registerWfSpec(client);

        Thread.sleep(200); // Wait for the data to propagate
        worker.start();

        this.failWfRun = runWf(VALID_WF_SPEC_NAME, Arg.of("var", Map.of("theField", "not-an-int")));
        this.successWfRun = runWf(VALID_WF_SPEC_NAME, Arg.of("var", Map.of("theField", 1776)));
        Thread.sleep(120);

        WfRun wfRun = client.getWfRun(WfRunId.newBuilder().setId(failWfRun).build());
        if (wfRun.getStatus() != LHStatus.ERROR) {
            throw new RuntimeException("Wf " + failWfRun + " should have failed!");
        }
        NodeRun nodeRun = getNodeRun(failWfRun, 0, 2);
        Failure failure = nodeRun.getFailures(0);
        if (!failure.getFailureName().equals("VAR_SUB_ERROR")) {
            throw new RuntimeException("Expected VAR_SUB_ERROR!");
        }
        if (nodeRun.getTask().hasTaskRunId()) {
            throw new RuntimeException("The TaskRun shoudln't have been created.");
        }

        // Now verify the other one succeeded.
        if (client.getWfRun(WfRunId.newBuilder().setId(successWfRun).build()).getStatus() != LHStatus.COMPLETED) {
            throw new RuntimeException("Wf " + successWfRun + " should have succeeded!");
        }
    }

    public void cleanup() {
        client.deleteWfRun(DeleteWfRunRequest.newBuilder()
                .setId(WfRunId.newBuilder().setId(successWfRun))
                .build());
        client.deleteWfRun(DeleteWfRunRequest.newBuilder()
                .setId(WfRunId.newBuilder().setId(failWfRun))
                .build());
        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                .setId(WfSpecId.newBuilder().setName(VALID_WF_SPEC_NAME))
                .build());

        client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                .setId(TaskDefId.newBuilder().setName(TASK_DEF_NAME))
                .build());
        worker.close();
    }

    private String runWf(String wfSpecName, Arg... args) {
        RunWfRequest.Builder b = RunWfRequest.newBuilder().setWfSpecName(wfSpecName);

        for (Arg arg : args) {
            try {
                b.putVariables(arg.name, LHLibUtil.objToVarVal(arg.value));
            } catch (LHSerdeError exn) {
                throw new RuntimeException(exn);
            }
        }

        return client.runWf(b.build()).getId().getId();
    }

    private NodeRun getNodeRun(String wfRunId, int threadRunNumber, int position) {
        return client.getNodeRun(NodeRunId.newBuilder()
                .setWfRunId(LHLibUtil.wfRunId(wfRunId))
                .setThreadRunNumber(threadRunNumber)
                .setPosition(position)
                .build());
    }
}

class AETaskNodeValidationWorker {

    @LHTaskMethod(AEImproperTaskNode.TASK_DEF_NAME)
    public String foo(int input) {
        return "hi";
    }
}

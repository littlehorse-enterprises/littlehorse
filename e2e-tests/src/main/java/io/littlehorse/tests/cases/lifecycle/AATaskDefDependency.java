package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecResponse;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.tests.Test;
import java.util.UUID;

/*
 * Tests that TaskDef Dependencies are respected; i.e. if a TaskDef doesn't
 * exist then a WfSpec can't be created.
 */
public class AATaskDefDependency extends Test {

    private String taskDefName;
    private String wfSpecName;

    public AATaskDefDependency(LHClient client, LHWorkerConfig config) {
        super(client, config);
        taskDefName = "task-def-" + UUID.randomUUID().toString();
        wfSpecName = "wf-spec-" + UUID.randomUUID().toString();
    }

    public String getDescription() {
        return ("Ensures that a WfSpec cannot be deployed if a TaskDef is missing "
                + "and that we can still deploy it after the TaskDef is properly created.");
    }

    public void test() throws LHApiError {
        Workflow wf =
                new WorkflowImpl(
                        wfSpecName,
                        thread -> {
                            thread.execute(taskDefName);
                        });

        PutWfSpecRequest request = wf.compileWorkflow();
        PutWfSpecResponse reply = client.getGrpcClient().putWfSpec(request);

        if (reply.getCode() != LHResponseCode.VALIDATION_ERROR) {
            throw new RuntimeException("Was able to create wfSpec with missing taskdef!");
        }

        if (client.getWfSpec(wfSpecName, null) != null) {
            throw new RuntimeException("If WfSpec creation fails we should not pollute the API!");
        }

        // Now, create the TaskDef and see that we can actually deploy the WfSpec.
        client.putTaskDef(PutTaskDefRequest.newBuilder().setName(taskDefName).build());
        WfSpec result = client.putWfSpec(request);
        if (result.getVersion() != 0) {
            throw new RuntimeException("Somehow the version wasn't zero!");
        }
    }

    public void cleanup() throws LHApiError {
        client.deleteTaskDef(taskDefName);
        client.deleteWfSpec(wfSpecName, 0);
    }
}

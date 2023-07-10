package io.littlehorse.jtests.test.lifecycletests;

import io.littlehorse.jtests.test.Test;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.PutTaskDefPb;
import io.littlehorse.sdk.common.proto.PutWfSpecPb;
import io.littlehorse.sdk.common.proto.PutWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.WfSpecPb;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
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
        return (
            "Ensures that a WfSpec cannot be deployed if a TaskDef is missing " +
            "and that we can still deploy it after the TaskDef is properly created."
        );
    }

    public void test() throws LHApiError {
        Workflow wf = new WorkflowImpl(
            wfSpecName,
            thread -> {
                thread.execute(taskDefName);
            }
        );

        PutWfSpecPb request = wf.compileWorkflow();
        PutWfSpecReplyPb reply = client.getGrpcClient().putWfSpec(request);

        if (reply.getCode() != LHResponseCodePb.VALIDATION_ERROR) {
            throw new RuntimeException(
                "Was able to create wfSpec with missing taskdef!"
            );
        }

        if (client.getWfSpec(wfSpecName, null) != null) {
            throw new RuntimeException(
                "If WfSpec creation fails we should not pollute the API!"
            );
        }

        // Now, create the TaskDef and see that we can actually deploy the WfSpec.
        client.putTaskDef(PutTaskDefPb.newBuilder().setName(taskDefName).build());
        WfSpecPb result = client.putWfSpec(request);
        if (result.getVersion() != 0) {
            throw new RuntimeException("Somehow the version wasn't zero!");
        }
    }

    public void cleanup() throws LHApiError {
        client.deleteTaskDef(taskDefName);
        client.deleteWfSpec(wfSpecName, 0);
    }
}

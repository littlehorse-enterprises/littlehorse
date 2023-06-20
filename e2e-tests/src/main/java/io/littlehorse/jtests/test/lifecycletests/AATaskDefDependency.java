package io.littlehorse.jtests.test.lifecycletests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.PutTaskDefPb;
import io.littlehorse.jlib.common.proto.PutWfSpecPb;
import io.littlehorse.jlib.common.proto.PutWfSpecReplyPb;
import io.littlehorse.jlib.common.proto.WfSpecPb;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jtests.test.Test;
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

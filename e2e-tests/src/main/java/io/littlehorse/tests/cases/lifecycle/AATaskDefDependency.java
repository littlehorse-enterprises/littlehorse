package io.littlehorse.tests.cases.lifecycle;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
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

    public AATaskDefDependency(LHPublicApiBlockingStub client, LHConfig config) {
        super(client, config);
        taskDefName = "task-def-" + UUID.randomUUID().toString();
        wfSpecName = "wf-spec-" + UUID.randomUUID().toString();
    }

    public String getDescription() {
        return ("Ensures that a WfSpec cannot be deployed if a TaskDef is missing "
                + "and that we can still deploy it after the TaskDef is properly created.");
    }

    public void test() {
        Workflow wf = new WorkflowImpl(wfSpecName, thread -> {
            thread.execute(taskDefName);
        });

        PutWfSpecRequest request = wf.compileWorkflow();
        StatusRuntimeException caught = null;

        try {
            client.putWfSpec(request);
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }
        if (caught == null || caught.getStatus().getCode() != Code.INVALID_ARGUMENT) {
            throw new RuntimeException("Was able to create wfSpec with missing taskdef!");
        }

        caught = null;
        try {
            client.getLatestWfSpec(
                    GetLatestWfSpecRequest.newBuilder().setName(wfSpecName).build());
        } catch (StatusRuntimeException exn) {
            caught = exn;
        }

        if (caught == null || caught.getStatus().getCode() != Code.NOT_FOUND) {
            throw new RuntimeException("If WfSpec creation fails we should not pollute the API!");
        }

        // Now, create the TaskDef and see that we can actually deploy the WfSpec.
        client.putTaskDef(PutTaskDefRequest.newBuilder().setName(taskDefName).build());
        WfSpec result = client.putWfSpec(request);
        if (result.getId().getMajorVersion() != 0) {
            throw new RuntimeException("Somehow the version wasn't zero!");
        }
    }

    public void cleanup() {
        client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                .setId(TaskDefId.newBuilder().setName(taskDefName))
                .build());
        client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                .setId(WfSpecId.newBuilder()
                        .setName(wfSpecName)
                        .setMajorVersion(0)
                        .setRevision(0))
                .build());
    }
}

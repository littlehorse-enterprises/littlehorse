package io.littlehorse.tests;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import java.util.Map;

public abstract class UserTaskWorkflowTest extends WorkflowLogicTest {

    public UserTaskWorkflowTest(LHPublicApiBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public abstract Map<String, Object> getRequiredUserTaskForms();

    @Override
    public void cleanup() {
        for (Map.Entry<String, Object> pair : getRequiredUserTaskForms().entrySet()) {
            client.deleteUserTaskDef(DeleteUserTaskDefRequest.newBuilder()
                    .setId(UserTaskDefId.newBuilder().setName(pair.getKey()).build())
                    .build());
        }
        super.cleanup();
    }

    public void deploy(LHPublicApiBlockingStub client, LHConfig config) throws TestFailure {
        // Deploy the UserTaskDef's
        for (Map.Entry<String, Object> pair : getRequiredUserTaskForms().entrySet()) {
            UserTaskSchema schema = new UserTaskSchema(pair.getValue(), pair.getKey());
            try {
                client.putUserTaskDef(schema.compile());
            } catch (StatusRuntimeException exn) {
                if (exn.getStatus().getCode() == Code.ALREADY_EXISTS) {
                    // nothing to do
                } else {
                    throw exn;
                }
            }
        }
        super.deploy(client, config);
    }
}

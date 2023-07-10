package io.littlehorse.jtests.test;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import java.util.Map;

public abstract class UserTaskWorkflowTest extends WorkflowLogicTest {

    public UserTaskWorkflowTest(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public abstract Map<String, Object> getRequiredUserTaskForms();

    @Override
    public void cleanup() throws LHApiError {
        for (Map.Entry<String, Object> pair : getRequiredUserTaskForms().entrySet()) {
            client.deleteUserTaskDef(pair.getKey());
        }
        super.cleanup();
    }

    public void deploy(LHClient client, LHWorkerConfig config)
        throws LogicTestFailure {
        // Deploy the UserTaskDef's
        for (Map.Entry<String, Object> pair : getRequiredUserTaskForms().entrySet()) {
            UserTaskSchema schema = new UserTaskSchema(
                pair.getValue(),
                pair.getKey()
            );
            try {
                client.putUserTaskDef(schema.compile(), true);
            } catch (LHApiError exn) {
                throw new LogicTestFailure(
                    this,
                    "Failed setting up userTask: " + exn.getMessage()
                );
            }
        }
        super.deploy(client, config);
    }
}

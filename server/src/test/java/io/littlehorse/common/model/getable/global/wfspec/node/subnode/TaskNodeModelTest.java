package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.grpc.Status.Code;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ExponentialBackoffRetryPolicyModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.server.TestCommandExecutionContext;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TaskNodeModelTest {

    private final PutTenantRequestModel dummySubcommand = new PutTenantRequestModel("my-tenant");
    private final MetadataCommandModel dummyCommand = new MetadataCommandModel(dummySubcommand);
    private TestCommandExecutionContext commandContext =
            TestCommandExecutionContext.create(dummyCommand.toProto().build());

    private static TaskNodeModel createTaskNodeWithEmptyTaskDef() {
        TaskDefIdModel id = new TaskDefIdModel("some-task");
        ReadOnlyMetadataManager manager = Mockito.mock(ReadOnlyMetadataManager.class);
        TaskNodeModel toTest = new TaskNodeModel();
        toTest.setTaskDefId(id);
        when(manager.get(id)).thenReturn(new TaskDefModel());
        return toTest;
    }

    @Test
    void shouldBeFineWithNoRetryPolicy() {
        TaskNodeModel noRetry = createTaskNodeWithEmptyTaskDef();
        noRetry.validate(commandContext);
    }

    @Test
    void shouldThrowWhenMaxDelayLessThanBaseDelay() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setSimpleRetries(20);
        toTest.setExponentialBackoffRetryPolicy(new ExponentialBackoffRetryPolicyModel(100, (long) 50, (float) 1.1));

        try {
            toTest.validate(commandContext);
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
        }
    }

    @Test
    void shouldThrowWhenBaseDelayIsZero() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setExponentialBackoffRetryPolicy(new ExponentialBackoffRetryPolicyModel(0, (long) 50, (float) 1.1));

        try {
            toTest.validate(commandContext);
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
            assertThat(exn.getStatus().getDescription().toLowerCase()).contains("base interval must be > 0");
        }
    }

    @Test
    void shouldThrowWhenSimpleRetriesAreNegative() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setSimpleRetries(-1);

        try {
            toTest.validate(commandContext);
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
            assertThat(exn.getStatus().getDescription().toLowerCase()).contains("negative retries");
        }
    }
}

package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.grpc.Status.Code;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ExponentialBackoffRetryPolicyModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.sdk.common.proto.TaskNode.RetryPolicyCase;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TaskNodeModelTest {

    private static TaskNodeModel createTaskNodeWithEmptyTaskDef() {
        TaskDefIdModel id = new TaskDefIdModel("some-task");
        ReadOnlyMetadataManager manager = Mockito.mock(ReadOnlyMetadataManager.class);
        TaskNodeModel toTest = new TaskNodeModel();
        toTest.setMetadataManager(manager);
        toTest.setTaskDefId(id);
        when(manager.get(id)).thenReturn(new TaskDefModel());
        return toTest;
    }

    @Test
    void shouldBeFineWithNoRetryPolicy() {
        TaskNodeModel noRetry = createTaskNodeWithEmptyTaskDef();
        noRetry.validate();
    }

    @Test
    void shouldThrowWhenMaxDelayLessThanBaseDelay() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setRetryPolicyType(RetryPolicyCase.EXPONENTIAL_BACKOFF);
        toTest.setExponentialBackoffRetryPolicy(
                new ExponentialBackoffRetryPolicyModel(100, (long) 50, 20, (float) 1.1));

        try {
            toTest.validate();
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
        }
    }

    @Test
    void shouldThrowWhenBaseDelayIsZero() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setRetryPolicyType(RetryPolicyCase.EXPONENTIAL_BACKOFF);
        toTest.setExponentialBackoffRetryPolicy(new ExponentialBackoffRetryPolicyModel(0, (long) 50, 20, (float) 1.1));

        try {
            toTest.validate();
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
            assertThat(exn.getStatus().getDescription().toLowerCase()).contains("base interval must be > 0");
        }
    }

    @Test
    void shouldThrowWhenSimpleRetriesAreNegative() {
        TaskNodeModel toTest = createTaskNodeWithEmptyTaskDef();
        toTest.setRetryPolicyType(RetryPolicyCase.SIMPLE_RETRIES);
        toTest.setSimpleRetries(-1);

        try {
            toTest.validate();
            throw new RuntimeException("Should've gotten an error!");
        } catch (LHApiException exn) {
            assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
            assertThat(exn.getStatus().getDescription().toLowerCase()).contains("negative retries");
        }
    }
}

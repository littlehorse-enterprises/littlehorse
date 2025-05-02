package io.littlehorse.common.model.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskNodeReference;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunSource;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

public class TaskRunModelTest {

    private final String tenantId = "myTenantId";
    private final ExecutionContext executionContext = mock();
    private final ProcessorExecutionContext processorContext = mock(Answers.RETURNS_DEEP_STUBS);

    @Test
    void setTaskWorkerVersionAndIdToTaskRun() {
        // arrange. Complex because all the dependencies needed
        NodeRunId nodeRunId = NodeRunId.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId("my-wf"))
                .build();
        TaskRun taskRunProto = TaskRun.newBuilder()
                .setId(new TaskRunIdModel(new WfRunIdModel("asdf"), processorContext).toProto())
                .addAttempts(TaskAttempt.newBuilder().setStatus(TaskStatus.TASK_PENDING))
                .setSource(TaskRunSource.newBuilder()
                        .setTaskNode(TaskNodeReference.newBuilder().setNodeRunId(nodeRunId)))
                .build();
        when(executionContext.castOnSupport(ProcessorExecutionContext.class)).thenReturn(processorContext);

        TaskRunModel taskRun = TaskRunModel.fromProto(taskRunProto, executionContext);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        AuthorizationContext mockContext = mock(AuthorizationContext.class);
        when(mockContext.tenantId()).thenReturn(new TenantIdModel(tenantId));
        when(executionContext.authorization()).thenReturn(mockContext);
        taskRun.setInputVariables(new ArrayList<>());

        taskRun.dispatchTaskToQueue();
        verify(processorContext.getTaskManager()).scheduleTask(any());

        TaskClaimEvent taskClaimEvent = new TaskClaimEvent();
        // act
        taskRun.onTaskAttemptStarted(taskClaimEvent);

        // assert
        assertThat(taskRun.getLatestAttempt().getTaskWorkerVersion()).isEqualTo(taskClaimEvent.getTaskWorkerVersion());
        assertThat(taskRun.getLatestAttempt().getTaskWorkerId()).isEqualTo(taskClaimEvent.getTaskWorkerId());
    }
}

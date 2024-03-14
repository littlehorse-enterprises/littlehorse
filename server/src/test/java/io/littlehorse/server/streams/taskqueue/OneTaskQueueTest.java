package io.littlehorse.server.streams.taskqueue;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.BulkUpdateJob;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.NoOpJob;
import io.littlehorse.server.TestProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.List;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class OneTaskQueueTest {
    private final TaskQueueManager taskQueueManager = mock(Answers.RETURNS_DEEP_STUBS);
    private final String taskName = "my-task";
    private final PollTaskRequestObserver mockClient = mock(Answers.RETURNS_DEEP_STUBS);
    private final ScheduledTaskModel mockTask = mock(Answers.RETURNS_DEEP_STUBS);
    private final OneTaskQueue taskQueue = new OneTaskQueue(
            taskName, taskQueueManager, Integer.MAX_VALUE, new TenantIdModel(LHConstants.DEFAULT_TENANT));
    private final Command command = commandProto();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessor = new MockProcessorContext<>();
    private final TestProcessorExecutionContext processorContext = TestProcessorExecutionContext.create(
            command,
            HeadersUtil.metadataHeadersFor(
                    new TenantIdModel(LHConstants.DEFAULT_TENANT),
                    new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL)),
            mockProcessor);
    private RequestExecutionContext requestContext = mock();

    @BeforeEach
    public void setup() {
        when(mockClient.getTaskDefId()).thenReturn(taskName);
        when(mockClient.getFreshExecutionContext().getableManager()).thenReturn(processorContext.getableManager());
        when(requestContext.getableManager()).thenReturn(processorContext.getableManager());
    }

    @Test
    public void shouldEnqueueScheduledTask() {
        taskQueue.onTaskScheduled(mockTask);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onPollRequest(mockClient, requestContext);
        verify(taskQueueManager, times(1)).itsAMatch(mockTask, mockClient);
    }

    @Test
    public void shouldRememberPendingClient() {
        taskQueue.onPollRequest(mockClient, requestContext);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onTaskScheduled(mockTask);
        verify(taskQueueManager, times(1)).itsAMatch(same(mockTask), same(mockClient));
    }

    @Test
    public void shouldNotEnqueuePendingTaskWhenQueueIsFull() {
        OneTaskQueue boundedQueue =
                new OneTaskQueue(taskName, taskQueueManager, 3, new TenantIdModel(LHConstants.DEFAULT_TENANT));
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isFalse();
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        verify(taskQueueManager, times(3)).itsAMatch(mockTask, mockClient);
    }

    @Test
    public void shouldRecoverScheduledTaskFromStoreAndKeepTheOriginalOrder() {
        ScheduledTaskModel task1 = TestUtil.scheduledTaskModel("wf-1");
        ScheduledTaskModel task2 = TestUtil.scheduledTaskModel("wf-2");
        ScheduledTaskModel task3 = TestUtil.scheduledTaskModel("wf-3");
        ScheduledTaskModel task4 = TestUtil.scheduledTaskModel("wf-4");

        TaskRunModel taskRun3 = TestUtil.taskRun(task3.getTaskRunId(), task3.getTaskDefId());
        TaskRunModel taskRun4 = TestUtil.taskRun(task4.getTaskRunId(), task4.getTaskDefId());
        processorContext.getableManager().put(taskRun3);
        processorContext.getableManager().put(taskRun4);
        processorContext.getCoreStore().put(task3);
        processorContext.getCoreStore().put(task4);
        processorContext.endExecution();
        ArgumentCaptor<ScheduledTaskModel> captor = ArgumentCaptor.forClass(ScheduledTaskModel.class);
        OneTaskQueue boundedQueue =
                new OneTaskQueue(taskName, taskQueueManager, 2, new TenantIdModel(LHConstants.DEFAULT_TENANT));

        boundedQueue.onTaskScheduled(task1);
        boundedQueue.onTaskScheduled(task2);
        boundedQueue.onTaskScheduled(task3);
        Assertions.assertThat(boundedQueue.isOutOfCapacity()).isTrue();
        boundedQueue.onTaskScheduled(task4);

        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        processorContext.getCoreStore().delete(task3);
        boundedQueue.onPollRequest(mockClient, requestContext);
        InOrder inOrder = inOrder(taskQueueManager);
        inOrder.verify(taskQueueManager).itsAMatch(same(task1), same(mockClient));
        inOrder.verify(taskQueueManager).itsAMatch(same(task2), same(mockClient));
        inOrder.verify(taskQueueManager, times(2)).itsAMatch(captor.capture(), same(mockClient));
        List<ScheduledTaskModel> allValues = captor.getAllValues();
        assertThat(allValues.get(0).getTaskRunId().wfRunId.getId())
                .isEqualTo(task3.getTaskRunId().wfRunId.getId());
        assertThat(allValues.get(1).getTaskRunId().wfRunId.getId())
                .isEqualTo(task4.getTaskRunId().wfRunId.getId());
    }

    private Command commandProto() {
        NoOpJob job = NoOpJob.newBuilder().build();
        BulkUpdateJob bulkJob = BulkUpdateJob.newBuilder()
                .setPartition(1)
                .setStartKey(GetableClassEnum.WF_RUN_VALUE + "/")
                .setEndKey(GetableClassEnum.WF_RUN_VALUE + "/~")
                .setNoOp(job)
                .build();
        return Command.newBuilder().setBulkJob(bulkJob).build();
    }
}

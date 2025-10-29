package io.littlehorse.server.streams.taskqueue;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.storeinternals.InMemoryGetableManager;
import java.util.Date;
import org.apache.kafka.streams.processor.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InOrder;

public class OneTaskQueueTest {
    private final TaskQueueManager taskQueueManager = mock(Answers.RETURNS_DEEP_STUBS);
    private final String taskName = "my-task";
    private final PollTaskRequestObserver mockClient = mock(Answers.RETURNS_DEEP_STUBS);
    private final ScheduledTaskModel mockTask = mock(Answers.RETURNS_DEEP_STUBS);
    private final TaskRunIdModel mockTaskRunId = mock(Answers.RETURNS_DEEP_STUBS);
    private final OneTaskQueue taskQueue =
            new OneTaskQueue(taskName, taskQueueManager, new TenantIdModel(LHConstants.DEFAULT_TENANT));
    private final TaskId streamsTaskId = TaskId.parse("0_2");
    private final RequestExecutionContext requestContext = mock();
    private final CoreProcessorContext mockProcessorContext = mock(Answers.RETURNS_DEEP_STUBS);
    private InMemoryGetableManager getableManager;

    @BeforeEach
    public void setup() {
        when(mockClient.getTaskDefId()).thenReturn(taskName);
        when(requestContext.getableManager(streamsTaskId)).thenReturn(getableManager);
        getableManager = new InMemoryGetableManager(mockProcessorContext);
        when(requestContext.getableManager(streamsTaskId)).thenReturn(getableManager);
    }

    @Test
    public void shouldEnqueueScheduledTask() {
        taskQueue.onTaskScheduled(streamsTaskId, mockTaskRunId);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onPollRequest(mockClient, requestContext);
        verify(taskQueueManager, times(1)).itsAMatch(mockTaskRunId, mockClient);
    }

    @Test
    public void shouldRememberPendingClient() {
        taskQueue.onPollRequest(mockClient, requestContext);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onTaskScheduled(streamsTaskId, mockTaskRunId);
        verify(taskQueueManager, times(1)).itsAMatch(same(mockTaskRunId), same(mockClient));
    }

    @Test
    public void shouldRecoverScheduledTaskFromStoreAndKeepTheOriginalOrder() {
        ScheduledTaskModel task1 = TestUtil.scheduledTaskModel("wf-1");
        task1.setCreatedAt(new Date(new Date().getTime() + 2000L));
        ScheduledTaskModel task2 = TestUtil.scheduledTaskModel("wf-2");
        task2.setCreatedAt(new Date(new Date().getTime() + 3000L));
        ScheduledTaskModel task3 = TestUtil.scheduledTaskModel("wf-3");
        task3.setCreatedAt(new Date(new Date().getTime() + 4000L));
        ScheduledTaskModel task4 = TestUtil.scheduledTaskModel("wf-4");
        task4.setCreatedAt(new Date(new Date().getTime() + 5000L));

        getableManager.put(task1);
        getableManager.put(task2);
        getableManager.put(task3);
        getableManager.put(task4);
        OneTaskQueue boundedQueue =
                new OneTaskQueue(taskName, taskQueueManager, new TenantIdModel(LHConstants.DEFAULT_TENANT));

        boundedQueue.onTaskScheduled(streamsTaskId, task1.getTaskRunId());
        boundedQueue.onTaskScheduled(streamsTaskId, task2.getTaskRunId());
        boundedQueue.onTaskScheduled(streamsTaskId, task3.getTaskRunId());
        boundedQueue.onTaskScheduled(streamsTaskId, task4.getTaskRunId());

        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        boundedQueue.onPollRequest(mockClient, requestContext);
        InOrder inOrder = inOrder(taskQueueManager);
        inOrder.verify(taskQueueManager, times(4)).itsAMatch(any(), same(mockClient));
    }
}

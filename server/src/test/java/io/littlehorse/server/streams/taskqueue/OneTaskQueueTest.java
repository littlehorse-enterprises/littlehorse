package io.littlehorse.server.streams.taskqueue;

import io.littlehorse.common.model.ScheduledTaskModel;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.mockito.Mockito.*;

public class OneTaskQueueTest {
    private final TaskQueueManager taskQueueManager = mock(Answers.RETURNS_DEEP_STUBS);
    private final String taskName = "my-task";
    private final PollTaskRequestObserver mockClient = mock();
    private final ScheduledTaskModel mockTask = mock(Answers.RETURNS_DEEP_STUBS);
    private final OneTaskQueue taskQueue = new OneTaskQueue(taskName, taskQueueManager, Integer.MAX_VALUE);

    @BeforeEach
    public void setup(){
        when(mockClient.getTaskDefId()).thenReturn(taskName);
    }

    @Test
    public void shouldEnqueueScheduledTask() {
        taskQueue.onTaskScheduled(mockTask);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onPollRequest(mockClient);
        verify(taskQueueManager, times(1)).itsAMatch(mockTask, mockClient);
    }

    @Test
    public void shouldRememberPendingClient() {
        taskQueue.onPollRequest(mockClient);
        verify(taskQueueManager, never()).itsAMatch(any(), any());
        taskQueue.onTaskScheduled(mockTask);
        verify(taskQueueManager, times(1)).itsAMatch(mockTask, mockClient);
    }

    @Test
    public void shouldNotEnqueuePendingTaskWhenQueueIsFull() {
        OneTaskQueue boundedQueue = new OneTaskQueue(taskName, taskQueueManager, 3);
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isTrue();
        assertThat(boundedQueue.onTaskScheduled(mockTask)).isFalse();
        boundedQueue.onPollRequest(mockClient);
        boundedQueue.onPollRequest(mockClient);
        boundedQueue.onPollRequest(mockClient);
        boundedQueue.onPollRequest(mockClient);
        verify(taskQueueManager, times(3)).itsAMatch(mockTask, mockClient);
    }


}

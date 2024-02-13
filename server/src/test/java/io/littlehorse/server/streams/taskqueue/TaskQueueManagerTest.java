package io.littlehorse.server.streams.taskqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskQueueManagerTest {

    private final KafkaStreamsServerImpl mockServer = Mockito.mock();
    private final TaskQueueManager queueManager = new TaskQueueManager(mockServer);
    private final TaskDefModel taskDef = TestUtil.taskDef("my-task");
    private final TaskDefIdModel taskId = taskDef.getId();
    private final ProcessorExecutionContext processorContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);
    private final RequestExecutionContext requestContext = Mockito.mock(Answers.RETURNS_DEEP_STUBS);
    private final UserTaskRunModel userTaskRun =
            TestUtil.userTaskRun(UUID.randomUUID().toString(), processorContext);
    private ScheduledTaskModel taskToSchedule;
    private final TenantIdModel tenantId = new TenantIdModel("my-tenant");

    private PollTaskRequestObserver trackableObserver;

    private PollTaskRequest pollTask = PollTaskRequest.newBuilder()
            .setTaskDefId(taskId.toProto().build())
            .setClientId("my-client")
            .build();

    @BeforeEach
    void setup() {
        when(processorContext.getableManager().get(any())).thenReturn(TestUtil.nodeRun());
        when(requestContext.authorization().tenantId()).thenReturn(new TenantIdModel("my-tenant"));
        taskToSchedule = new ScheduledTaskModel(taskId, List.of(), userTaskRun, processorContext);
        trackableObserver = new PollTaskRequestObserver(mock(), queueManager, requestContext);
    }

    @Test
    public void shouldSchedulePendingTask() {
        queueManager.onTaskScheduled(taskId, taskToSchedule, tenantId);
        // Task was scheduled, now we need to verify only one task is returned to the client
        trackableObserver.onNext(pollTask);
        verify(mockServer, times(1)).returnTaskToClient(taskToSchedule, trackableObserver);
        Mockito.reset(mockServer);
        trackableObserver.onNext(pollTask);
        verify(mockServer, never()).returnTaskToClient(taskToSchedule, trackableObserver);
    }

    @Test
    public void shouldFeedHungryClientWhenATaskIsScheduled() {
        trackableObserver.onNext(pollTask);
        verify(mockServer, never()).returnTaskToClient(taskToSchedule, trackableObserver);
        queueManager.onTaskScheduled(taskId, taskToSchedule, tenantId);
        verify(mockServer, times(1)).returnTaskToClient(taskToSchedule, trackableObserver);
    }

    @Test
    public void shouldSchedulePendingTaskConcurrently() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(10);
        int numberOfTaskToSchedule = 100_000;
        try {
            for (int i = 0; i < numberOfTaskToSchedule; i++) {
                service.submit(
                        Executors.callable(() -> queueManager.onTaskScheduled(taskId, taskToSchedule, tenantId)));
            }
        } finally {
            service.shutdown();
            assertThat(service.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
        }
        for (int i = 0; i < numberOfTaskToSchedule; i++) {
            trackableObserver.onNext(pollTask);
        }
        verify(mockServer, times(numberOfTaskToSchedule)).returnTaskToClient(taskToSchedule, trackableObserver);
    }
}

package io.littlehorse.server.streams.taskqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.server.streams.CommandSender;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.streams.processor.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Disabled
public class TaskQueueManagerTest {

    private final CommandSender commandSender = Mockito.mock();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    private final TaskQueueManager queueManager =
            new TaskQueueManager("test", executor, commandSender, Integer.MAX_VALUE);
    private final TaskDefModel taskDef = TestUtil.taskDef("my-task");
    private final TaskDefIdModel taskId = taskDef.getId();
    private final TaskId streamsTaskId = TaskId.parse("0_1");
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
        when(requestContext.getableManager().get(any())).thenReturn(TestUtil.nodeRun());
        when(requestContext.authorization().tenantId()).thenReturn(new TenantIdModel("my-tenant"));
        taskToSchedule = new ScheduledTaskModel(taskId, List.of(), userTaskRun, processorContext);
        trackableObserver = spy(new PollTaskRequestObserver(
                "test",
                mock(),
                queueManager,
                new TenantIdModel("my-tenant"),
                new PrincipalIdModel(""),
                mock(),
                new MetadataCache(),
                mock(),
                mock()));
        doReturn(requestContext).when(trackableObserver).getFreshExecutionContext();
    }

    @Test
    public void shouldSchedulePendingTask() {
        queueManager.onTaskScheduled(streamsTaskId, taskId, taskToSchedule, tenantId);
        // Task was scheduled, now we need to verify only one task is returned to the client
        trackableObserver.onNext(pollTask);
        verify(commandSender, times(1))
                .doSend(eq(new TaskClaimEvent(taskToSchedule, "123", "567")), requestContext.authorization());
        Mockito.reset(commandSender);
        trackableObserver.onNext(pollTask);
        verify(commandSender, never()).doSend(any(TaskClaimEvent.class), any());
    }

    @Test
    public void shouldFeedHungryClientWhenATaskIsScheduled() {
        trackableObserver.onNext(pollTask);
        verify(commandSender, never()).doSend(any(TaskClaimEvent.class), any());
        queueManager.onTaskScheduled(streamsTaskId, taskId, taskToSchedule, tenantId);
        verify(commandSender, times(1)).doSend(eq(new TaskClaimEvent(taskToSchedule, "123", "456")), any());
    }

    @Test
    public void shouldSchedulePendingTaskConcurrently() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(10);
        int numberOfTaskToSchedule = 100_000;
        try {
            for (int i = 0; i < numberOfTaskToSchedule; i++) {
                service.submit(Executors.callable(() ->
                        queueManager.onTaskScheduled(streamsTaskId, taskId, TestUtil.scheduledTaskModel(), tenantId)));
            }
        } finally {
            service.shutdown();
            assertThat(service.awaitTermination(6, TimeUnit.SECONDS)).isTrue();
        }
        for (int i = 0; i < numberOfTaskToSchedule; i++) {
            trackableObserver.onNext(pollTask);
        }
        verify(commandSender, times(numberOfTaskToSchedule)).doSend(any(TaskClaimEvent.class), any());
    }
}

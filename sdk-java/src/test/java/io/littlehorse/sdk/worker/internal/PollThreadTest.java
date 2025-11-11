package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.QueuedStreamObserver;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

public class PollThreadTest {

    private final LittleHorseGrpc.LittleHorseStub stub = mock();
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub = mock();
    private final TaskDefId task = TaskDefId.newBuilder().setName("my-task").build();
    private final ScheduledTaskExecutor taskExecutor = mock();
    private final String taskWorkerId = "my-worker";
    private final String taskWorkerVersion = "0";
    private final List<VariableMapping> mappings = List.of();
    private final LHConfig config = mock();
    private Method taskMethod;

    private PollThread pollThread;
    private QueuedStreamObserver<PollTaskRequest, PollTaskResponse> recordableObserver;
    private PollThreadFactory pollThreadFactory;
    private QueuedStreamObserver.DelegatedStreamObserver<PollTaskRequest> delegatedObserver;

    @BeforeEach
    public void setup() throws NoSuchMethodException {
        this.taskMethod = this.getClass().getDeclaredMethod("myTaskMethod");
        when(config.getAsyncStub(anyString(), anyInt())).thenReturn(stub);
        ArgumentCaptor<StreamObserver<PollTaskResponse>> argumentCaptor = ArgumentCaptor.forClass(StreamObserver.class);
        delegatedObserver = new QueuedStreamObserver.DelegatedStreamObserver<>();
        when(config.getInflightTasks()).thenReturn(1);
        when(stub.pollTask(any())).thenReturn(delegatedObserver);
        when(config.getTaskWorkerVersion()).thenReturn(taskWorkerVersion);
        pollThreadFactory = new PollThreadFactory(
                config, bootstrapStub, task, taskWorkerId, mappings, this, taskMethod, taskExecutor);

        pollThread = pollThreadFactory.create(
                "test", LHHostInfo.newBuilder().setHost("a").setPort(1).build());
        verify(stub, atLeast(0)).pollTask(argumentCaptor.capture());
        this.recordableObserver = new QueuedStreamObserver<>(argumentCaptor.getValue());
        delegatedObserver.setObserver(recordableObserver.getRequestObserver());
    }

    @Test
    public void shouldPollScheduledTask() {
        PollTaskResponse response1 = mock(Answers.RETURNS_DEEP_STUBS);
        PollTaskResponse response2 = mock(Answers.RETURNS_DEEP_STUBS);
        PollTaskResponse response3 = mock(Answers.RETURNS_DEEP_STUBS);
        when(response1.hasResult()).thenReturn(true);
        when(response2.hasResult()).thenReturn(true);
        when(response3.hasResult()).thenReturn(false);
        recordableObserver.record(response1);
        recordableObserver.record(response2);
        recordableObserver.record(response3);
        pollThread.start();
        Awaitility.await().until(() -> !pollThread.isAlive());
        verify(taskExecutor, times(2)).doTask(any(), same(bootstrapStub), eq(mappings), same(this), eq(taskMethod));
        assertThat(pollThread.isRunning()).isFalse();
    }

    @Test
    public void shouldClosePollThreadOnServerError() {
        PollTaskResponse response1 = mock(Answers.RETURNS_DEEP_STUBS);
        PollTaskResponse response2 = mock(Answers.RETURNS_DEEP_STUBS);
        when(response1.hasResult()).thenReturn(true);
        when(response2.hasResult()).thenReturn(true);
        recordableObserver.record(response1);
        recordableObserver.record(response2);
        recordableObserver.record(new RuntimeException("Failed"));
        pollThread.start();
        Awaitility.await().until(() -> !pollThread.isAlive());
        verify(taskExecutor, times(2)).doTask(any(), same(bootstrapStub), eq(mappings), same(this), eq(taskMethod));
        assertThat(pollThread.isRunning()).isFalse();
        pollThread.close();
        assertThat(delegatedObserver.isCompleted()).isTrue();
    }

    @Test
    public void shouldClosePollThreadOnRuntimeException() {
        delegatedObserver.setObserver(new StreamObserver<>() {
            @Override
            public void onNext(PollTaskRequest value) {
                throw new RuntimeException("kaboom!");
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });
        try {
            pollThread.start();
        } catch (Exception ignored) {
        }
        verify(taskExecutor, never()).doTask(any(), any(), any(), any(), any());
        assertThat(pollThread.isRunning()).isFalse();
        assertThat(delegatedObserver.isCompleted()).isTrue();
    }

    public void myTaskMethod() {}
}

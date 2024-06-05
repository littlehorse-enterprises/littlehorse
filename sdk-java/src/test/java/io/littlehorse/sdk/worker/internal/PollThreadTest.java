package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.QueuedStreamObserver;
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
    private Method taskMethod;

    private PollThread pollThread;
    private QueuedStreamObserver<PollTaskRequest, PollTaskResponse> recordableObserver;

    @BeforeEach
    public void setup() throws NoSuchMethodException {
        this.taskMethod = this.getClass().getDeclaredMethod("myTaskMethod");
        ArgumentCaptor<StreamObserver<PollTaskResponse>> argumentCaptor = ArgumentCaptor.forClass(StreamObserver.class);
        QueuedStreamObserver.DelegatedStreamObserver<PollTaskRequest> delegatedObserver =
                new QueuedStreamObserver.DelegatedStreamObserver<>();
        when(stub.pollTask(any())).thenReturn(delegatedObserver);
        pollThread = new PollThread(
                "test",
                1,
                stub,
                bootstrapStub,
                task,
                taskWorkerId,
                taskWorkerVersion,
                mappings,
                this,
                taskMethod,
                taskExecutor);
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
    }

    public void myTaskMethod() {}
}

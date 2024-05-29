package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.RecordableStreamObserver;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.internal.util.VariableMapping;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PollThreadTest {

    private final LittleHorseGrpc.LittleHorseStub stub = Mockito.mock();
    private final LittleHorseGrpc.LittleHorseStub bootstrapStub = Mockito.mock();
    private final TaskDefId task = TaskDefId.newBuilder().setName("my-task").build();
    private final String taskWorkerId = "my-worker";
    private final String taskWorkerVersion = "0";
    private final List<VariableMapping> mappings = List.of();
    private Method taskMethod;

    private PollThread pollThread;
    private RecordableStreamObserver<PollTaskRequest, PollTaskResponse> recordableObserver;

    @BeforeEach
    public void setup() throws NoSuchMethodException {
        this.taskMethod = this.getClass().getDeclaredMethod("myTaskMethod");
        pollThread = new PollThread(
                "test", stub, bootstrapStub, task, taskWorkerId, taskWorkerVersion, mappings, this, taskMethod);
        this.recordableObserver = new RecordableStreamObserver<>(pollThread);
    }

    @Test
    public void shouldPollScheduledTask() {
        ScheduledTask.Builder builder = ScheduledTask.newBuilder();
        PollTaskResponse.newBuilder().setResult().build();
        recordableObserver.record();
        pollThread.start();
    }

    public void myTaskMethod() {}
}

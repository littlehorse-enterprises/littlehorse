package io.littlehorse.sdk.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class LHTaskWorkerTest {

    @Test
    public void getUnhealthyIfManagerIsNull() {
        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), "greet", Map.of(), new LHConfig(), null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, task::healthStatus);
        assertThat(exception.getMessage()).isEqualTo("Worker not started");
    }

    @Test
    public void shouldResolvePlaceHolder() {
        String taskDefName = "a-task-name-${CLUSTER_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("a-task-name-pedro-cluster");
    }

    @Test
    public void shouldResolvePlaceHolderWhenItIsTheOnlyTextOnTheStringTemplate() {
        String taskDefName = "${CLUSTER_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("pedro-cluster");
    }

    @Test
    public void shouldResolve2PlaceHolders() {
        String taskDefName = "a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}";
        Map<String, String> values = Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("a-task-name-pedro-cluster-aws");
    }

    @Test
    public void shouldResolve3PlaceHoldersWithOnePlaceholderAtTheBeginningOfTheTemplate() {
        String taskDefName = "${REGION}_a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}";
        Map<String, String> values =
                Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws", "REGION", "us-west-2");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("us-west-2_a-task-name-pedro-cluster-aws");
    }

    @Test
    public void taskDefNameRemainsTheSameIfItHasNoPlaceholders() {
        String taskDefName = "greet";
        Map<String, String> values =
                Map.of("CLUSTER_NAME", "pedro-cluster", "CLOUD_NAME", "aws", "REGION", "us-west-2");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), taskDefName, new LHConfig(), values);

        assertThat(task.getTaskDefName()).isEqualTo("greet");
    }

    @Test
    public void shouldFailWithClearMessageWhenTaskDefIsNotRegistered() {
        LHConfig config = mock(LHConfig.class);
        LittleHorseBlockingStub grpcClient = mock(LittleHorseBlockingStub.class);
        LHServerConnectionManager manager = mock(LHServerConnectionManager.class);

        when(config.getBlockingStub()).thenReturn(grpcClient);
        when(config.getTypeAdapterRegistry()).thenReturn(LHTypeAdapterRegistry.empty());
        when(grpcClient.getTaskDef(any(TaskDefId.class))).thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), "greet", Map.of(), config, manager);

        IllegalStateException ex = assertThrows(IllegalStateException.class, task::start);
        assertThat(ex.getMessage())
                .isEqualTo("TaskDef 'greet' was not found on the server. Register it before starting this worker.");
    }

    @Test
    public void shouldApplyPlaceholdersForInlineStructTypes() {
        LHConfig config = mock(LHConfig.class);
        when(config.getTypeAdapterRegistry()).thenReturn(LHTypeAdapterRegistry.empty());
        LittleHorseBlockingStub grpcClient = mock(LittleHorseBlockingStub.class);
        AtomicReference<PutTaskDefRequest> capturedRequest = new AtomicReference<>();

        when(config.getBlockingStub()).thenReturn(grpcClient);
        doAnswer(invocation -> {
                    PutTaskDefRequest request = invocation.getArgument(0);
                    capturedRequest.set(request);
                    return TaskDef.newBuilder()
                            .setId(TaskDefId.newBuilder()
                                    .setName(request.getName())
                                    .build())
                            .build();
                })
                .when(grpcClient)
                .putTaskDef(any(PutTaskDefRequest.class));

        Map<String, String> placeholders = Map.of(
                "taskName", "inline-struct-acme",
                "inputStruct", "customer-request",
                "outputStruct", "customer");

        LHTaskWorker task = new LHTaskWorker(new TaskWorker(), "${taskName}", config, placeholders);
        task.registerTaskDef();

        PutTaskDefRequest request = capturedRequest.get();
        assertThat(request.getName()).isEqualTo("inline-struct-acme");
        assertThat(request.getInputVars(0).getTypeDef().getStructDefId().getName())
                .isEqualTo("customer-request");
        assertThat(request.getReturnType().getReturnType().getStructDefId().getName())
                .isEqualTo("customer");
        verify(grpcClient).putTaskDef(any(PutTaskDefRequest.class));
    }

    @Test
    public void shouldResolveTaskMethodWithCustomTaskResolver() {
        LHConfig config = mock(LHConfig.class);
        when(config.getTypeAdapterRegistry()).thenReturn(LHTypeAdapterRegistry.empty());
        LittleHorseBlockingStub grpcClient = mock(LittleHorseBlockingStub.class);
        AtomicReference<PutTaskDefRequest> capturedRequest = new AtomicReference<>();

        when(config.getBlockingStub()).thenReturn(grpcClient);
        doAnswer(invocation -> {
                    PutTaskDefRequest request = invocation.getArgument(0);
                    capturedRequest.set(request);
                    return TaskDef.newBuilder()
                            .setId(TaskDefId.newBuilder()
                                    .setName(request.getName())
                                    .build())
                            .build();
                })
                .when(grpcClient)
                .putTaskDef(any(PutTaskDefRequest.class));

        LHTaskWorker task = new LHTaskWorker(
                new TaskWorker(),
                "custom-task-resolver",
                config,
                Map.of(),
                (executable, taskDefName, placeholderValues) -> {
                    try {
                        return executable.getClass().getMethod(
                            "withCustomTaskResolver", String.class, Integer.class, Boolean.class, WfRunId.class);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
        task.registerTaskDef();

        PutTaskDefRequest request = capturedRequest.get();
        assertThat(request.getName()).isEqualTo("custom-task-resolver");
        assertThat(request.getInputVars(0).getTypeDef().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(request.getInputVars(1).getTypeDef().getPrimitiveType()).isEqualTo(VariableType.INT);
        assertThat(request.getInputVars(2).getTypeDef().getPrimitiveType()).isEqualTo(VariableType.BOOL);
        assertThat(request.getInputVars(3).getTypeDef().getPrimitiveType()).isEqualTo(VariableType.WF_RUN_ID);
        assertThat(request.getReturnType().getReturnType().getPrimitiveType()).isEqualTo(VariableType.STR);
        verify(grpcClient).putTaskDef(any(PutTaskDefRequest.class));
    }
}

class TaskWorker {
    @LHTaskMethod("greet")
    public String greeting(String name) {
        return "hello there, " + name;
    }

    @LHTaskMethod("something-${INVALID_PLACEHOLDER}")
    public String withInvalidPlaceHolder(String name) {
        return "task with invalid placeholder " + name;
    }

    @LHTaskMethod("${REGION}_a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}")
    public String withPlaceHolderAtTheBeginning(String name) {
        return "task with placeholder at the beginning " + name;
    }

    @LHTaskMethod("a-task-name-${CLUSTER_NAME}-${CLOUD_NAME}")
    public String with2PlaceHolderAtTheBeginning(String name) {
        return "task with 2 placeholders at the beginning " + name;
    }

    @LHTaskMethod("a-task-name-${CLUSTER_NAME}")
    public String with1PlaceHolderAtTheBeginning(String name) {
        return "task with 1 placeholders at the beginning " + name;
    }

    @LHTaskMethod("${CLUSTER_NAME}")
    public String onlyWithPlaceHolder(String name) {
        return "task only with placeholder " + name;
    }

    @LHTaskMethod("${taskName}")
    @LHType(structDefName = "${outputStruct}")
    public InlineStruct inlineStructTask(@LHType(structDefName = "${inputStruct}") InlineStruct input) {
        return input;
    }

    @LHTaskMethod("custom-task-resolver")
    public String withCustomTaskResolver(String name, Integer age, Boolean active, WfRunId wfRunId) {
        return "task with custom task resolver " + name + " " + age + " " + active + " " + wfRunId.getId();
    }
}

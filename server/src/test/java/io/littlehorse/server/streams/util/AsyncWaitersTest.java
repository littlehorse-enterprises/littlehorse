package io.littlehorse.server.streams.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.TestStreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalWaitForWfEventRequest;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsyncWaitersTest {

    public AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final InternalWaitForWfEventRequest mockRequest = mock(Answers.RETURNS_DEEP_STUBS);
    private final RequestExecutionContext requestContext = mock(Answers.RETURNS_DEEP_STUBS);
    private final WorkflowEventModel mockEvent = mock(Answers.RETURNS_DEEP_STUBS);
    private final WfRunIdModel wfRunId =
            TestUtil.wfRun(UUID.randomUUID().toString()).getId();
    private final TenantIdModel testTenantId = new TenantIdModel("test-tenant");

    @BeforeEach
    public void setup() {
        when(mockEvent.getId().getWfRunId()).thenReturn(wfRunId);
        when(mockRequest.getRequest().getWfRunId()).thenReturn(wfRunId.toProto().build());
    }

    @Test
    void shouldDeliverThrownEventToWaitingClient() {
        when(requestContext.getableManager().getWorkflowEvents(wfRunId)).thenReturn(List.of(mockEvent));
        TestStreamObserver<WorkflowEvent> clientObserver1 = new TestStreamObserver<>();
        asyncWaiters.registerObserverWaitingForWorkflowEvent(mockRequest, clientObserver1, requestContext);
        asyncWaiters.registerWorkflowEventHappened(mockEvent, testTenantId);
        Assertions.assertThat(clientObserver1.getValues()).hasSize(1).allMatch(Objects::nonNull);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
    }

    @Test
    void shouldSupportWorkflowEventsWithMultitenancy() {
        PrincipalIdModel principalId = new PrincipalIdModel("my-principal");
        TenantIdModel tenantId = new TenantIdModel("my-tenant");
        AuthorizationContext auth = new AuthorizationContextImpl(principalId, tenantId, List.of(), false);
        when(requestContext.authorization()).thenReturn(auth);
        TestStreamObserver<WorkflowEvent> clientObserver1 = new TestStreamObserver<>();
        asyncWaiters.registerObserverWaitingForWorkflowEvent(mockRequest, clientObserver1, requestContext);
        asyncWaiters.registerWorkflowEventHappened(mockEvent, tenantId);
        Assertions.assertThat(clientObserver1.getValues()).hasSize(1).allMatch(Objects::nonNull);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
    }

    @Test
    void shouldDeliverThrownEventToANewlyRegisteredClient() {
        when(requestContext.getableManager().getWorkflowEvents(wfRunId)).thenReturn(List.of(mockEvent));
        TestStreamObserver<WorkflowEvent> clientObserver1 = new TestStreamObserver<>();

        asyncWaiters.registerWorkflowEventHappened(mockEvent, testTenantId);
        asyncWaiters.registerObserverWaitingForWorkflowEvent(mockRequest, clientObserver1, requestContext);
        Assertions.assertThat(clientObserver1.getValues()).hasSize(1).allMatch(Objects::nonNull);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
    }

    @Test
    void shouldDeliverResponsesBackToWaitingClients() {
        TestStreamObserver<WaitForCommandResponse> clientObserver1 = new TestStreamObserver<>();
        String commandId = "command-1";
        WaitForCommandResponse mockResponse = Mockito.mock();
        asyncWaiters.registerObserverWaitingForCommand(commandId, 0, clientObserver1);
        asyncWaiters.registerCommandProcessed(commandId, mockResponse);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
        List<WaitForCommandResponse> commands = clientObserver1.getValues();
        Assertions.assertThat(commands).hasSize(1);
        Assertions.assertThat(commands.get(0)).isSameAs(mockResponse);
    }

    @Test
    void shouldGuaranteeUniqueResponseForWaitingClients() {
        TestStreamObserver<WaitForCommandResponse> clientObserver1 = new TestStreamObserver<>();
        String commandId = "command-1";
        WaitForCommandResponse mockResponse = Mockito.mock();
        asyncWaiters.registerCommandProcessed(commandId, mockResponse);
        asyncWaiters.registerObserverWaitingForCommand(commandId, 0, clientObserver1);
        // new clients should not get the previous response
        asyncWaiters.registerObserverWaitingForCommand(commandId, 0, clientObserver1);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
        List<WaitForCommandResponse> commands = clientObserver1.getValues();
        Assertions.assertThat(commands).hasSize(1);
        Assertions.assertThat(commands.get(0)).isSameAs(mockResponse);
    }

    @Test
    void shouldHandleConcurrencyForWaitingClients() throws Exception {
        ExecutorService executorService = Executors.newWorkStealingPool();
        final Map<String, TestStreamObserver<WaitForCommandResponse>> commandObservers = new HashMap<>();
        final int concurrentClients = 100_000;
        for (int i = 0; i < concurrentClients; i++) {
            String commandId = UUID.randomUUID().toString();
            TestStreamObserver<WaitForCommandResponse> clientObserver = new TestStreamObserver<>();
            commandObservers.put(commandId, clientObserver);
            executorService.submit(() -> asyncWaiters.registerObserverWaitingForCommand(commandId, 0, clientObserver));
            executorService.submit(() -> asyncWaiters.registerCommandProcessed(commandId, Mockito.mock()));
        }
        executorService.shutdown();
        boolean tasksTerminated = executorService.awaitTermination(3, TimeUnit.SECONDS);
        Assertions.assertThat(tasksTerminated).isTrue();
        Assertions.assertThat(commandObservers.keySet()).hasSize(concurrentClients);
        commandObservers.forEach((commandId, observer) -> {
            Assertions.assertThat(observer.getValues()).hasSize(1);
            Assertions.assertThat(observer.getValues().get(0)).isNotNull();
        });
    }
}

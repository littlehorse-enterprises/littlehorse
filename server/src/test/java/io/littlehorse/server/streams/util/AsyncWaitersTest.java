package io.littlehorse.server.streams.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.TestUtil;
import io.littlehorse.common.TestStreamObserver;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalWaitForWfEventRequest;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsyncWaitersTest {

    public AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final InternalWaitForWfEventRequest mockRequest = mock(Answers.RETURNS_DEEP_STUBS);
    private final RequestExecutionContext requestContext = mock(Answers.RETURNS_DEEP_STUBS);
    private final WorkflowEventModel mockEvent = mock(Answers.RETURNS_DEEP_STUBS);
    private final WfRunIdModel wfRunId =
            TestUtil.wfRun(UUID.randomUUID().toString()).getId();

    @BeforeEach
    public void setup() {
        when(mockEvent.getId().getWfRunId()).thenReturn(wfRunId);
        when(mockRequest.getRequest().getWfRunId()).thenReturn(wfRunId.toProto().build());
    }

    @Test
    void shouldDeliverThrownEventToWaitingClient() {
        when(requestContext.getableManager().iterateOverPrefix(wfRunId.toString() + "/", WorkflowEventModel.class))
                .thenReturn(List.of(mockEvent));
        TestStreamObserver<WorkflowEvent> clientObserver1 = new TestStreamObserver<>();
        asyncWaiters.registerObserverWaitingForWorkflowEvent(mockRequest, clientObserver1, requestContext);
        asyncWaiters.registerWorkflowEventHappened(mockEvent);
        Assertions.assertThat(clientObserver1.getValues()).hasSize(1).allMatch(Objects::nonNull);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
    }

    @Test
    void shouldDeliverThrownEventToANewlyRegisteredClient() {
        when(requestContext.getableManager().iterateOverPrefix(wfRunId.toString() + "/", WorkflowEventModel.class))
                .thenReturn(List.of(mockEvent));
        TestStreamObserver<WorkflowEvent> clientObserver1 = new TestStreamObserver<>();

        asyncWaiters.registerWorkflowEventHappened(mockEvent);
        asyncWaiters.registerObserverWaitingForWorkflowEvent(mockRequest, clientObserver1, requestContext);
        Assertions.assertThat(clientObserver1.getValues()).hasSize(1).allMatch(Objects::nonNull);
        Assertions.assertThat(clientObserver1.isCompleted()).isTrue();
    }
}

package io.littlehorse.server.streams.util;

import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AsyncWaitersTest {

    private final AsyncWaiters asyncWaiters = new AsyncWaiters();
    private final TenantIdModel tenantId = new TenantIdModel("test");

    @Test
    void shouldCompleteAllRegisteredFuturesAndReturnWorkflowEvents()
            throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowEventDefIdModel event1 = new WorkflowEventDefIdModel("123");
        WorkflowEventDefIdModel event2 = new WorkflowEventDefIdModel("456");
        WorkflowEventDefIdModel event3 = new WorkflowEventDefIdModel("789");
        WfRunIdModel wfRunId = new WfRunIdModel("123");
        CompletableFuture<WorkflowEvent>[] orRegisterFuture =
                asyncWaiters.getOrRegisterFuture(tenantId, wfRunId, event1, event2, event3);
        asyncWaiters.completeEvent(tenantId, wfRunId, event1, Mockito.mock(WorkflowEvent.class, "event1"));
        asyncWaiters.completeEvent(tenantId, wfRunId, event2, Mockito.mock(WorkflowEvent.class, "event2"));
        asyncWaiters.completeEvent(tenantId, wfRunId, event3, Mockito.mock(WorkflowEvent.class, "event3"));
        CompletableFuture.allOf(orRegisterFuture).get(1, TimeUnit.MILLISECONDS);
        boolean allCompleted = Arrays.stream(orRegisterFuture).allMatch(CompletableFuture::isDone);
        Assertions.assertThat(allCompleted).isTrue();
        List<WorkflowEvent> result =
                Arrays.stream(orRegisterFuture).map(CompletableFuture::join).toList();
        Assertions.assertThat(result).isNotEmpty().hasSize(3);
    }
}

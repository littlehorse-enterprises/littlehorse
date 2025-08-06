package io.littlehorse.server.streams.util;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AsyncWaitersTest {
    private final Duration removeCompletedAfter = Duration.ofMillis(2);
    private final AsyncWaiters asyncWaiters = new AsyncWaiters(removeCompletedAfter);
    private final TenantIdModel tenantId = new TenantIdModel("test");

    @Test
    public void shouldRemoveFutureWhenCompleted() throws Exception {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<Message> futureResponse =
                asyncWaiters.getOrRegisterFuture(commandId, Message.class, new CompletableFuture<>());
        futureResponse.complete(Empty.newBuilder().build());
        TimeUnit.MILLISECONDS.sleep(10);
        CompletableFuture<Message> futureResponse2 =
                asyncWaiters.getOrRegisterFuture(commandId, Message.class, new CompletableFuture<>());
        Assertions.assertThat(futureResponse2).isNotSameAs(futureResponse).isNotCompleted();
    }
}

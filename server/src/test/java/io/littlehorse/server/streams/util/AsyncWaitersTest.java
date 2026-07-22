package io.littlehorse.server.streams.util;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;

class AsyncWaitersTest {
    private final Duration removeCompletedAfter = Duration.ofMillis(2);
    private final AsyncWaiters asyncWaiters = new AsyncWaiters(removeCompletedAfter);

    @RetryingTest(maxAttempts = 3, suspendForMs = 1000)
    public void shouldRemoveFutureWhenCompleted() throws Exception {
        String commandId = UUID.randomUUID().toString();
        CompletableFuture<Message> futureResponse =
                asyncWaiters.getOrRegisterFuture(commandId, Message.class, new CompletableFuture<>());
        futureResponse.complete(Empty.newBuilder().build());
        CompletableFuture<Message> futureResponse2 = futureResponse;
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (futureResponse2 == futureResponse && System.nanoTime() < deadlineNanos) {
            TimeUnit.MILLISECONDS.sleep(100);
            futureResponse2 = asyncWaiters.getOrRegisterFuture(commandId, Message.class, new CompletableFuture<>());
        }
        Assertions.assertThat(futureResponse2).isNotSameAs(futureResponse).isNotCompleted();
    }
}

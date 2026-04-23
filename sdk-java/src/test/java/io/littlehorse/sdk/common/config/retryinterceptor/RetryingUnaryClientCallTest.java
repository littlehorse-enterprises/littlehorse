package io.littlehorse.sdk.common.config.retryinterceptor;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.CallOptions;
import io.grpc.Deadline;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RetryingUnaryClientCallTest {

    @Test
    void shouldNotRetryWhenDeadlineWouldBeExceeded() {
        CallOptions callOptions = CallOptions.DEFAULT.withDeadline(Deadline.after(1, TimeUnit.SECONDS));
        RetryingUnaryClientCall<?, ?> call = new RetryingUnaryClientCall<>(null, callOptions, null, null);

        assertThat(call.canRetryWithinDeadline(2000)).isFalse();
    }

    @Test
    void shouldRetryWhenDeadlineHasSufficientTime() {
        CallOptions callOptions = CallOptions.DEFAULT.withDeadline(Deadline.after(5, TimeUnit.SECONDS));
        RetryingUnaryClientCall<?, ?> call = new RetryingUnaryClientCall<>(null, callOptions, null, null);

        assertThat(call.canRetryWithinDeadline(1000)).isTrue();
    }

    @Test
    void shouldRetryWhenNoDeadlineIsSet() {
        CallOptions callOptions = CallOptions.DEFAULT;
        RetryingUnaryClientCall<?, ?> call = new RetryingUnaryClientCall<>(null, callOptions, null, null);

        assertThat(call.canRetryWithinDeadline(5000)).isTrue();
    }

    @Test
    void shouldNotRetryWhenDeadlineAlreadyExpired() {
        CallOptions callOptions = CallOptions.DEFAULT.withDeadline(Deadline.after(-1, TimeUnit.SECONDS));
        RetryingUnaryClientCall<?, ?> call = new RetryingUnaryClientCall<>(null, callOptions, null, null);

        assertThat(call.canRetryWithinDeadline(1000)).isFalse();
    }
}

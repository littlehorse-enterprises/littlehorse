package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ExponentialBackoffRetryPolicyModel extends LHSerializable<ExponentialBackoffRetryPolicy> {

    private int baseIntervalMs;
    private long maxDelayMs;
    private int maxRetries;
    private float multiplier;

    @Override
    public Class<ExponentialBackoffRetryPolicy> getProtoBaseClass() {
        return ExponentialBackoffRetryPolicy.class;
    }

    @Override
    public ExponentialBackoffRetryPolicy.Builder toProto() {
        ExponentialBackoffRetryPolicy.Builder out = ExponentialBackoffRetryPolicy.newBuilder()
                .setBaseIntervalMs(baseIntervalMs)
                .setMaxDelayMs(maxDelayMs)
                .setMaxRetries(maxRetries)
                .setMultiplier(multiplier);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        ExponentialBackoffRetryPolicy p = (ExponentialBackoffRetryPolicy) proto;
        baseIntervalMs = p.getBaseIntervalMs();
        maxDelayMs = p.getMaxDelayMs();
        maxRetries = p.getMaxRetries();
        multiplier = p.getMultiplier();
    }

    /**
     * Calculates the delay for the next attempt.
     * @param attemptNumber is the attemptNumber, with `1` corresponding to the
     * first retry.
     * @return the delay in milliseconds before the next TaskAttempt should be scheduled.
     */
    private long doSomeMath(int attemptNumber) {
        if (attemptNumber <= 0 || attemptNumber > maxRetries) {
            throw new IllegalArgumentException("Invalid attempt number");
        }

        // Calculate the delay using exponential backoff with a multiplier
        double exponentialBackoff = Math.pow(multiplier, attemptNumber - 1);
        long delay = (long) (baseIntervalMs * exponentialBackoff);

        // Cap the delay to the maximum allowed delay
        return Math.min(delay, maxDelayMs);
    }

    /**
     * Takes in an attempt number (where zero is the first attempt, 1 is the first retry, etc)
     * and returns the delay in milliseconds before the next attempt should be scheduled. Returns
     * empty if the attempts have been exhausted.
     * @param attemptNumber is the zero-indexed attempt number.
     * @return the delay in milliseconds before the next attempt.
     */
    public Optional<Long> calculateDelayForNextAttempt(int attemptNumber) {
        return (attemptNumber > maxRetries) ? Optional.empty() : Optional.of(doSomeMath(attemptNumber));
    }
}

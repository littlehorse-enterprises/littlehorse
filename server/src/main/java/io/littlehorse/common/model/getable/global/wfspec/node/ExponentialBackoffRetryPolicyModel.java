package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class ExponentialBackoffRetryPolicyModel extends LHSerializable<ExponentialBackoffRetryPolicy> {

    private int baseIntervalMs;
    private long maxDelayMs;
    private int maxAttempts;
    private float multipler;

    @Override
    public Class<ExponentialBackoffRetryPolicy> getProtoBaseClass() {
        return ExponentialBackoffRetryPolicy.class;
    }

    @Override
    public ExponentialBackoffRetryPolicy.Builder toProto() {
        ExponentialBackoffRetryPolicy.Builder out = ExponentialBackoffRetryPolicy.newBuilder()
                .setBaseIntervalMs(baseIntervalMs)
                .setMaxDelayMs(maxDelayMs)
                .setMaxAttempts(maxAttempts)
                .setMultiplier(multipler);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        ExponentialBackoffRetryPolicy p = (ExponentialBackoffRetryPolicy) proto;
        baseIntervalMs = p.getBaseIntervalMs();
        maxDelayMs = p.getMaxDelayMs();
        maxAttempts = p.getMaxAttempts();
        multipler = p.getMultiplier();
    }

    /**
     * 
     * @param attemptNumber is the attemptNumber, with `1` corresponding to the
     * first retry.
     * @return the delay in milliseconds before the next TaskAttempt should be scheduled.
     */
    public long calculateDelayForNextAttempt(int attemptNumber) {

    }
}

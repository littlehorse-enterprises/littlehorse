package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Aggregator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class LatencyModel extends LHSerializable<Aggregator.Latency> {

    private Aggregator.StatusRange statusRange;

    @Override
    public Aggregator.Latency.Builder toProto() {
        return Aggregator.Latency.newBuilder().setStatusRange(statusRange);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Aggregator.Latency p = (Aggregator.Latency) proto;
        statusRange = p.getStatusRange();
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return Aggregator.Latency.class;
    }
}

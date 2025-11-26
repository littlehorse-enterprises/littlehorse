package io.littlehorse.common.model.getable.global.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.Aggregator;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;

public class AggregatorModel extends LHSerializable<Aggregator> {

    private CountModel count;
    private RatioModel ratio;
    private LatencyModel latency;
    private Duration windowLength;
    private Aggregator.TypeCase typeCase;

    @Override
    public Aggregator.Builder toProto() {
        Aggregator.Builder out = Aggregator.newBuilder();
        if (count != null) out.setCount(count.toProto());
        if (ratio != null) out.setRatio(ratio.toProto());
        if (latency != null) out.setLatency(latency.toProto());
        out.setWindowLength(com.google.protobuf.Duration.newBuilder().setSeconds(windowLength.getSeconds()));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Aggregator p = (Aggregator) proto;
        typeCase = p.getTypeCase();
        if (p.hasCount()) count = LHSerializable.fromProto(p.getCount(), CountModel.class, context);
        if (p.hasRatio()) ratio = LHSerializable.fromProto(p.getRatio(), RatioModel.class, context);
        if (p.hasLatency()) latency = LHSerializable.fromProto(p.getLatency(), LatencyModel.class, context);
        windowLength = Duration.ofSeconds(p.getWindowLength().getSeconds());
    }

    @Override
    public Class<Aggregator> getProtoBaseClass() {
        return Aggregator.class;
    }

    public Duration getWindowLength() {
        return windowLength;
    }

    public AggregationType getAggregationType() {
        return switch (typeCase) {
            case COUNT -> AggregationType.COUNT;
            case RATIO -> AggregationType.RATIO;
            case LATENCY -> AggregationType.LATENCY;
            default -> throw new IllegalStateException("Unexpected value: " + typeCase);
        };
    }
}

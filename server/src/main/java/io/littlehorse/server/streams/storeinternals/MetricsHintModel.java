package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.MetricsHint;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class MetricsHintModel extends Storeable<MetricsHint> {

    public static final String METRICS_HINT_KEY = "metrics_hint";

    private Timestamp lastProcessedTimestamp;

    public MetricsHintModel() {}

    public MetricsHintModel(Timestamp timestamp) {
        this.lastProcessedTimestamp = timestamp;
    }

    @Override
    public Class<MetricsHint> getProtoBaseClass() {
        return MetricsHint.class;
    }

    @Override
    public MetricsHint.Builder toProto() {
        return MetricsHint.newBuilder().setLastProcessedTimestamp(lastProcessedTimestamp);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        MetricsHint p = (MetricsHint) proto;
        this.lastProcessedTimestamp = p.getLastProcessedTimestamp();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.METRICS_HINT;
    }

    @Override
    public String getStoreKey() {
        return MetricsHintModel.METRICS_HINT_KEY;
    }
}

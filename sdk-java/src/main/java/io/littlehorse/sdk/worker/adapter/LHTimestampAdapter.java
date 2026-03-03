package io.littlehorse.sdk.worker.adapter;

import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHTimestampAdapter<T> implements LHTypeAdapter<T> {
    public abstract Timestamp toTimestamp(T src);

    public abstract T fromTimestamp(Timestamp src);

    public VariableType getVariableType() {
        return VariableType.TIMESTAMP;
    }
}

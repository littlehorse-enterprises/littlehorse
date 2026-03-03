package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHDoubleAdapter<T> implements LHTypeAdapter<T> {
    public abstract Double toDouble(T src);

    public abstract T fromDouble(Double src);

    public VariableType getVariableType() {
        return VariableType.DOUBLE;
    }
}

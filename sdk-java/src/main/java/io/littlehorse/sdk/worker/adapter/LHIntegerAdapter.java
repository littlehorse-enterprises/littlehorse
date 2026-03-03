package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHIntegerAdapter<T> implements LHTypeAdapter<T> {
    public abstract Integer toInteger(T src);

    public abstract T fromInteger(Integer src);

    public VariableType getVariableType() {
        return VariableType.INT;
    }
}

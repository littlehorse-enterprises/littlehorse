package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public abstract class LHBooleanAdapter<T> implements LHTypeAdapter<T> {
    public abstract Boolean toBoolean(T src);

    public abstract T fromBoolean(Boolean src);

    public VariableType getVariableType() {
        return VariableType.BOOL;
    }
}

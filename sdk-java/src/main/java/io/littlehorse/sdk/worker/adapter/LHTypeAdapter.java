package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

public interface LHTypeAdapter<T> {
    public Class<T> getTypeClass();

    public default VariableType getVariableType() {
        return VariableType.UNRECOGNIZED;
    }
}

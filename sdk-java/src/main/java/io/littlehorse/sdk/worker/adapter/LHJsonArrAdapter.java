package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#JSON_ARR} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHJsonArrAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into a JSON array string representation.
     *
     * @param src input Java value
     * @return serialized JSON array string
     */
    public abstract String toJsonArr(T src);

    /**
     * Converts a LittleHorse JSON array string into the target Java type.
     *
     * @param src runtime JSON array string
     * @return deserialized Java value
     */
    public abstract T fromJsonArr(String src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#JSON_ARR}
     */
    public VariableType getVariableType() {
        return VariableType.JSON_ARR;
    }
}

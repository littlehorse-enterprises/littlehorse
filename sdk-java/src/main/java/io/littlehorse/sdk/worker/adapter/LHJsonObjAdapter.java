package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#JSON_OBJ} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHJsonObjAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into a JSON object string representation.
     *
     * @param src input Java value
     * @return serialized JSON object string
     */
    public abstract String toJsonObj(T src);

    /**
     * Converts a LittleHorse JSON object string into the target Java type.
     *
     * @param src runtime JSON object string
     * @return deserialized Java value
     */
    public abstract T fromJsonObj(String src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#JSON_OBJ}
     */
    public VariableType getVariableType() {
        return VariableType.JSON_OBJ;
    }
}

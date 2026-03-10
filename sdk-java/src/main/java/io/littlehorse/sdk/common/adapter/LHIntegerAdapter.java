package io.littlehorse.sdk.common.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse integer values via {@link Integer}.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHIntegerAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into its {@link Integer} representation.
     *
     * @param src input Java value
     * @return serialized integer value
     */
    public abstract Integer toInteger(T src);

    /**
     * Converts a LittleHorse integer value into the target Java type.
     *
     * @param src runtime integer value
     * @return deserialized Java value
     */
    public abstract T fromInteger(Integer src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#INT}
     */
    public VariableType getVariableType() {
        return VariableType.INT;
    }
}

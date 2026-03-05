package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#DOUBLE} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHDoubleAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into its {@link Double} representation.
     *
     * @param src input Java value
     * @return serialized double value
     */
    public abstract Double toDouble(T src);

    /**
     * Converts a LittleHorse double value into the target Java type.
     *
     * @param src runtime double value
     * @return deserialized Java value
     */
    public abstract T fromDouble(Double src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#DOUBLE}
     */
    public VariableType getVariableType() {
        return VariableType.DOUBLE;
    }
}

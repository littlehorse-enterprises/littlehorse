package io.littlehorse.sdk.common.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#BOOL} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHBooleanAdapter<T> implements LHTypeAdapter<T> {

    /** Protected no-arg constructor for subclassing. */
    protected LHBooleanAdapter() {}

    /**
     * Converts a Java value into its {@link Boolean} representation.
     *
     * @param src input Java value
     * @return serialized boolean value
     */
    public abstract Boolean toBoolean(T src);

    /**
     * Converts a LittleHorse boolean value into the target Java type.
     *
     * @param src runtime boolean value
     * @return deserialized Java value
     */
    public abstract T fromBoolean(Boolean src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#BOOL}
     */
    public VariableType getVariableType() {
        return VariableType.BOOL;
    }
}

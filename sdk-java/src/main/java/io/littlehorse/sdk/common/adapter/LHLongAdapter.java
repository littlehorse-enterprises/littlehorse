package io.littlehorse.sdk.common.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse integer values via {@link Long}.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHLongAdapter<T> implements LHTypeAdapter<T> {

    /** Protected no-arg constructor for subclassing. */
    protected LHLongAdapter() {}

    /**
     * Converts a Java value into its {@link Long} representation.
     *
     * @param src input Java value
     * @return serialized long value
     */
    public abstract Long toLong(T src);

    /**
     * Converts a LittleHorse integer value into the target Java type.
     *
     * @param src runtime long value
     * @return deserialized Java value
     */
    public abstract T fromLong(Long src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#INT}
     */
    public VariableType getVariableType() {
        return VariableType.INT;
    }
}

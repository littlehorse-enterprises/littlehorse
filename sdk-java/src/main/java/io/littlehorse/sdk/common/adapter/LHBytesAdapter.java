package io.littlehorse.sdk.common.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#BYTES} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHBytesAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into its byte-array representation.
     *
     * @param src input Java value
     * @return serialized bytes value
     */
    public abstract byte[] toBytes(T src);

    /**
     * Converts a LittleHorse bytes value into the target Java type.
     *
     * @param src runtime bytes value
     * @return deserialized Java value
     */
    public abstract T fromBytes(byte[] src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#BYTES}
     */
    public VariableType getVariableType() {
        return VariableType.BYTES;
    }
}

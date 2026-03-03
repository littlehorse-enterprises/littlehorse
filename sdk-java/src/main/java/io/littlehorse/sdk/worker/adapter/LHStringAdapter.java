package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter for Java types represented as LittleHorse {@link VariableType#STR} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public interface LHStringAdapter<T> extends LHTypeAdapter<T> {

    /**
     * Converts a Java value into its string representation for LittleHorse runtime storage.
     *
     * @param src input Java value
     * @return serialized string value
     */
    public String toString(T src);

    /**
     * Converts a LittleHorse string value back into the target Java type.
     *
     * @param src runtime string value
     * @return deserialized Java value
     */
    public T fromString(String src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#STR}
     */
    public default VariableType getVariableType() {
        return VariableType.STR;
    }
}

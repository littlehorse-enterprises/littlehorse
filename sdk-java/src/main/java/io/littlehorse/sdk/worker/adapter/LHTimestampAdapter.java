package io.littlehorse.sdk.worker.adapter;

import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#TIMESTAMP} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHTimestampAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into a protobuf {@link Timestamp}.
     *
     * @param src input Java value
     * @return serialized timestamp value
     */
    public abstract Timestamp toTimestamp(T src);

    /**
     * Converts a LittleHorse timestamp value into the target Java type.
     *
     * @param src runtime timestamp value
     * @return deserialized Java value
     */
    public abstract T fromTimestamp(Timestamp src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#TIMESTAMP}
     */
    public VariableType getVariableType() {
        return VariableType.TIMESTAMP;
    }
}

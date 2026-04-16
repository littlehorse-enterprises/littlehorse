package io.littlehorse.sdk.common.adapter;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * Base interface for custom Java type adapters used by the LittleHorse Java SDK.
 *
 * <p>An adapter tells the SDK how a user-defined Java type maps to one of LittleHorse's supported
 * runtime {@link VariableType} values (for example, mapping {@code UUID} to {@code STR}).
 * Implementations are typically one of the specialized adapter types such as
 * {@link LHStringAdapter}, {@link LHLongAdapter}, or {@link LHTimestampAdapter}.
 *
 * @param <T> the Java type handled by this adapter
 */
public interface LHTypeAdapter<T> {

    /**
     * Returns the Java class that this adapter supports. Implemented by the user.
     *
     * @return the Java type class for this adapter
     */
    public Class<T> getTypeClass();

    /**
     * Returns the LittleHorse variable type used as the wire/runtime representation.
     *
     * <p>Most implementations should override this, typically via a specialized adapter base class.
     *
     * @return the mapped LittleHorse {@link VariableType}
     */
    public default VariableType getVariableType() {
        return VariableType.UNRECOGNIZED;
    }
}

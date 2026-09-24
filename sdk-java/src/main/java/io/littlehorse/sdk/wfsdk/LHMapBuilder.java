package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * Builder for creating native Map values inside a workflow specification.
 *
 * <p>An {@code LHMapBuilder} is {@link Serializable} so it can be passed as a task
 * argument, assigned to a variable, or used anywhere a {@link Serializable} is expected.
 *
 * <p>This is the Map analog of {@link LHStructBuilder}, and permits dynamic
 * (runtime-resolved) keys and values, which a literal Map cannot express.
 */
public interface LHMapBuilder extends Serializable {

    /**
     * Adds a key/value entry to the map being built.
     *
     * <p>Both key and value can be literals, workflow variable references,
     * expressions, or any other {@link Serializable} that the SDK understands.
     * Keys must resolve to a primitive type at runtime.
     *
     * @param key the map key (literal, variable reference, or expression)
     * @param value the map value
     * @return this builder
     */
    LHMapBuilder put(Serializable key, Serializable value);
}

package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * Builder for creating named Struct values inside a workflow specification.
 *
 * <p>An {@code LHStructBuilder} is {@link Serializable} so it can be passed as a task
 * argument, assigned to a variable, or used anywhere a {@link Serializable} is expected.
 */
public interface LHStructBuilder extends Serializable {

    /**
     * Adds or replaces a field value.
     * @param fieldName the Struct field name
     * @param value the field value, workflow reference, expression, or literal
     * @return this builder
     */
    LHStructBuilder put(String fieldName, Serializable value);

    /**
     * Adds or replaces a field with a nested inline Struct value.
     * @param fieldName the Struct field name
     * @param nested a nested inline Struct builder
     * @return this builder
     */
    LHStructBuilder put(String fieldName, InlineLHStructBuilder nested);
}

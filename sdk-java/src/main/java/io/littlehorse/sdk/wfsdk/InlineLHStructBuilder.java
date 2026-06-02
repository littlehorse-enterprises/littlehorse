package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * Builder for creating nested inline Struct values inside a workflow specification.
 *
 * <p>Unlike {@link LHStructBuilder}, an {@code InlineLHStructBuilder} is <b>not</b>
 * {@link Serializable} and therefore cannot be passed as a task argument, assigned to a
 * variable, or used anywhere a {@link Serializable} is expected. It can only be nested
 * inside another builder via {@link LHStructBuilder#put(String, InlineLHStructBuilder)}
 * or {@link InlineLHStructBuilder#put(String, InlineLHStructBuilder)}.
 */
public interface InlineLHStructBuilder {

    /**
     * Adds or replaces a field value.
     * @param fieldName the Struct field name
     * @param value the field value, workflow reference, expression, or literal
     * @return this builder
     */
    InlineLHStructBuilder put(String fieldName, Serializable value);

    /**
     * Adds or replaces a field with a nested inline Struct value.
     * @param fieldName the Struct field name
     * @param nested a nested inline Struct builder
     * @return this builder
     */
    InlineLHStructBuilder put(String fieldName, InlineLHStructBuilder nested);
}

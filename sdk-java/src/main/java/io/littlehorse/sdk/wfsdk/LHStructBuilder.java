package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * Builder for creating Struct values inside a workflow specification.
 */
public interface LHStructBuilder extends Serializable {

    /**
     * Adds or replaces a field value.
     * @param fieldName the Struct field name
     * @param value the field value, workflow reference, expression, or nested builder
     * @return this builder
     */
    LHStructBuilder put(String fieldName, Serializable value);

    /**
     * Pins the StructDef version used by this builder.
     * @param version concrete StructDef version
     * @return this builder
     */
    LHStructBuilder withVersion(int version);
}

package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for adding metadata based on the target type.
 * <p>
 * This annotation can be applied to either a method or a method parameter:
 * <p>
 * - Method: When applied to a method, the metadata will be added to a {@code NodeOutput}.
 * - Method Parameter: When applied to a method parameter, the metadata will be added to a {@code VariableDef}.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHType {

    /**
     * Indicates whether the value should be masked.
     *
     * @return true if the value should be masked; false otherwise.
     */
    boolean masked() default false;

    /**
     * Optional display name override for the target variable or node output.
     *
     * @return configured name override
     */
    String name() default "";

    /**
     * Indicates the StructDef name used when the annotated type is an InlineStruct.
     *
     * @return the StructDef name expected for InlineStruct values.
     */
    String structDefName() default "";

    /**
     * Indicates the StructDef version used when the annotated type is an InlineStruct.
     *
     * When structDefName is set, this version will be used to determine which version of the StructDef to use. If unset, the latest version of the StructDef will be used.
     * @return
     */
    int structDefVersion() default -1;

    /**
     * Indicates whether or not Array types marked with this annotation should be serialized as a LittleHorse native Array.
     *
     * @return whether or not this Array should be a LittleHorse native Array or a JSON_ARR.
     */
    boolean isLHArray() default false;
}

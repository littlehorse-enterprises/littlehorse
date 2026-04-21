package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining metadata on fields and accessors of a StructDef field.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHStructField {
    /**
     * OPTIONAL: This allows you to give the StructDef Field a specific name.
     *
     * Defaults to method name, but this overrides the default.
     *
     * @return the name of the StructDef.
     */
    String name() default "";

    /**
     * OPTIONAL: This allows you to set the StructDef Field as masked, ensuring
     *           the value stays hidden from users.
     *
     * @return whether or not the type value should be masked.
     */
    boolean masked() default false;

    /**
     * OPTIONAL: Indicates that this array-typed field should be serialized as a LittleHorse native Array
     * rather than a JSON_ARR.
     *
     * @return whether or not this field's array should be an LH native Array.
     */
    boolean isLHArray() default false;

    /**
     * OPTIONAL: Indicates that this field is nullable, meaning its value may be set to null.
     *
     * @return whether or not this field is nullable.
     */
    boolean isNullable() default false;
}

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
    boolean masked();

    String name() default "";
}

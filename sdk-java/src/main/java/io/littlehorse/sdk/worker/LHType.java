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
 *
 * <p>Usage example in a method:
 * <pre>{@code
 *     @LHTaskMethod("greet")
 *     @LHType(masked = true)
 *     public String greeting(@LHType(masked = true) String name) {
 *         log.debug("Executing task greet");
 *         return "hello there, " + name;
 *     }
 * }</pre>
 * In this example, the parameter {@code name} is marked as {@code @LHType(masked = true)},
 * which means the value of {@code name} will be masked.
 * The return value of the method is also marked as {@code @LHType(masked = true)},
 * which means the return value will also be masked.
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

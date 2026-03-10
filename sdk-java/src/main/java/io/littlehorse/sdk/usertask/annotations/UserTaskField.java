package io.littlehorse.sdk.usertask.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields exposed in a user task form.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface UserTaskField {
    /**
     * Optional field description for user interfaces.
     *
     * @return field description
     */
    String description() default "";

    /**
     * Optional user-facing display name.
     *
     * @return display name override
     */
    String displayName() default "";

    /**
     * Indicates whether this field must be provided before task completion.
     *
     * @return true when required
     */
    boolean required() default true;
}

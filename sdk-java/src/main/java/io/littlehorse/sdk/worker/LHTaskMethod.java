package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that the annotated method should be used as the method to execute a
 * Task in the LH Java Task Worker library.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHTaskMethod {

    String description() default "";
    /**
     * This is the value of the annotation; it corresponds to the name of the TaskDef executed by
     * the annotated Method.
     *
     * @return the taskdef name.
     */
    String value();
}

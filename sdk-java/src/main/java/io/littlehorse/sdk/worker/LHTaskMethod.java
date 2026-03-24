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

    /**
     * Optional TaskDef description to publish when registering this task.
     *
     * @return task description
     */
    String description() default "";
    /**
     * This is the value of the annotation; it corresponds to the name of the TaskDef executed by
     * the annotated Method.
     *
     * @return the taskdef name.
     */
    String value();

    /**
     * This allows you to opt-in to serializing Array types as LittleHorse native Arrays instead of
     * JSON_ARRs. Using LittleHorse native Arrays ensures Array type-safety before running workflows.
     * @return whether or not Array types returned by this task method should be serialized as LittleHorse Arrays.
     */
    boolean returnsLHArray() default false;
}

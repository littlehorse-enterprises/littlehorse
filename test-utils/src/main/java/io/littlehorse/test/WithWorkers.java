package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link WithWorkers} annotation defines a way to start multiple workers for test classes
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableWithWorkers.class)
public @interface WithWorkers {
    /**
     * Method name that returns the worker class
     * @return public method name
     */
    String value();

    /**
     * Identifies the {@link io.littlehorse.sdk.worker.LHTaskMethod} to be started
     * @return {@link io.littlehorse.sdk.worker.LHTaskMethod} names
     */
    String[] lhMethods() default {};
}

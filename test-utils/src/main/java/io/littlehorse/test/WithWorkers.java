package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableWithWorkers.class)
public @interface WithWorkers {

    String value();

    String[] lhMethods() default {};
}

package io.littlehorse.jlib.usertask.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface UserTaskField {
    String description() default "";

    String displayName() default "";
}

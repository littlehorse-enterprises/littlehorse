package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
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
}

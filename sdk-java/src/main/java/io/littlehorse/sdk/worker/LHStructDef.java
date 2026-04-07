package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that the annotated method should be used as the method to execute a
 * Task in the LH Java Task Worker library.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHStructDef {
    /**
     * This corresponds to the name of the StructDef.
     *
     * @return the StructDef name.
     */
    String value();

    /**
     * Human-readable description for the generated StructDef.
     *
     * @return StructDef description
     */
    String description() default "";

    /**
     * Optional version number for the StructDef.
     *
     * When unset, the version will default to -1, which indicates that the latest version will be used.
     * @return StructDef version
     */
    int version() default -1;
}

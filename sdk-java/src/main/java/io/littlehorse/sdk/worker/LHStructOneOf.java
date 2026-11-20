package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares mutually exclusive Struct alternatives. On a property, {@link #group()}
 * names its group. On a helper type, omit the group: the containing property's
 * serialized name names the group and its fields are flattened into the parent.
 * Alternatives must be reference types without defaults or schema nullability.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface LHStructOneOf {
    String group() default "";
}

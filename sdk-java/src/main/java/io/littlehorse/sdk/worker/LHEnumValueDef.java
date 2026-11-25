package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the backing value of a LittleHorse Enum member.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LHEnumValueDef {
    /**
     * The backing value of this member. The value must be 0 or more.
     *
     * @return the backing value of this member
     */
    int value();
}

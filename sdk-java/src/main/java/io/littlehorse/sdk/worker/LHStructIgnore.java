package io.littlehorse.sdk.worker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with {@code @LHStructIgnore} and their properties 
 * will be excluded from StructDefs generated using this annotation.
 * 
 * This annotation can be applied to getter/setter methods of a StructDef class.
 * 
 * @see java.lang.annotation.ElementType
 * @see java.lang.annotation.RetentionPolicy
 * @see java.lang.annotation.Target
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHStructIgnore {}

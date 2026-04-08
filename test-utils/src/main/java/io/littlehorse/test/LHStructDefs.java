package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers StructDefs based on the list of classes returned by the annotated field.
 *
 * The field should be a {@code List<Class<?>>} where each class is a StructDef class.
 *
 * StructDefs will be registered in order, so if you have StructDefs that reference each other, make sure to order them appropriately in the list. For example, if you have a Person struct that references a Car struct, you should list Car before Person in the list of classes.
 *
 * <blockquote>
 * <pre>
 * {@code
 * @LHStructDefs
 * private {@literal List<Class<?>>} structClasses = List.of(Car.class, Person.class);
 * }
 * </pre>
 * </blockquote>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHStructDefs {}

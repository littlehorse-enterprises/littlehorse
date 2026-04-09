package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a class annotated with
 * {@link io.littlehorse.test.LHTest} should have the specified StructDefs
 * registered on the LH server before tests are run.
 *
 * This will register StructDefs in the order they are specified in the
 * annotation.
 *
 * This doesn't allow for StructDef evolution testing since the
 * StructDefs are only registered once before all tests are run. For tests that
 * require more fine-grained control over when StructDefs are registered, test
 * authors can use the client API to register StructDefs directly within their
 * test methods.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithStructDefs {
    Class<?>[] value();
}

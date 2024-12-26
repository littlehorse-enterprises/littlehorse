package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @LHTest} identifies test classes and methods that run a Littlehorse workflow.
 * This annotation will spin up a Littlehorse cluster using the "bootstrapper.class" configuration.
 * It can be used at test class level or method level:
 * <blockquote><pre>
 *  {@code @LHTest}
 *  class MyTest {
 *      {@code @LHTest}
 *      void myTestMethod() {
 *      }
 *  }
 * </pre></blockquote>
 *
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LHExtension.class)
public @interface LHTest {

    /**
     * An array of external events that will be registered during bootstrapping.
     */
    String[] externalEventNames() default {};
}

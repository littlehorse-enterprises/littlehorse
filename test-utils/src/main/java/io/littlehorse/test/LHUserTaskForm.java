package io.littlehorse.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register a {@link io.littlehorse.sdk.common.proto.UserTaskDef} from a class
 *  <blockquote><pre>
 *  {@code @LHUserTaskForm}
 *   public final MyForm form = new MyForm();
 *  </pre></blockquote>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHUserTaskForm {

    String value();
}

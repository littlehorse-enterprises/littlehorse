package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link LHWorkflowEvent} annotation identifies a {@link PutWorkflowEventDefRequest} to be registered.
 *  <blockquote><pre>
 *  {@code @LHWorkflowEvent}
 *   public final PutWorkflowEventDefRequest eventDef = PutWorkflowEventDefRequest.newBuilder()
 *     .setType(VariableType.STR)
 *     .setName("my-workflow-event")
 *     .build();
 *  </pre></blockquote>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LHWorkflowEvent {}

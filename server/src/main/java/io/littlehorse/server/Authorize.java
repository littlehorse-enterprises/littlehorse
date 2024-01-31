package io.littlehorse.server;

import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Marks a Grpc service endpoint with specific ACL resources and actions.
 *
 *  Required ACLs will be verified by {@link io.littlehorse.server.auth.RequestAuthorizer}.
 *
 *  If this annotation is not present, {@link io.littlehorse.server.auth.RequestAuthorizer} takes
 *  ACLResource.ACL_ALL_RESOURCES and ACLAction.ALL_ACTIONS as default
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Authorize {

    /**
     * Specific set of actions required over the specific resources
     */
    ACLAction[] actions();

    /**
     * Specific set of resources required on the service method
     */
    ACLResource[] resources();
}

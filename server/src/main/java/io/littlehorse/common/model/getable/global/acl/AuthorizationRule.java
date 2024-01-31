package io.littlehorse.common.model.getable.global.acl;

import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class AuthorizationRule {

    private ACLResource resource;
    private ACLAction action;

    public AuthorizationRule(final ACLResource resource, final ACLAction action) {
        this.action = action;
        this.resource = resource;
    }

    public static AuthorizationRule of(final ACLResource resource, final ACLAction action) {
        return new AuthorizationRule(resource, action);
    }
}

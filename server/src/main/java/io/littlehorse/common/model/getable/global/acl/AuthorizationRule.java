package io.littlehorse.common.model.getable.global.acl;

import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationRule that = (AuthorizationRule) o;
        return resource == that.resource && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, action);
    }

    public ACLResource getResource() {
        return this.resource;
    }

    public ACLAction getAction() {
        return this.action;
    }
}

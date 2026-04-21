package io.littlehorse.common.model.getable.global.acl;

import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;

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
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorizationRule)) return false;
        final AuthorizationRule other = (AuthorizationRule) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$resource = this.getResource();
        final Object other$resource = other.getResource();
        if (this$resource == null ? other$resource != null : !this$resource.equals(other$resource)) return false;
        final Object this$action = this.getAction();
        final Object other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorizationRule;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $resource = this.getResource();
        result = result * PRIME + ($resource == null ? 43 : $resource.hashCode());
        final Object $action = this.getAction();
        result = result * PRIME + ($action == null ? 43 : $action.hashCode());
        return result;
    }

    public ACLResource getResource() {
        return this.resource;
    }

    public ACLAction getAction() {
        return this.action;
    }
}

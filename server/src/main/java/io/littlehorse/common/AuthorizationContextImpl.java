package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class AuthorizationContextImpl implements AuthorizationContext {

    private final String authorizedTenant;

    private final String authorizedPrincipalId;
    private final List<ServerACLModel> acls;
    private final boolean isAdmin;

    public AuthorizationContextImpl(
            final String authorizedPrincipalId,
            final String authorizedTenant,
            final List<ServerACLModel> acls,
            final boolean isAdmin) {
        this.authorizedTenant = Objects.requireNonNull(authorizedTenant);
        this.authorizedPrincipalId = Objects.requireNonNull(authorizedPrincipalId);
        this.acls = Objects.requireNonNull(acls);
        this.isAdmin = isAdmin;
    }

    @Override
    public String principalId() {
        return authorizedPrincipalId;
    }

    @Override
    public String tenantId() {
        return authorizedTenant;
    }

    @Override
    public Collection<ServerACLModel> acls() {
        return acls;
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }
}

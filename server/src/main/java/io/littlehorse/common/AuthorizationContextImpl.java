package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class AuthorizationContextImpl implements AuthorizationContext {

    private final TenantIdModel authorizedTenant;

    private final PrincipalIdModel authorizedPrincipalId;
    private final List<ServerACLModel> acls;
    private final boolean isAdmin;

    public AuthorizationContextImpl(
            final PrincipalIdModel authorizedPrincipalId,
            final TenantIdModel authorizedTenant,
            final List<ServerACLModel> acls,
            final boolean isAdmin) {
        this.authorizedTenant = Objects.requireNonNull(authorizedTenant);
        this.authorizedPrincipalId = Objects.requireNonNull(authorizedPrincipalId);
        this.acls = Objects.requireNonNull(acls);
        this.isAdmin = isAdmin;
    }

    @Override
    public PrincipalIdModel principalId() {
        return authorizedPrincipalId;
    }

    @Override
    public TenantIdModel tenantId() {
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

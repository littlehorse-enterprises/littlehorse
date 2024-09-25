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
    private final List<ServerACLModel> globalAcls;
    private final List<ServerACLModel> perTenantAcls;
    private final boolean isAdmin;

    public AuthorizationContextImpl(
            final PrincipalIdModel authorizedPrincipalId,
            final TenantIdModel authorizedTenant,
            final List<ServerACLModel> globalAcls,
            final List<ServerACLModel> perTenantAcls,
            final boolean isAdmin) {
        this.authorizedTenant = Objects.requireNonNull(authorizedTenant);
        this.authorizedPrincipalId = Objects.requireNonNull(authorizedPrincipalId);
        this.globalAcls = Objects.requireNonNull(globalAcls);
        this.perTenantAcls = Objects.requireNonNull(perTenantAcls);
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
    public Collection<ServerACLModel> globalAcls() {
        return globalAcls;
    }

    @Override
    public Collection<ServerACLModel> perTenantAcls() {
        return perTenantAcls;
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }
}

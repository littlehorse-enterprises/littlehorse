package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import java.util.Collection;
import java.util.List;

public final class AuthorizationContextImpl implements AuthorizationContext {

    private final String authorizedTenant;

    private final String authorizedPrincipalId;
    private final List<ServerACLModel> acls;

    public AuthorizationContextImpl(
            final String authorizedPrincipalId, final String authorizedTenant, final List<ServerACLModel> acls) {
        this.authorizedTenant = authorizedTenant;
        this.authorizedPrincipalId = authorizedPrincipalId;
        this.acls = acls;
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
}

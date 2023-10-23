package io.littlehorse.common;

import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import java.util.Optional;

public final class ServerContextImpl implements ServerContext {

    private final String tenantId;

    private final ServerContext.Scope scope;

    public ServerContextImpl(final String tenantId, final ServerContext.Scope scope) {
        this.tenantId = tenantId;
        this.scope = scope;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public Optional<PrincipalModel> principal() {
        return null;
    }

    @Override
    public String tenantId() {
        return tenantId;
    }
}

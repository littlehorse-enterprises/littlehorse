package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.List;

public class BackgroundContext implements ExecutionContext {

    @Override
    public AuthorizationContext authorization() {
        return new AuthorizationContextImpl(
                new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL),
                new TenantIdModel(LHConstants.DEFAULT_TENANT),
                List.of(),
                true);
    }

    @Override
    public WfService service() {
        return null;
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return null;
    }

    @Override
    public LHServerConfig serverConfig() {
        return null;
    }
}

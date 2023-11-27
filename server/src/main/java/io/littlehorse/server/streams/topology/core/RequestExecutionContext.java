package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class RequestExecutionContext implements ExecutionContext {

    private final AuthorizationContext authorization;
    private final ReadOnlyModelStore coreStore;
    private final ReadOnlyModelStore globalStore;
    private final ReadOnlyGetableManager readOnlyGetableManager;
    private final ReadOnlyMetadataManager metadataManager;
    private String clientId;
    private String tenantId;
    private final WfService service;
    private final LHServerConfig lhConfig;

    public RequestExecutionContext(
            String clientId,
            String tenantId,
            ReadOnlyKeyValueStore<String, Bytes> globalMetadataNativeStore,
            ReadOnlyKeyValueStore<String, Bytes> coreNativeStore,
            MetadataCache metadataCache,
            LHServerConfig lhConfig) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.coreStore = resolveStore(coreNativeStore, tenantId);
        this.globalStore = resolveStore(globalMetadataNativeStore, tenantId);
        this.readOnlyGetableManager = new ReadOnlyGetableManager(this.coreStore);
        this.metadataManager = new ReadOnlyMetadataManager(
                ModelStore.defaultStore(globalMetadataNativeStore, this),
                ModelStore.tenantStoreFor(globalMetadataNativeStore, tenantId, this));
        this.service = new WfService(this.metadataManager, metadataCache, this);
        this.authorization = authContextFor(clientId, tenantId);
        this.lhConfig = lhConfig;
    }

    private ReadOnlyModelStore resolveStore(ReadOnlyKeyValueStore<String, Bytes> nativeStore, String tenantId) {

        // Principal and Tenants are stored in the default store
        return ModelStore.instanceFor(nativeStore, tenantId, this);
    }

    public ReadOnlyGetableManager getableManager() {
        return readOnlyGetableManager;
    }

    private PrincipalModel resolvePrincipal(String clientId, String tenantId) {
        if (clientId != null && tenantId != null) {
            PrincipalModel storedPrincipal = metadataManager.get(new PrincipalIdModel(clientId));
            if (storedPrincipal == null) {
                return service.getPrincipal(null);
            }
            return storedPrincipal;
        } else if (clientId != null) {
            PrincipalModel storedPrincipal = metadataManager.get(new PrincipalIdModel(clientId));
            if (storedPrincipal == null) {
                return service.getPrincipal(null);
            }
            return storedPrincipal;
        } else {
            return service.getPrincipal(null);
        }
    }

    @Override
    public WfService service() {
        return service;
    }

    @Override
    public AuthorizationContext authorization() {
        return authorization;
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return lhConfig;
    }

    private AuthorizationContext authContextFor(String clientId, String tenantId) {
        PrincipalModel resolvedPrincipal = resolvePrincipal(clientId, tenantId);
        if (tenantId == null) {
            tenantId = LHConstants.DEFAULT_TENANT;
        }
        List<ServerACLModel> currentAcls;
        if (resolvedPrincipal.getPerTenantAcls().containsKey(tenantId)) {
            currentAcls = resolvedPrincipal.getPerTenantAcls().get(tenantId).getAcls();
        } else {
            currentAcls = resolvedPrincipal.getGlobalAcls().getAcls();
        }
        return new AuthorizationContextImpl(
                resolvedPrincipal.getId(), tenantId, currentAcls, resolvedPrincipal.isAdmin());
    }
}

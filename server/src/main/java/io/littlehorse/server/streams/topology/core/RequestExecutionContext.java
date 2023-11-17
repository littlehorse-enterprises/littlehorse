package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.List;

public class RequestExecutionContext implements ExecutionContext {

    private final AuthorizationContext authorization;
    private final ReadOnlyModelStore store;
    private final ReadOnlyGetableManager readOnlyGetableManager;
    private String clientId;
    private String tenantId;
    private final WfService service;

    public RequestExecutionContext(String clientId, String tenantId, ReadOnlyKeyValueStore<String, Bytes> nativeStore, MetadataCache metadataCache) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.store = resolveStore(nativeStore);
        this.readOnlyGetableManager = new ReadOnlyGetableManager(this.store);
        this.service = new WfService(this.store, metadataCache, readOnlyGetableManager);
        this.authorization = authContextFor(clientId, tenantId);
    }

    private ReadOnlyModelStore resolveStore(ReadOnlyKeyValueStore<String, Bytes> nativeStore){
        // Tenants are stored in the default store
        return ModelStore.instanceFor(nativeStore, ModelStore.DEFAULT_TENANT, this);
    }

    public ReadOnlyGetableManager getableManager(){
        return readOnlyGetableManager;
    }

    private PrincipalModel resolvePrincipal(String clientId, String tenantId) {
        if (clientId != null && tenantId != null) {
            PrincipalModel storedPrincipal = readOnlyGetableManager.get(new PrincipalIdModel(clientId));
            if (storedPrincipal == null) {
                return service.getPrincipal(null);
            }
            return storedPrincipal;
        } else if (clientId != null) {
            PrincipalModel storedPrincipal = readOnlyGetableManager.get(new PrincipalIdModel(clientId));
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

    private AuthorizationContext authContextFor(String clientId, String tenantId){
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
        return new AuthorizationContextImpl(resolvedPrincipal.getId(), tenantId, currentAcls, resolvedPrincipal.isAdmin());
    }

    @Override
    public AuthorizationContext authorization() {
        return authorization;
    }

    @Override
    public ReadOnlyModelStore store() {
        return this.store;
    }


}

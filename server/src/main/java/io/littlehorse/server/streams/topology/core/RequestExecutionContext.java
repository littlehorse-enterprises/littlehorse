package io.littlehorse.server.streams.topology.core;

import io.grpc.Context;
import io.grpc.Deadline;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.ServerACLModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.auth.PermissionDeniedException;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.rocksdb.RocksDB;

public class RequestExecutionContext implements ExecutionContext {

    private AuthorizationContext authorization;
    private final ReadOnlyGetableManager readOnlyGetableManager;
    private final ReadOnlyMetadataManager metadataManager;
    private final WfService service;
    private final LHServerConfig lhConfig;
    private final CoreStoreProvider coreStoreProvider;
    private final boolean isRequestingClusterScopedResource;
    private final RocksDB db;

    public RequestExecutionContext(
            PrincipalIdModel clientId,
            TenantIdModel tenantId,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            LHServerConfig lhConfig,
            boolean isRequestingClusterScopedResource,
            RocksDB db) {
        this.db = db;
        if (tenantId == null) {
            tenantId = new TenantIdModel(LHConstants.DEFAULT_TENANT);
        }
        if (clientId == null) {
            clientId = new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL);
        }
        this.coreStoreProvider = coreStoreProvider;
        this.isRequestingClusterScopedResource = isRequestingClusterScopedResource;

        ReadOnlyKeyValueStore<String, Bytes> nativeGlobalStore = coreStoreProvider.getNativeGlobalStore();
        ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore = coreStoreProvider.nativeCoreStore();
        ReadOnlyClusterScopedStore clusterMetadataStore =
                ReadOnlyClusterScopedStore.newInstance(/*nativeGlobalStore,*/ this, db);
        ReadOnlyTenantScopedStore tenantMetadataStore =
                ReadOnlyTenantScopedStore.newInstance(/*nativeGlobalStore, */ tenantId, this, db);

        ReadOnlyTenantScopedStore tenantCoreStore =
                ReadOnlyTenantScopedStore.newInstance(/*nativeCoreStore, */ tenantId, this, db);

        this.readOnlyGetableManager = new ReadOnlyGetableManager(tenantCoreStore);
        this.metadataManager = new ReadOnlyMetadataManager(clusterMetadataStore, tenantMetadataStore, metadataCache);
        this.service = new WfService(this.metadataManager, metadataCache, this);
        if (!tenantId.getId().equals(LHConstants.DEFAULT_TENANT)) {
            TenantModel storedTenant = metadataManager.get(tenantId);
            if (storedTenant == null) {
                throw new PermissionDeniedException("Tenant not allowed");
            }
        }
        this.authorization = authContextFor(clientId, tenantId);
        this.lhConfig = lhConfig;
    }

    public ReadOnlyGetableManager getableManager() {
        return readOnlyGetableManager;
    }

    public ReadOnlyGetableManager getableManager(TaskId streamsTaskId) {
        ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore =
                coreStoreProvider.nativeCoreStore(streamsTaskId.partition());
        ReadOnlyTenantScopedStore tenantCoreStore =
                ReadOnlyTenantScopedStore.newInstance(/*nativeCoreStore, */ authorization.tenantId(), this, db);
        return new ReadOnlyGetableManager(tenantCoreStore, streamsTaskId);
    }

    private PrincipalModel resolvePrincipal(PrincipalIdModel clientId) {
        if (clientId != null) {
            PrincipalModel storedPrincipal = metadataManager.get(clientId);
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

    private AuthorizationContext authContextFor(PrincipalIdModel clientId, TenantIdModel tenantId) {
        PrincipalModel resolvedPrincipal = resolvePrincipal(clientId);
        List<ServerACLModel> acls = new ArrayList<>();
        if (resolvedPrincipal.getPerTenantAcls().containsKey(tenantId.toString())) {
            if (this.isRequestingClusterScopedResource) {
                throw new PermissionDeniedException(String.format(
                        "Current Principal %s has per-tenant ACLs for Tenant %s, but is trying to access a cluster-scoped resource. Cluster-scoped resource access requires global ACLs.",
                        clientId.getId(), tenantId.getId()));
            }

            acls.addAll(resolvedPrincipal
                    .getPerTenantAcls()
                    .get(tenantId.toString())
                    .getAcls());
        } else {
            acls.addAll(resolvedPrincipal.getGlobalAcls().getAcls());
        }
        return new AuthorizationContextImpl(resolvedPrincipal.getId(), tenantId, acls, resolvedPrincipal.isAdmin());
    }

    public Optional<Deadline> getDeadlineFromClient() {
        return Optional.ofNullable(Context.current().getDeadline());
    }
}

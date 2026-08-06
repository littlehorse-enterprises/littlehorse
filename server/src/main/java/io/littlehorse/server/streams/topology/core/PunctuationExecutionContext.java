package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class PunctuationExecutionContext extends BackgroundContext {

    private final long timestamp;
    private final ReadOnlyClusterScopedStore clusterMetadataStore;
    private final ReadOnlyTenantScopedStore tenantMetadataStore;
    private final TenantScopedStore coreStore;
    private final ReadOnlyMetadataManager metadataManager;
    private final LHServerConfig lhConfig;
    private final AuthorizationContext auth;

    public PunctuationExecutionContext(
            long timestamp,
            LHServerConfig lhConfig,
            ReadOnlyKeyValueStore<String, Bytes> readOnlyMetadataNativeStore,
            KeyValueStore<String, Bytes> nativeCoreStore,
            MetadataCache metadataCache,
            TenantIdModel tenant) {
        this.timestamp = timestamp;
        this.clusterMetadataStore = ReadOnlyClusterScopedStore.newInstance(readOnlyMetadataNativeStore, this);
        this.tenantMetadataStore = ReadOnlyTenantScopedStore.newInstance(readOnlyMetadataNativeStore, tenant, this);
        this.metadataManager = new ReadOnlyMetadataManager(clusterMetadataStore, tenantMetadataStore, metadataCache);
        this.coreStore = TenantScopedStore.newInstance(nativeCoreStore, tenant, this);
        this.lhConfig = lhConfig;
        this.auth = new AuthorizationContextImpl(
                new PrincipalIdModel(LHConstants.ANONYMOUS_PRINCIPAL), tenant, List.of(), true);
    }

    public PunctuationExecutionContext(
            long timestamp,
            LHServerConfig lhConfig,
            ReadOnlyKeyValueStore<String, Bytes> readOnlyMetadataNativeStore,
            KeyValueStore<String, Bytes> nativeCoreStore,
            MetadataCache metadataCache) {
        this(
                timestamp,
                lhConfig,
                readOnlyMetadataNativeStore,
                nativeCoreStore,
                metadataCache,
                new TenantIdModel(LHConstants.DEFAULT_TENANT));
    }

    public long timestamp() {
        return timestamp;
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return lhConfig;
    }

    public ReadOnlyClusterScopedStore metadataStore() {
        return clusterMetadataStore;
    }

    public TenantScopedStore coreStore() {
        return coreStore;
    }
}

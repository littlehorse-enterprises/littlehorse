package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class RepartitionExecutionContext implements ExecutionContext {

    private final LHServerConfig lhConfig;
    private final ProcessorContext<Void, Void> repartitionContext;
    private final MetadataCache metadataCache;
    private final ReadOnlyMetadataManager metadataManager;

    public RepartitionExecutionContext(
            Headers recordHeaders,
            LHServerConfig lhConfig,
            ProcessorContext<Void, Void> repartitionContext,
            MetadataCache metadataCache) {

        this.repartitionContext = repartitionContext;

        ReadOnlyKeyValueStore<String, Bytes> nativeGlobalStore = nativeGlobalStore();
        TenantIdModel tenantId = HeadersUtil.tenantIdFromMetadata(recordHeaders);
        ReadOnlyClusterScopedStore clusterMetadataStore =
                ReadOnlyClusterScopedStore.newInstance(nativeGlobalStore, this);
        ReadOnlyTenantScopedStore tenantMetadataStore =
                ReadOnlyTenantScopedStore.newInstance(nativeGlobalStore, tenantId, this);
        this.metadataManager = new ReadOnlyMetadataManager(clusterMetadataStore, tenantMetadataStore);

        this.lhConfig = lhConfig;
        this.metadataCache = metadataCache;
    }

    @Override
    public AuthorizationContext authorization() {
        return null;
    }

    @Override
    public WfService service() {
        return new WfService(metadataManager, metadataCache, this);
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return lhConfig;
    }

    private ReadOnlyKeyValueStore<String, Bytes> nativeGlobalStore() {
        return repartitionContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
    }
}

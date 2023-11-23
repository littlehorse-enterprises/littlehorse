package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class RepartitionExecutionContext implements ExecutionContext {

    private final LHServerConfig lhConfig;
    private final ProcessorContext<Void, Void> repartitionContext;
    private final MetadataCache metadataCache;
    private final ReadOnlyModelStore globalStore;
    private final ReadOnlyMetadataManager metadataManager;

    public RepartitionExecutionContext(
            Headers metadataHeaders,
            LHServerConfig lhConfig,
            ProcessorContext<Void, Void> repartitionContext,
            MetadataCache metadataCache) {
        this.lhConfig = lhConfig;
        this.repartitionContext = repartitionContext;
        this.metadataCache = metadataCache;
        this.globalStore =
                ModelStore.instanceFor(nativeGlobalStore(), HeadersUtil.tenantIdFromMetadata(metadataHeaders), this);
        this.metadataManager = new ReadOnlyMetadataManager(globalStore);
    }

    @Override
    public AuthorizationContext authorization() {
        return null;
    }

    @Override
    public WfService service() {
        return new WfService(null, globalStore, metadataCache, null);
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return metadataManager;
    }

    @Override
    public LHServerConfig serverConfig() {
        return lhConfig;
    }

    private KeyValueStore<String, Bytes> nativeGlobalStore() {
        return repartitionContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE);
    }
}

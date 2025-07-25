package io.littlehorse.server;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.mockito.Mockito;

@Getter
public class TestCoreProcessorContext extends CoreProcessorContext {

    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;
    private final TaskQueueManager globalTaskQueueManager;
    private final TenantScopedStore coreStore;
    private final TenantScopedStore tenantMetadataStore;
    private final ClusterScopedStore clusterMetadataStore;
    private final Headers recordMetadata;
    private final LHServer server;
    private GetableManager getableManager;

    public TestCoreProcessorContext(
            Command currentCommand,
            Headers recordMetadata,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager globalTaskQueueManager,
            MetadataCache metadataCache,
            LHServer server) {
        super(currentCommand, recordMetadata, config, processorContext, globalTaskQueueManager, metadataCache, server);
        this.metadataCache = metadataCache;
        this.recordMetadata = recordMetadata;
        this.lhConfig = config;
        this.globalTaskQueueManager = globalTaskQueueManager;

        TenantIdModel tenantId = HeadersUtil.tenantIdFromMetadata(recordMetadata);
        this.server = server;

        this.coreStore = Mockito.spy(TenantScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.CORE_STORE), tenantId, this));
        this.tenantMetadataStore = Mockito.spy(TenantScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE), tenantId, this));
        this.clusterMetadataStore = Mockito.spy(ClusterScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE), this));
        clusterMetadataStore.put(new StoredGetable<>(new TenantModel(tenantId)));
        getableManager = Mockito.spy(super.getableManager());
    }

    public static TestCoreProcessorContext create(
            Command currentCommand,
            Headers recordMetadata,
            MockProcessorContext<String, CommandProcessorOutput> processorContext) {
        LHServerConfig lhConfig = Mockito.mock();
        TaskQueueManager globalTaskQueueManager = Mockito.mock();
        MetadataCache metadataCache = new MetadataCache();
        LHServer server = Mockito.mock();
        KeyValueStore<String, Bytes> nativeMetadataStore = Mockito.spy(Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled()
                .build());
        KeyValueStore<String, Bytes> nativeCoreStore = Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(ServerTopology.CORE_STORE), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled()
                .build();
        KeyValueStore<String, Bytes> nativeGlobalStore = Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(ServerTopology.GLOBAL_METADATA_STORE),
                        Serdes.String(),
                        Serdes.Bytes())
                .withLoggingDisabled()
                .build();
        nativeMetadataStore.init(processorContext.getStateStoreContext(), nativeMetadataStore);
        nativeCoreStore.init(processorContext.getStateStoreContext(), nativeCoreStore);
        nativeGlobalStore.init(processorContext.getStateStoreContext(), nativeGlobalStore);
        return new TestCoreProcessorContext(
                currentCommand,
                recordMetadata,
                lhConfig,
                processorContext,
                globalTaskQueueManager,
                metadataCache,
                server);
    }

    @Override
    public GetableManager getableManager() {
        return getableManager;
    }

    @Override
    public MetadataManager metadataManager() {
        return new MetadataManager(clusterMetadataStore, tenantMetadataStore, metadataCache);
    }
}

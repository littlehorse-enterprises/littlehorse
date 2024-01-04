package io.littlehorse.server;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
public class TestProcessorExecutionContext extends ProcessorExecutionContext {

    private final MetadataCache metadataCache;
    private final LHServerConfig lhConfig;
    private final TaskQueueManager globalTaskQueueManager;
    private final TenantScopedStore coreStore;
    private final ReadOnlyTenantScopedStore tenantMetadataStore;
    private final ReadOnlyClusterScopedStore clusterMetadataStore;
    private final Headers recordMetadata;

    public TestProcessorExecutionContext(
            Command currentCommand,
            Headers recordMetadata,
            LHServerConfig config,
            ProcessorContext<String, CommandProcessorOutput> processorContext,
            TaskQueueManager globalTaskQueueManager,
            MetadataCache metadataCache,
            KafkaStreamsServerImpl server) {
        super(currentCommand, recordMetadata, config, processorContext, globalTaskQueueManager, metadataCache, server);
        this.metadataCache = metadataCache;
        this.recordMetadata = recordMetadata;
        this.lhConfig = config;
        this.globalTaskQueueManager = globalTaskQueueManager;

        String tenantId = HeadersUtil.tenantIdFromMetadata(recordMetadata);

        this.coreStore = TenantScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.CORE_STORE), tenantId, this);
        this.tenantMetadataStore = ReadOnlyTenantScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.METADATA_STORE), tenantId, this);
        this.clusterMetadataStore = ReadOnlyClusterScopedStore.newInstance(
                processorContext.getStateStore(ServerTopology.METADATA_STORE), this);
    }

    public static TestProcessorExecutionContext create(
            Command currentCommand,
            Headers recordMetadata,
            MockProcessorContext<String, CommandProcessorOutput> processorContext) {
        LHServerConfig lhConfig = Mockito.mock();
        TaskQueueManager globalTaskQueueManager = Mockito.mock();
        MetadataCache metadataCache = new MetadataCache();
        KafkaStreamsServerImpl server = Mockito.mock();
        KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
                .withLoggingDisabled()
                .build();
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
        return new TestProcessorExecutionContext(
                currentCommand,
                recordMetadata,
                lhConfig,
                processorContext,
                globalTaskQueueManager,
                metadataCache,
                server);
    }

    @Override
    public ReadOnlyMetadataManager metadataManager() {
        return new ReadOnlyMetadataManager(clusterMetadataStore, tenantMetadataStore);
    }
}

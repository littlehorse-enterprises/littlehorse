package io.littlehorse.server;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.Command;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.DefaultModelStore;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
    private final DefaultModelStore metadataStore;
    private final DefaultModelStore globalMetadataStore;
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
        metadataStore = ModelStore.defaultStore(processorContext.getStateStore(ServerTopology.METADATA_STORE), this);
        this.globalMetadataStore =
                ModelStore.defaultStore(processorContext.getStateStore(ServerTopology.GLOBAL_METADATA_STORE), this);
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
    public MetadataManager metadataManager() {
        return new MetadataManager(metadataStore, null);
    }

    public MetadataManager globalMetadataManager() {
        return new MetadataManager(globalMetadataStore, null);
    }
}

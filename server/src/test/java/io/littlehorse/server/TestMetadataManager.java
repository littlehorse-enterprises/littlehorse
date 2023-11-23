package io.littlehorse.server;

import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class TestMetadataManager extends MetadataManager {

    public TestMetadataManager(ModelStore store) {
        super(store);
    }

    public static TestMetadataManager create(
            ProcessorContext<String, Bytes> ctx, ProcessorExecutionContext processorContext) {
        KeyValueStore<String, Bytes> nativeGlobalStore = ctx.getStateStore(ServerTopology.METADATA_STORE);
        ModelStore globalStore = ModelStore.defaultStore(nativeGlobalStore, processorContext);
        return new TestMetadataManager(globalStore);
    }
}

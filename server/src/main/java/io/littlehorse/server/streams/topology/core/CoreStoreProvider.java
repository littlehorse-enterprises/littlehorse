package io.littlehorse.server.streams.topology.core;

import io.littlehorse.server.streams.ServerTopology;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class CoreStoreProvider {
    private final KafkaStreams serverInstance;
    private static final boolean ENABLE_STALE_STORES = true;

    public CoreStoreProvider(final KafkaStreams serverInstance) {
        this.serverInstance = serverInstance;
    }

    public ReadOnlyKeyValueStore<String, Bytes> getNativeGlobalStore() {
        return getStore(ServerTopology.GLOBAL_METADATA_STORE, null);
    }

    public ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore() {
        return getStore(ServerTopology.CORE_STORE, null);
    }

    public ReadOnlyKeyValueStore<String, Bytes> nativeCoreStore(int specificPartition) {
        return getStore(ServerTopology.CORE_STORE, specificPartition);
    }

    private ReadOnlyKeyValueStore<String, Bytes> getStore(final String storeName, Integer specificPartition) {
        final boolean instanceIsRunning = serverInstance.state() == KafkaStreams.State.RUNNING;
        if (instanceIsRunning) {
            StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

            if (ENABLE_STALE_STORES) {
                params = params.enableStaleStores();
            }

            if (specificPartition != null) {
                params = params.withPartition(specificPartition);
            }

            return serverInstance.store(params);
        } else {
            return null;
        }
    }
}

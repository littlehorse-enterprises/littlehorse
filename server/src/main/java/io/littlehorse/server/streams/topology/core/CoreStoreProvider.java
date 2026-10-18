package io.littlehorse.server.streams.topology.core;

import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
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
        // Kafka Streams only serves stores while it is RUNNING or REBALANCING. Once it starts
        // shutting down, store() throws an IllegalStateException with a full stack trace, and the
        // per-partition background workers keep polling until the processor is closed, so that
        // exception lands in the log on every tick. Report the store as transiently unavailable
        // instead: that is what it is, and every caller already handles it.
        final KafkaStreams.State state = serverInstance.state();
        if (state != KafkaStreams.State.RUNNING && state != KafkaStreams.State.REBALANCING) {
            throw new LHApiException(Status.UNAVAILABLE, "Kafka Streams is not running: " + state);
        }

        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

        if (ENABLE_STALE_STORES) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        ReadOnlyKeyValueStore<String, Bytes> result = serverInstance.store(params);
        if (result == null) {
            throw new LHApiException(Status.UNAVAILABLE, "Kafka Streams is not ready yet");
        }
        return result;
    }
}

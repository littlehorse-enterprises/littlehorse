package io.littlehorse.common.dao;

import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ReadOnlyLHStore;
import io.littlehorse.server.streams.topology.core.ReadOnlyMetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public final class DAOFactory {

    private final KafkaStreams streamsInstance;
    private final MetadataCache metadataCache;

    public DAOFactory(final KafkaStreams streamsInstance, final MetadataCache metadataCache) {
        this.streamsInstance = streamsInstance;
        this.metadataCache = metadataCache;
    }

    public ReadOnlyMetadataProcessorDAO getMetadataDao(
            int specificPartition, boolean enableStaleStores, String tenantId) {
        ReadOnlyKeyValueStore<String, Bytes> nativeStore =
                readOnlyStore(specificPartition, enableStaleStores, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataProcessorDAOImpl(ReadOnlyLHStore.instanceFor(nativeStore, tenantId), metadataCache);
    }

    public ReadOnlyMetadataProcessorDAO getMetadataDao(boolean enableStaleStores, String tenantId) {
        ReadOnlyKeyValueStore<String, Bytes> nativeStore =
                readOnlyStore(null, enableStaleStores, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataProcessorDAOImpl(ReadOnlyLHStore.instanceFor(nativeStore, tenantId), metadataCache);
    }

    public ReadOnlyKeyValueStore<String, Bytes> readOnlyStore(
            Integer specificPartition, boolean enableStaleStores, String storeName) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

        if (enableStaleStores) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return streamsInstance.store(params);
    }
}

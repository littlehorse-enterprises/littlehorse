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

public class ServerDAOFactory implements DAOFactory {

    private final KafkaStreams streamsInstance;
    private final MetadataCache metadataCache;

    private static final boolean ENABLE_STALE_STORES = true;

    public ServerDAOFactory(final KafkaStreams streamsInstance, final MetadataCache metadataCache) {
        this.streamsInstance = streamsInstance;
        this.metadataCache = metadataCache;
    }

    @Override
    public ReadOnlyMetadataProcessorDAO getMetadataDao(String tenantId) {
        ReadOnlyKeyValueStore<String, Bytes> nativeStore = readOnlyStore(null, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataProcessorDAOImpl(
                ReadOnlyLHStore.instanceFor(nativeStore, tenantId), metadataCache, null);
    }

    private ReadOnlyKeyValueStore<String, Bytes> readOnlyStore(Integer specificPartition, String storeName) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

        if (ENABLE_STALE_STORES) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return streamsInstance.store(params);
    }
}

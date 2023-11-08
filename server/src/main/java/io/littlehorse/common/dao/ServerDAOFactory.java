package io.littlehorse.common.dao;

import static io.littlehorse.server.auth.ServerAuthorizer.AUTH_CONTEXT;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.ReadOnlyMetadataProcessorDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class ServerDAOFactory {

    private final KafkaStreams streamsInstance;
    private final MetadataCache metadataCache;

    private static final boolean ENABLE_STALE_STORES = true;

    public ServerDAOFactory(final KafkaStreams streamsInstance, final MetadataCache metadataCache) {
        this.streamsInstance = streamsInstance;
        this.metadataCache = metadataCache;
    }

    public ReadOnlyMetadataProcessorDAO getMetadataDao() {
        final String tenantId = AUTH_CONTEXT.get().tenantId();
        return getMetadataDao(tenantId);
    }

    public ReadOnlyMetadataProcessorDAO getMetadataDao(String tenantId) {
        ReadOnlyKeyValueStore<String, Bytes> allPartitionNativeStore =
                readOnlyStore(null, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataProcessorDAOImpl(
                ModelStore.instanceFor(allPartitionNativeStore, tenantId), metadataCache, contextFor(tenantId));
    }

    public ReadOnlyMetadataProcessorDAO getDefaultMetadataDao() {
        ReadOnlyKeyValueStore<String, Bytes> allPartitionNativeStore =
                readOnlyStore(null, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataProcessorDAOImpl(
                ModelStore.defaultStore(allPartitionNativeStore), metadataCache, contextFor(ModelStore.DEFAULT_TENANT));
    }

    private AuthorizationContext contextFor(String tenantId) {
        return new AuthorizationContextImpl(null, tenantId, AuthorizationContext.Scope.READ, List.of());
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

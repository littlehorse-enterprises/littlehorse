package io.littlehorse.common.dao;

import static io.littlehorse.server.auth.ServerAuthorizer.AUTH_CONTEXT;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHConstants;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.ReadOnlyMetadataDAOImpl;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.List;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

/**
 * This class is intended to be used only for the scope of a GRPC request
 */
public class ServerDAOFactory {

    private final KafkaStreams streamsInstance;
    private final MetadataCache metadataCache;

    private static final boolean ENABLE_STALE_STORES = false;

    public ServerDAOFactory(final KafkaStreams streamsInstance, final MetadataCache metadataCache) {
        this.streamsInstance = streamsInstance;
        this.metadataCache = metadataCache;
    }

    /**
     * Gets a{@link ReadOnlyMetadataDAO} instance based on the current authorized GRPC Request.
     * This DAO will use Global Metadata Store
     */
    public ReadOnlyMetadataDAO getMetadataDao() {
        final String tenantId = AUTH_CONTEXT.get().tenantId();
        final String principalId = AUTH_CONTEXT.get().principalId();
        ReadOnlyKeyValueStore<String, Bytes> allPartitionNativeStore =
                readOnlyStore(null, ServerTopology.GLOBAL_METADATA_STORE);
        return new ReadOnlyMetadataDAOImpl(
                ModelStore.instanceFor(allPartitionNativeStore, tenantId),
                metadataCache,
                contextFor(tenantId, principalId));
    }

    public ReadOnlyMetadataDAO getDefaultMetadataDao() {
        ReadOnlyKeyValueStore<String, Bytes> allPartitionNativeStore =
                readOnlyStore(null, ServerTopology.METADATA_STORE);
        return new ReadOnlyMetadataDAOImpl(
                ModelStore.defaultStore(allPartitionNativeStore),
                metadataCache,
                contextFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL));
    }

    private AuthorizationContext contextFor(String tenantId, String principalId) {
        return new AuthorizationContextImpl(principalId, tenantId, List.of());
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

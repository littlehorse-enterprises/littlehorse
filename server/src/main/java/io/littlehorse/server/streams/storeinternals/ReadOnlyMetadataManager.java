package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ReadOnlyClusterScopedStore;
import io.littlehorse.server.streams.stores.ReadOnlyTenantScopedStore;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadOnlyMetadataManager {

    protected final Map<String, GetableToStore<?, ?>> uncommittedChanges = new TreeMap<>();
    private final ReadOnlyClusterScopedStore clusterStore;
    private final ReadOnlyTenantScopedStore tenantStore;

    public ReadOnlyMetadataManager(
            final ReadOnlyClusterScopedStore clusterStore,
            final ReadOnlyTenantScopedStore tenantStore,
            final MetadataCache metadataCache) {
        this.clusterStore = clusterStore;
        this.tenantStore = tenantStore;
        this.clusterStore.enableCache(metadataCache);
        this.tenantStore.enableCache(metadataCache);
    }

    public <U extends Message, T extends MetadataGetable<U>> T get(MetadataId<?, U, T> id) {
        log.trace("Getting {} with key {}", id.getType(), id);
        T out = null;
        // First check the cache.
        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferedResult = (GetableToStore<U, T>) uncommittedChanges.get(id.getStoreableKey());
        if (bufferedResult != null) {
            return bufferedResult.getObjectToStore();
        }

        // Next check the store.
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) tenantStore.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) return null;

        // If we got here, that means that:
        // 1. The Getable exists in the store, and
        // 2. This is the first time in this txn (eg. Command Processing) that
        // we are getting the
        out = storeResult.getStoredObject();

        uncommittedChanges.put(
                id.getStoreableKey(),
                new GetableToStore<>(storeResult.getStoredObject(), storeResult, id.getObjectClass()));
        return out;
    }

    public <U extends Message, T extends MetadataGetable<U>, EX extends Throwable> T getOrThrow(
            MetadataId<?, U, T> id, Supplier<EX> throwable) throws EX {
        T result = get(id);
        if (result == null) {
            throw throwable.get();
        }
        return result;
    }

    public <U extends Message, T extends ClusterMetadataGetable<U>> T get(ClusterMetadataId<?, U, T> id) {
        log.trace("Getting {} with key {}", id.getType(), id);
        T out = null;
        // First check the cache.
        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferedResult = (GetableToStore<U, T>) uncommittedChanges.get(id.getStoreableKey());
        if (bufferedResult != null) {
            return bufferedResult.getObjectToStore();
        }

        // Next check the store.
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) clusterStore.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) return null;

        // If we got here, that means that:
        // 1. The Getable exists in the store, and
        // 2. This is the first time in this txn (eg. Command Processing) that
        // we are getting the
        out = storeResult.getStoredObject();

        uncommittedChanges.put(
                id.getStoreableKey(),
                new GetableToStore<>(storeResult.getStoredObject(), storeResult, id.getObjectClass()));
        return out;
    }

    public <U extends Message, T extends ClusterMetadataGetable<U>, EX extends Throwable> T getOrThrow(
            ClusterMetadataId<?, U, T> id, Supplier<EX> throwable) throws EX {
        T result = get(id);
        if (result == null) {
            throw throwable.get();
        }
        return result;
    }

    public List<Tag> clusterScopedTagScan(GetableClassEnum objectType, List<Attribute> attributes) {
        String startKey = Tag.getAttributeString(objectType, attributes);
        String endKey = startKey + "~";

        try (LHKeyValueIterator<Tag> rangeResult = clusterStore.range(startKey, endKey, Tag.class)) {
            final List<Tag> result = new ArrayList<>();
            rangeResult.forEachRemaining(tagLHIterKeyValue -> {
                result.add(tagLHIterKeyValue.getValue());
            });
            return result;
        }
    }

    /**
     * Note that this method acts over the TENANT scope.
     */
    public <U extends Message, T extends MetadataGetable<U>> T getLastFromPrefix(String prefix, Class<T> clazz) {
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) tenantStore.getLastFromPrefix(prefix, StoredGetable.class);
        if (storeResult == null) return null;
        return storeResult.getStoredObject();
    }

    protected final boolean isClusterLevelObject(ObjectIdModel<?, ?, ?> objectId) {
        return objectId instanceof PrincipalIdModel || objectId instanceof TenantIdModel;
    }
}

package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.MetadataGetable;
import io.littlehorse.common.model.getable.ClusterMetadataId;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.util.MetadataCache;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataManager extends ReadOnlyMetadataManager {

    private ClusterScopedStore clusterStore;
    private TenantScopedStore tenantStore;
    private TenantScopedGetableCallback callback;

    public MetadataManager(
            ClusterScopedStore clusterStore, TenantScopedStore tenantStore, MetadataCache metadataCache) {
        this(clusterStore, tenantStore, metadataCache, null);
    }

    public MetadataManager(
            ClusterScopedStore clusterStore,
            TenantScopedStore tenantStore,
            MetadataCache metadataCache,
            TenantScopedGetableCallback callback) {
        super(clusterStore, tenantStore, metadataCache);
        this.clusterStore = clusterStore;
        this.tenantStore = tenantStore;
        this.callback = callback;
    }

    public <U extends Message, T extends ClusterMetadataGetable<U>> void put(T getable) {
        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        StoredGetable<?, ?> old = clusterStore.get(getable.getObjectId().getStoreableKey(), StoredGetable.class);
        if (old != null) {
            log.trace("removing tags for metadata getable {}", getable.getObjectId());
            for (String tagId : old.getIndexCache().getTagIds()) {
                clusterStore.delete(tagId, StoreableType.TAG);
            }
        }

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        clusterStore.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            clusterStore.put(tag);
        }
    }

    public <U extends Message, T extends MetadataGetable<U>> void put(T getable) {
        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        StoredGetable<?, ?> old = tenantStore.get(getable.getObjectId().toString(), StoredGetable.class);
        if (old != null) {
            log.trace("removing tags for metadata getable {}", getable.getObjectId());
            for (String tagId : old.getIndexCache().getTagIds()) {
                tenantStore.delete(tagId, StoreableType.TAG);
            }
        }

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        tenantStore.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            tenantStore.put(tag);
        }

        if (this.callback != null) {
            callback.observe(getable);
        }
    }

    public <U extends Message, T extends MetadataGetable<U>> void delete(MetadataId<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = tenantStore.get(id.getStoreableKey(), StoredGetable.class);
        log.trace("trying to delete " + id.getStoreableKey());

        if (storeResult == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find provided " + id.getObjectClass().getSimpleName());
        }

        tenantStore.delete(id.getStoreableKey(), StoreableType.STORED_GETABLE);

        // Now delete all the tags
        for (String tagId : storeResult.getIndexCache().getTagIds()) {
            tenantStore.delete(tagId, StoreableType.TAG);
        }
    }

    public <U extends Message, T extends ClusterMetadataGetable<U>> void delete(ClusterMetadataId<?, U, T> id) {
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = clusterStore.get(id.getStoreableKey(), StoredGetable.class);
        log.trace("trying to delete " + id.getStoreableKey());

        if (storeResult == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find provided " + id.getObjectClass().getSimpleName());
        }

        clusterStore.delete(id.getStoreableKey(), StoreableType.STORED_GETABLE);

        // Now delete all the tags
        for (String tagId : storeResult.getIndexCache().getTagIds()) {
            clusterStore.delete(tagId, StoreableType.TAG);
        }
    }
}

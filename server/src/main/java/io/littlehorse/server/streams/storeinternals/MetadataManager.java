package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.DefaultModelStore;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.store.TenantModelStore;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataManager extends ReadOnlyMetadataManager {

    private ModelStore defaultStore;
    private ModelStore tenantStore;

    public MetadataManager(DefaultModelStore defaultStore, TenantModelStore tenantStore) {
        super(defaultStore, tenantStore);
        this.defaultStore = defaultStore;
        this.tenantStore = tenantStore != null ? tenantStore : defaultStore;
    }

    public <U extends Message, T extends GlobalGetable<U>> void put(T getable) {
        ModelStore specificStore = isClusterLevelObject(getable.getObjectId()) || tenantStore == null ? defaultStore : tenantStore;
        // The cast is necessary to tell the store that the ObjectId belongs to a
        // GlobalGetable.
        @SuppressWarnings("unchecked")
        StoredGetable<?, ?> old = specificStore.get((ObjectIdModel<?, U, T>) getable.getObjectId());
        if (old != null) {
            log.trace("removing tags for metadata getable {}", getable.getObjectId());
            for (String tagId : old.getIndexCache().getTagIds()) {
                specificStore.delete(tagId, StoreableType.TAG);
            }
        }

        StoredGetable<U, T> toStore = new StoredGetable<U, T>(getable);
        specificStore.put(toStore);
        for (Tag tag : getable.getIndexEntries()) {
            specificStore.put(tag);
        }
    }

    public <U extends Message, T extends GlobalGetable<U>> void delete(ObjectIdModel<?, U, T> id) {
        ModelStore specificStore = isClusterLevelObject(id) || tenantStore == null ? defaultStore : tenantStore;
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult = specificStore.get(id.getStoreableKey(), StoredGetable.class);
        log.trace("trying to delete " + id.getStoreableKey());

        if (storeResult == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find provided " + id.getObjectClass().getSimpleName());
        }

        specificStore.delete(id.getStoreableKey(), StoreableType.STORED_GETABLE);

        // Now delete all the tags
        for (String tagId : storeResult.getIndexCache().getTagIds()) {
            specificStore.delete(tagId, StoreableType.TAG);
        }
    }
}

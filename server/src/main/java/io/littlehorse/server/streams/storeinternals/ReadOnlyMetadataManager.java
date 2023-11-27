package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyModelDefaultStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.store.ReadOnlyTenantStore;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadOnlyMetadataManager {

    protected final Map<String, GetableToStore<?, ?>> uncommittedChanges = new TreeMap<>();
    private final ReadOnlyModelStore defaultStore;
    private final ReadOnlyModelStore tenantStore;

    public ReadOnlyMetadataManager(
            final ReadOnlyModelDefaultStore defaultStore, final ReadOnlyTenantStore tenantStore) {
        this.defaultStore = defaultStore;
        this.tenantStore = tenantStore != null ? tenantStore : defaultStore;

    }

    public <U extends Message, T extends GlobalGetable<U>> T get(MetadataId<?, U, T> id) {
        log.trace("Getting {} with key {}", id.getType(), id);
        T out = null;
        ReadOnlyModelStore specificStore = isClusterLevelObject(id) ? defaultStore : tenantStore;

        // First check the cache.
        @SuppressWarnings("unchecked")
        GetableToStore<U, T> bufferedResult = (GetableToStore<U, T>) uncommittedChanges.get(id.getStoreableKey());
        if (bufferedResult != null) {
            return bufferedResult.getObjectToStore();
        }

        // Next check the store.
        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) specificStore.get(id.getStoreableKey(), StoredGetable.class);

        if (storeResult == null) return null;

        // If we got here, that means that:
        // 1. The Getable exists in the store, and
        // 2. This is the first time in this txn (eg. Command Processing) that
        // we are getting the
        out = storeResult.getStoredObject();

        uncommittedChanges.put(id.getStoreableKey(), new GetableToStore<>(storeResult, id.getObjectClass()));
        return out;
    }

    public List<Tag> tagScan(GetableClassEnum objectType, List<Attribute> attributes) {
        String tagAttributeString = Tag.getAttributeString(objectType, attributes);
        String startKey = "%s/" + tagAttributeString;
        String endKey = startKey + "~";
        LHKeyValueIterator<Tag> rangeResult = tenantStore.range(startKey, endKey, Tag.class);
        final List<Tag> result = new ArrayList<>();
        rangeResult.forEachRemaining(tagLHIterKeyValue -> {
            result.add(tagLHIterKeyValue.getValue());
        });
        return result;
    }

    public <U extends Message, T extends GlobalGetable<U>> T lastFromPrefix(String prefix) {
        StoredGetable<U, T> storeResult =
                (StoredGetable<U, T>) tenantStore.getLastFromPrefix(prefix, StoredGetable.class);
        if (storeResult == null) return null;
        return storeResult.getStoredObject();
    }

    protected final boolean isClusterLevelObject(ObjectIdModel<?, ?, ?> objectId) {
        return objectId instanceof PrincipalIdModel || objectId instanceof TenantIdModel;
    }
}

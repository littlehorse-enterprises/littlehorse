package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GlobalGetable;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
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
    private final ReadOnlyModelStore store;

    public ReadOnlyMetadataManager(final ReadOnlyModelStore store) {
        this.store = store;
    }

    public <U extends Message, T extends GlobalGetable<U>> T get(MetadataId<?, U, T> id) {
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
        StoredGetable<U, T> storeResult = (StoredGetable<U, T>) store.get(id.getStoreableKey(), StoredGetable.class);

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
        LHKeyValueIterator<Tag> rangeResult = store.range(startKey, endKey, Tag.class);
        final List<Tag> result = new ArrayList<>();
        rangeResult.forEachRemaining(tagLHIterKeyValue -> {
            result.add(tagLHIterKeyValue.getValue());
        });
        return result;
    }

    public <U extends Message, T extends GlobalGetable<U>> T lastFromPrefix(String prefix) {
        StoredGetable<U, T> storeResult = (StoredGetable<U, T>) store.getLastFromPrefix(prefix, StoredGetable.class);
        if (storeResult == null) return null;
        return storeResult.getStoredObject();
    }
}

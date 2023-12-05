package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.store.StoredGetable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadOnlyGetableManager {

    protected final Map<String, GetableToStore<?, ?>> uncommittedChanges = new TreeMap<>();
    private final ReadOnlyModelStore store;

    public ReadOnlyGetableManager(ReadOnlyModelStore store) {
        this.store = store;
    }

    /**
     * Gets a getable with a provided ID from the store (within a transaction). Note
     * that if you make any modifications to the Java object returned by this
     * method,
     * then those changes will be persisted in the state store when the
     * "transaction"
     * is committed, i.e. when we call {@link LHDAO#commitChanges()}.
     *
     * @param <U> is the proto type of the AbstractGetable.
     * @param <T> is the java class type of the AbstractGetable.
     * @param id  is the ObjectId to look for.
     * @return the specified AbstractGetable, or null if it doesn't exist.
     */
    public <U extends Message, T extends CoreGetable<U>> T get(CoreObjectId<?, U, T> id) {
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

    // Note that this is an expensive operation. It's used when deleting a WfRun.
    protected <U extends Message, T extends CoreGetable<U>> List<GetableToStore<U, T>> iterateOverPrefixAndPutInBuffer(
            String prefix, Class<T> cls) {

        List<GetableToStore<U, T>> out = iterateOverPrefix(prefix, cls);

        // put everything in the buffer.
        for (GetableToStore<U, T> thing : out) {
            uncommittedChanges.put(thing.getObjectToStore().getObjectId().getStoreableKey(), thing);
        }

        return out;
    }

    // Note that this is an expensive operation. It's used by External Event Nodes.
    @SuppressWarnings("unchecked")
    protected <U extends Message, T extends CoreGetable<U>> List<GetableToStore<U, T>> iterateOverPrefix(
            String prefix, Class<T> cls) {
        Map<String, GetableToStore<U, T>> all = new HashMap<>();

        // First iterate over what's in the store.
        String storePrefix = StoredGetable.getRocksDBKey(prefix, AbstractGetable.getTypeEnum(cls));

        try (LHKeyValueIterator<?> iterator = store.range(storePrefix, storePrefix + "~", StoredGetable.class)) {

            while (iterator.hasNext()) {
                LHIterKeyValue<? extends Storeable<?>> next = iterator.next();

                StoredGetable<U, T> item = (StoredGetable<U, T>) next.getValue();
                all.put(item.getStoreKey(), new GetableToStore<>(item, cls));
            }
        }

        // Overwrite what's in the store with what's in the buffer.
        for (Map.Entry<String, GetableToStore<?, ?>> entry : uncommittedChanges.entrySet()) {
            if (entry.getKey().startsWith(storePrefix)) {
                all.put(entry.getKey(), (GetableToStore<U, T>) entry.getValue());
            }
        }

        return all.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .toList();
    }
}

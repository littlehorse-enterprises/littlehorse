package io.littlehorse.server.streamsbackend.storeinternals.utils;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.server.Tags;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class LHLocalStore extends LHLocalReadOnlyStore {

    private KeyValueStore<String, Bytes> store;

    public LHLocalStore(KeyValueStore<String, Bytes> store, LHConfig config) {
        super(store, config);
        this.store = store;
    }

    public void put(Storeable<?> thing) {
        String storeKey = StoreUtils.getStoreKey(thing);
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }

    public Tags getTagsCache(GETable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);
        Bytes raw = store.get(tagCacheKey);
        if (raw == null) {
            return null;
        }

        try {
            return LHSerializable.fromBytes(raw.get(), Tags.class, config);
        } catch (LHSerdeError exn) {
            // Not possible unless bug in LittleHorse
            throw new RuntimeException(exn);
        }
    }

    public void putTagsCache(GETable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);

        Tags newTagsCache = new Tags();
        newTagsCache.entries = thing.getTags();

        store.put(tagCacheKey, new Bytes(newTagsCache.toBytes(config)));
    }
}

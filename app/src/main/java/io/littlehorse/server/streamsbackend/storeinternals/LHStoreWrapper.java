package io.littlehorse.server.streamsbackend.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.index.Tag;
import io.littlehorse.common.model.index.TagsCache;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

public class LHStoreWrapper extends LHROStoreWrapper {

    private KeyValueStore<String, Bytes> store;

    public LHStoreWrapper(KeyValueStore<String, Bytes> store, LHConfig config) {
        super(store, config);
        this.store = store;
    }

    public void put(Storeable<?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing);
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }

    public void delete(Storeable<?> thing) {
        store.delete(StoreUtils.getFullStoreKey(thing));
    }

    public void delete(String fullStoreKey) {
        store.delete(fullStoreKey);
    }

    public Bytes getRaw(String rawKey) {
        return store.get(rawKey);
    }

    public void putRaw(String rawKey, Bytes rawVal) {
        store.put(rawKey, rawVal);
    }

    public void deleteRaw(String rawKey) {
        store.delete(rawKey);
    }

    public TagsCache getTagsCache(GETable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);
        Bytes raw = store.get(tagCacheKey);
        if (raw == null) {
            return null;
        }

        try {
            return LHSerializable.fromBytes(raw.get(), TagsCache.class, config);
        } catch (LHSerdeError exn) {
            // Not possible unless bug in LittleHorse
            throw new RuntimeException(exn);
        }
    }

    public void putTagsCache(GETable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);

        TagsCache newTagsCache = new TagsCache();
        for (Tag tag : thing.getTags()) {
            newTagsCache.tagIds.add(StoreUtils.getFullStoreKey(tag));
        }

        store.put(tagCacheKey, new Bytes(newTagsCache.toBytes(config)));
    }
}

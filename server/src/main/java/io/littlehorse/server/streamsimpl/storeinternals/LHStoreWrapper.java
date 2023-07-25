package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.CommandResult;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class LHStoreWrapper extends LHROStoreWrapper {

    private KeyValueStore<String, Bytes> store;

    public LHStoreWrapper(KeyValueStore<String, Bytes> store, LHConfig config) {
        super(store, config);
        this.store = store;
    }

    public void put(Storeable<?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing);
        log.trace("Putting {}", storeKey);
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }

    public void put(String storeKey, Storeable<?> thing) {
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }

    public void delete(Storeable<?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing);
        log.trace("Deleting {}", storeKey);
        store.delete(storeKey);
    }

    public void deleteByStoreKey(
        String storeKey,
        Class<? extends Storeable<?>> storeableClass
    ) {
        String fullStoreKey = StoreUtils.getFullStoreKey(storeKey, storeableClass);
        delete(fullStoreKey);
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

    public TagsCache getTagsCache(String getableId, Class<? extends Getable<?>> cls) {
        Bytes raw = store.get(StoreUtils.getTagsCacheKey(getableId, cls));
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

    public void putTagsCache(
        String getableId,
        Class<? extends Getable<?>> getableCls,
        TagsCache newTagsCache
    ) {
        store.put(
            StoreUtils.getTagsCacheKey(getableId, getableCls),
            new Bytes(newTagsCache.toBytes(config))
        );
    }

    public void deleteTagCache(Getable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);
        store.delete(tagCacheKey);
    }

    public void putResponseToDelete(String objId) {
        String key = "responsekeys" + LHUtil.toLhDbFormat(new Date().getTime());
        store.put(
            key,
            new Bytes(
                StoreUtils.getFullStoreKey(objId, CommandResult.class).getBytes()
            )
        );
    }
}

package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoredGetable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class LHStoreWrapper extends LHROStoreWrapper {

    private KeyValueStore<String, Bytes> store;
    private int totalPuts;
    private int totalDeletes;
    private int totalGets;

    public LHStoreWrapper(KeyValueStore<String, Bytes> store, LHConfig config) {
        super(store, config);
        this.store = store;
        totalPuts = 0;
        totalDeletes = 0;
        totalGets = 0;
    }

    public void put(Storeable<?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing);
        put(storeKey, thing);
    }

    public void put(StoredGetable<?, ?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing.getStoredObject());
        put(storeKey, thing);
    }

    private void put(String storeKey, Storeable<?> thing) {
        totalPuts++;
        log.trace("Putting {}", storeKey);
        store.put(storeKey, new Bytes(thing.toBytes(config)));
    }

    public void delete(Storeable<?> thing) {
        String storeKey = StoreUtils.getFullStoreKey(thing);
        delete(storeKey);
    }

    public void delete(String storeKey, Class<? extends Storeable<?>> cls) {
        delete(StoreUtils.getFullStoreKey(storeKey, cls));
    }

    public void deleteByStoreKey(
        String storeKey,
        Class<? extends Storeable<?>> storeableClass
    ) {
        String fullStoreKey = StoreUtils.getFullStoreKey(storeKey, storeableClass);
        delete(fullStoreKey);
    }

    @Override
    public <
        U extends Message, T extends Getable<U>
    > StoredGetable<U, T> getStoredGetable(String objectId, Class<T> cls) {
        totalGets++;
        return super.getStoredGetable(objectId, cls);
    }

    public void delete(String fullStoreKey) {
        log.trace("Deleting {}", fullStoreKey);
        totalDeletes++;
        store.delete(fullStoreKey);
    }

    public Bytes getRaw(String rawKey) {
        totalGets++;
        log.warn("Getting: {}", rawKey);
        return store.get(rawKey);
    }

    public void putRaw(String rawKey, Bytes rawVal) {
        totalPuts++;
        store.put(rawKey, rawVal);
    }

    public void deleteRaw(String rawKey) {
        totalDeletes++;
        store.delete(rawKey);
    }

    public TagsCache getTagsCache(String getableId, Class<? extends Getable<?>> cls) {
        Bytes raw = getRaw(StoreUtils.getTagsCacheKey(getableId, cls));
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
        putRaw(
            StoreUtils.getTagsCacheKey(getableId, getableCls),
            new Bytes(newTagsCache.toBytes(config))
        );
    }

    public void deleteTagCache(Getable<?> thing) {
        String tagCacheKey = StoreUtils.getTagsCacheKey(thing);
        delete(tagCacheKey);
    }

    public void clearCommandMetrics(Command cmd) {
        if (cmd.getType() == CommandCase.TASK_WORKER_HEART_BEAT) {
            return;
        }
        log.trace(
            "{}: {} gets, {} puts, {} deletes",
            cmd.getType().toString(),
            totalGets,
            totalPuts,
            totalDeletes
        );
        totalGets = 0;
        totalPuts = 0;
        totalDeletes = 0;
    }
}

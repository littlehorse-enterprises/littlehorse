package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.CachedTag;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoredGetable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

import java.util.List;

@Slf4j
public class GetableStorageManager {

    private LHStoreWrapper localStore;

    private TagStorageManager tagStorageManager;

    public GetableStorageManager(
        LHStoreWrapper localStore,
        LHConfig config,
        TagStorageManager tagStorageManager
    ) {
        this.localStore = localStore;
        this.tagStorageManager = tagStorageManager;
    }

    public GetableStorageManager(
        LHStoreWrapper localStore,
        LHConfig config,
        ProcessorContext<String, CommandProcessorOutput> context
    ) {
        this(localStore, config, new TagStorageManager(localStore, context, config));
    }

    @SuppressWarnings("unchecked")
    public <T extends Getable<?>> void store(T getable) {
        localStore.put(getable);
        tagStorageManager.store(
            getable.getIndexEntries(),
            getable.getStoreKey(),
            (Class<? extends Getable<?>>) getable.getClass()
        );
    }
    public <U extends Message, T extends Getable<U>> void pepeStore(StoredGetable<U, T> getable) {
        TagsCache previousTags = getable.getIndexCache();
        List<Tag> newTags = getable.getStoredObject().getIndexEntries();
        List<CachedTag> newCachedTags = newTags
                .stream()
                .map(tag -> new CachedTag(tag.getStoreKey(), tag.isRemote()))
                .toList();
        StoredGetable<U, T> entityToStore = new StoredGetable<>(new TagsCache(newCachedTags), getable.getStoredObject(), getable.getObjectType());
        localStore.pepePut(entityToStore);
        tagStorageManager.pepeStore(newTags, previousTags);
    }

    public <U extends Message, T extends Getable<U>> void pepeDelete(
        String storeKey,
        Class<T> getableClass
    ) {
        StoredGetable<U, T> getable = localStore.getPepe(storeKey, getableClass);
        if (getable == null) {
            log.debug(
                    "Tried to delete a thing that didn't exist! Likely because it " +
                            "was created and deleted in the same Command Event."
            );
            return;
        }
        localStore.delete(getable.getStoredObject());
        tagStorageManager.pepeStore(List.of(), getable.getIndexCache());
    }

    public <U extends Message, T extends Getable<U>> void delete(
        String storeKey,
        Class<T> getableClass
    ) {
        // TODO: I think there might be a cacheing-related bug here.
        T getable = localStore.get(storeKey, getableClass);
        delete(getable);
    }

    @SuppressWarnings("unchecked")
    public <T extends Getable<?>> void delete(T getable) {
        if (getable == null) {
            log.debug(
                "Tried to delete a thing that didn't exist! Likely because it " +
                "was created and deleted in the same Command Event."
            );
            return;
        }
        localStore.delete(getable);

        TagsCache tagsCache = localStore.getTagsCache(
            getable.getStoreKey(),
            (Class<? extends Getable<?>>) getable.getClass()
        );
        tagsCache
            .getTags()
            .forEach(tagStoreKey -> {
                tagStorageManager.removeTag(tagStoreKey);
            });
        localStore.deleteTagCache(getable);
    }
}

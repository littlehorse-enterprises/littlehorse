package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.CachedTag;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoredGetable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Slf4j
public class GetableStorageManager {

    private final Map<String, StoredGetable<? extends Message, ? extends Getable<?>>> uncommittedChanges;

    private final LHStoreWrapper localStore;

    private final TagStorageManager tagStorageManager;

    public GetableStorageManager(
        LHStoreWrapper localStore,
        TagStorageManager tagStorageManager
    ) {
        this.localStore = localStore;
        this.tagStorageManager = tagStorageManager;
        this.uncommittedChanges = new HashMap<>();
    }

    public GetableStorageManager(
        LHStoreWrapper localStore,
        LHConfig config,
        ProcessorContext<String, CommandProcessorOutput> context
    ) {
        this(localStore, new TagStorageManager(localStore, context, config));
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends Getable<U>> T get(
        String key,
        Class<T> clazz
    ) {
        if (uncommittedChanges.containsKey(key)) {
            StoredGetable<U, T> storedGetable = (StoredGetable<U, T>) uncommittedChanges.get(key);
            return storedGetable.getStoredObject();
        }

        StoredGetable<U, T> storedGetable = localStore.getPepe(key, clazz);

        if (storedGetable != null) {
            uncommittedChanges.put(key, storedGetable);
            return storedGetable.getStoredObject();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends Getable<U>> void put(
        T getable,
        Class<T> clazz
    ) throws IllegalStateException {
        log.trace("Putting {} with key {}", getable.getClass(), getable.getStoreKey());

        StoredGetable<U, T> uncommittedEntity = (StoredGetable<U, T>) uncommittedChanges.get(getable.getStoreKey());

        if (uncommittedEntity != null) {
            if (uncommittedEntity.getStoredObject() != getable) {
                throw new IllegalStateException(
                    "Appears that Getable " +
                    getable.getObjectId() +
                    " was re-instantiated"
                );
            }
            return;
        }

        StoredGetable<U, T> previousValue = localStore.getPepe(getable.getStoreKey(), clazz);

        final StoredGetable<U, T> toPut = previousValue != null
            ? new StoredGetable<>(
                previousValue.getIndexCache(),
                getable,
                previousValue.getObjectType()
            )
            : new StoredGetable<>(
                new TagsCache(),
                getable,
                Getable.getTypeEnum(clazz)
            );

        uncommittedChanges.put(getable.getStoreKey(), toPut);
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends Getable<U>> void deletep(
        String key,
        Class<T> clazz
    ) throws IllegalStateException {
        log.trace("Deleting {} with key {}", clazz, key);

        StoredGetable<U, T> entity = localStore.getPepe(key, clazz);

        if (entity != null) {
            StoredGetable<U, T> toDelete = new StoredGetable<>(
                entity.getIndexCache(),
                null,
                entity.getObjectType()
            );
            uncommittedChanges.put(key, toDelete);
        }
    }

    public void commit() {
        for (Map.Entry<String, StoredGetable<? extends Message, ? extends Getable<?>>> entry : uncommittedChanges.entrySet()) {
            StoredGetable<? extends Message, ? extends Getable<?>> storedGetable = entry.getValue();
            if (storedGetable.getStoredObject() != null) {
                pepeStore(storedGetable);
            } else {
                pepeDelete(entry.getKey(), storedGetable);
            }
        }
        uncommittedChanges.clear();
    }

    private <U extends Message, T extends Getable<U>> void pepeStore(
        StoredGetable<U, T> getable
    ) {
        TagsCache previousTags = getable.getIndexCache();
        List<Tag> newTags = getable.getStoredObject().getIndexEntries();
        List<CachedTag> newCachedTags = newTags
            .stream()
            .map(tag -> new CachedTag(tag.getStoreKey(), tag.isRemote()))
            .toList();
        StoredGetable<U, T> entityToStore = new StoredGetable<>(
            new TagsCache(newCachedTags),
            getable.getStoredObject(),
            getable.getObjectType()
        );
        localStore.pepePut(entityToStore);
        tagStorageManager.pepeStore(newTags, previousTags);
    }

    private <U extends Message, T extends Getable<U>> void pepeDelete(
        String key,
        StoredGetable<U, T> getable
    ) {
        localStore.delete(key, getable.getStoredClass());
        tagStorageManager.pepeStore(List.of(), getable.getIndexCache());
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

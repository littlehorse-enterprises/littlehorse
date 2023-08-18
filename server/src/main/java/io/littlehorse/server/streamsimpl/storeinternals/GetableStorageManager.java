package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.CachedTag;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHKeyValueIterator;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoredGetable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Slf4j
public class GetableStorageManager {

    private final Map<String, StoredGetable<?, ?>> uncommittedChanges;

    private final LHStoreWrapper localStore;

    private final TagStorageManager tagStorageManager;

    public GetableStorageManager(LHStoreWrapper localStore, TagStorageManager tagStorageManager) {
        this.localStore = localStore;
        this.tagStorageManager = tagStorageManager;
        this.uncommittedChanges = new HashMap<>();
    }

    public GetableStorageManager(
            LHStoreWrapper localStore, LHConfig config, ProcessorContext<String, CommandProcessorOutput> context) {
        this(localStore, new TagStorageManager(localStore, context, config));
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends Getable<U>> T get(String key, Class<T> clazz) {
        if (uncommittedChanges.containsKey(key)) {
            StoredGetable<U, T> storedGetable = (StoredGetable<U, T>) uncommittedChanges.get(key);
            return storedGetable.getStoredObject();
        }

        StoredGetable<U, T> storedGetable = localStore.getStoredGetable(key, clazz);

        if (storedGetable != null) {
            uncommittedChanges.put(key, storedGetable);
            return storedGetable.getStoredObject();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <U extends Message, T extends Getable<U>> void put(T getable, Class<T> clazz) throws IllegalStateException {
        log.trace("Putting {} with key {}", getable.getClass(), getable.getStoreKey());

        StoredGetable<U, T> uncommittedEntity = (StoredGetable<U, T>) uncommittedChanges.get(getable.getStoreKey());

        if (uncommittedEntity != null) {
            if (uncommittedEntity.getStoredObject() != getable) {
                throw new IllegalStateException(
                        "Appears that Getable " + getable.getObjectId() + " was re-instantiated");
            }
            return;
        }

        StoredGetable<U, T> previousValue = localStore.getStoredGetable(getable.getStoreKey(), clazz);

        final StoredGetable<U, T> toPut = previousValue != null
                ? new StoredGetable<>(previousValue.getIndexCache(), getable, previousValue.getObjectType())
                : new StoredGetable<>(new TagsCache(), getable, Getable.getTypeEnum(clazz));

        uncommittedChanges.put(getable.getStoreKey(), toPut);
    }

    public <U extends Message, T extends Getable<U>> void delete(String key, Class<T> clazz)
            throws IllegalStateException {
        log.trace("Deleting {} with key {}", clazz, key);

        StoredGetable<U, T> entity = localStore.getStoredGetable(key, clazz);

        if (entity != null) {
            StoredGetable<U, T> toDelete = new StoredGetable<>(entity.getIndexCache(), null, entity.getObjectType());
            uncommittedChanges.put(key, toDelete);
        }
    }

    public <U extends Message, T extends Getable<U>> void abortAndUpdate(T getable) {
        uncommittedChanges.clear();

        @SuppressWarnings("unchecked")
        StoredGetable<U, T> storedGetable =
                (StoredGetable<U, T>) localStore.getStoredGetable(getable.getStoreKey(), getable.getClass());
        if (storedGetable != null) {
            StoredGetable<U, T> toUpdate =
                    new StoredGetable<>(storedGetable.getIndexCache(), getable, storedGetable.getObjectType());
            insertIntoStore(toUpdate);
        }
    }

    public void commit() {
        for (Map.Entry<String, StoredGetable<?, ?>> entry : uncommittedChanges.entrySet()) {
            StoredGetable<? extends Message, ? extends Getable<?>> storedGetable = entry.getValue();
            if (storedGetable.getStoredObject() != null) {
                insertIntoStore(storedGetable);
            } else {
                deleteFromStore(entry.getKey(), storedGetable);
            }
        }
        uncommittedChanges.clear();
    }

    private <U extends Message, T extends Getable<U>> void insertIntoStore(StoredGetable<?, ?> getable) {
        TagsCache previousTags = getable.getIndexCache();
        List<Tag> newTags = getable.getStoredObject().getIndexEntries();
        List<CachedTag> newCachedTags = newTags.stream()
                .map(tag -> new CachedTag(tag.getStoreKey(), tag.isRemote()))
                .toList();

        @SuppressWarnings("unchecked")
        StoredGetable<U, T> entityToStore = (StoredGetable<U, T>)
                new StoredGetable<>(new TagsCache(newCachedTags), getable.getStoredObject(), getable.getObjectType());
        localStore.put(entityToStore);
        tagStorageManager.store(newTags, previousTags);
    }

    private <U extends Message, T extends Getable<U>> void deleteFromStore(String key, StoredGetable<?, ?> getable) {
        localStore.delete(key, getable.getStoredClass());
        tagStorageManager.store(List.of(), getable.getIndexCache());
    }

    /**
     * @deprecated Should not use this method because it's not saving/deleting using the
     *     StoredGetable class. This method will be removed once all entities are migrated to use
     *     the StoredGetable class.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public <T extends Getable<?>> void store(T getable) {
        localStore.put(getable);
        tagStorageManager.storeUsingCache(
                getable.getIndexEntries(), getable.getStoreKey(), (Class<? extends Getable<?>>) getable.getClass());
    }

    /**
     * @deprecated Should not use this method because it's not saving/deleting using the
     *     StoredGetable class. This method will be removed once all entities are migrated to use
     *     the StoredGetable class.
     */
    @Deprecated(forRemoval = true)
    public <U extends Message, T extends Getable<U>> void deleteGetable(String storeKey, Class<T> getableClass) {
        // TODO: I think there might be a cacheing-related bug here.
        T getable = localStore.get(storeKey, getableClass);
        deleteGetable(getable);
    }

    /**
     * @deprecated Should not use this method because it's not saving/deleting using the
     *     StoredGetable class. This method will be removed once all entities are migrated to use
     *     the StoredGetable class.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public <T extends Getable<?>> void deleteGetable(T getable) {
        if (getable == null) {
            log.debug("Tried to delete a thing that didn't exist! Likely because it "
                    + "was created and deleted in the same Command Event.");
            return;
        }
        localStore.delete(getable);

        TagsCache tagsCache =
                localStore.getTagsCache(getable.getStoreKey(), (Class<? extends Getable<?>>) getable.getClass());
        tagsCache.getTags().forEach(tagStoreKey -> {
            tagStorageManager.removeTag(tagStoreKey);
        });
        localStore.deleteTagCache(getable);
    }

    public <U extends Message, T extends Getable<U>> T getFirstByCreatedTimeFromPrefix(
            String prefix, Class<T> cls, Predicate<T> discriminator) {
        for (String extEvtId : uncommittedChanges.keySet()) {
            if (extEvtId.startsWith(prefix)) {
                return (T) uncommittedChanges.get(extEvtId).getStoredObject();
            }
        }

        return getEntityListByPrefix(prefix, cls).stream()
                .filter(entity -> discriminator.test(entity.getStoredObject()))
                .min(Comparator.comparing(entity -> entity.getStoredObject().getCreatedAt()))
                .map(entity -> {
                    uncommittedChanges.put(entity.getStoreKey(), entity);
                    return entity.getStoredObject();
                })
                .orElse(null);
    }

    private <U extends Message, T extends Getable<U>> List<StoredGetable<U, T>> getEntityListByPrefix(
            String prefix, Class<T> cls) {
        try (LHKeyValueIterator<StoredGetable<U, T>> entityIterator = localStore.prefixScanStoredGetable(prefix, cls)) {
            ArrayList<StoredGetable<U, T>> entityList = new ArrayList<>();
            entityIterator.forEachRemaining(entity -> entityList.add(entity.getValue()));
            return entityList;
        }
    }
}

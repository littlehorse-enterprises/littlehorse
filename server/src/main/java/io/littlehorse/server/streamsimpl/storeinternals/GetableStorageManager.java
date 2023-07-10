package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.processor.api.ProcessorContext;

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
    public void store(Getable<?> getable) {
        localStore.put(getable);
        Collection<Tag> tags = getTagsFor(getable);
        tagStorageManager.store(
            tags,
            getable.getStoreKey(),
            // SAUL_EDUWER_TODO: Why is this "unchecked" warning?
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

    public Collection<Tag> getTagsFor(Getable<?> getAble) {
        List<GetableIndex> getAbleIndices = GetableIndexRegistry
            .getInstance()
            .findIndexesFor(getAble.getClass());
        return getAbleIndices
            .stream()
            .filter(geTableIndex -> geTableIndex.isActive(getAble))
            .flatMap(getableIndex ->
                extractTagValues(getableIndex, getAble)
                    .stream()
                    .map(pairs ->
                        new Tag(getAble, getableIndex.getTagStorageTypePb(), pairs)
                    )
            )
            .collect(Collectors.toList());
    }

    private Collection<List<Pair<String, String>>> extractTagValues(
        GetableIndex getableIndex,
        Getable<?> geTable
    ) {
        return getableIndex
            .getKeys()
            .stream()
            .flatMap(key ->
                getableIndex
                    .getValue(geTable, key)
                    .stream()
                    .map(value -> Pair.of(key, value))
            )
            .collect(
                Collectors.groupingBy(pair ->
                    getableIndex
                        .getValue(geTable, pair.getLeft())
                        .indexOf(pair.getRight())
                )
            )
            .values();
    }
}

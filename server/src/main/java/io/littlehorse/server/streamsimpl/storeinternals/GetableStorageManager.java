package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
    public <T extends Getable<?>> void store(T getable) {
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

    public <T extends Getable<?>> Collection<Tag> getTagsFor(T getable) {
        return extractTagValues(getable);
    }

    private <T extends Getable<?>> List<Tag> extractTagValues(T geTable) {
        List<Tag> tags = new ArrayList<>();
        for (GetableIndex<? extends Getable<?>> indexConfiguration : geTable.getIndexConfigurations()) {
            if (!indexConfiguration.isValid(geTable)) {
                continue;
            }
            Optional<TagStorageTypePb> tagStorageTypePb = indexConfiguration.getTagStorageTypePb();
            List<IndexedField> singleIndexedValues = indexConfiguration
                .getAttributes()
                .stream()
                .filter(stringValueTypePair -> {
                    return stringValueTypePair
                        .getValue()
                        .equals(GetableIndex.ValueType.SINGLE);
                })
                .map(stringValueTypePair -> {
                    return geTable
                        .getIndexValues(
                            stringValueTypePair.getKey(),
                            tagStorageTypePb
                        )
                        .get(0);
                })
                .toList();
            List<IndexedField> dynamicIndexedFields = indexConfiguration
                .getAttributes()
                .stream()
                .filter(stringValueTypePair -> {
                    return stringValueTypePair
                        .getValue()
                        .equals(GetableIndex.ValueType.DYNAMIC);
                })
                .flatMap(stringValueTypePair ->
                    geTable
                        .getIndexValues(
                            stringValueTypePair.getKey(),
                            tagStorageTypePb
                        )
                        .stream()
                )
                .toList();
            List<List<IndexedField>> combine = combine(
                singleIndexedValues,
                dynamicIndexedFields
            );
            for (List<IndexedField> list : combine) {
                TagStorageTypePb storageType = list
                    .stream()
                    .map(IndexedField::getTagStorageTypePb)
                    .filter(tagStorageTypePb1 ->
                        tagStorageTypePb1 == TagStorageTypePb.REMOTE
                    )
                    .findAny()
                    .orElse(TagStorageTypePb.LOCAL);
                List<Pair<String, String>> pairs = list
                    .stream()
                    .map(indexedField ->
                        Pair.of(
                            indexedField.getKey(),
                            indexedField.getValue().toString()
                        )
                    )
                    .toList();
                tags.add(new Tag(geTable, storageType, pairs));
            }
        }
        return tags;
    }

    private List<List<IndexedField>> combine(
        List<IndexedField> source,
        List<IndexedField> multiple
    ) {
        if (multiple.isEmpty()) {
            return List.of(source);
        }
        List<List<IndexedField>> result = new ArrayList<>();
        for (IndexedField dynamicIndexedField : multiple) {
            List<IndexedField> list = Stream
                .concat(source.stream(), Stream.of(dynamicIndexedField))
                .toList();
            result.add(list);
        }
        return result;
    }
}

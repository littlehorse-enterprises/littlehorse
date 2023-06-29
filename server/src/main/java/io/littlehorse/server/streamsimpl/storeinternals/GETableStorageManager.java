package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagUtils;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class GETableStorageManager {

    private LHStoreWrapper localStore;

    private LHConfig lhConfig;

    private TagStorageManager tagStorageManager;

    public GETableStorageManager(
        LHStoreWrapper localStore,
        LHConfig lhConfig,
        TagStorageManager tagStorageManager
    ) {
        this.localStore = localStore;
        this.lhConfig = lhConfig;
        this.tagStorageManager = tagStorageManager;
    }

    public GETableStorageManager(
        LHStoreWrapper localStore,
        LHConfig lhConfig,
        ProcessorContext<String, CommandProcessorOutput> context
    ) {
        this(
            localStore,
            lhConfig,
            new TagStorageManager(localStore, context, lhConfig)
        );
    }

    public void store(GETable<?> geTable) {
        localStore.put(geTable);
        Collection<Tag> tags = getTagsFor(geTable);
        String tagsCacheKey = StoreUtils.getTagsCacheKey(geTable);
        tagStorageManager.store(tags, tagsCacheKey);
    }

    public Collection<Tag> getTagsFor(GETable<?> getAble) {
        if (getAble instanceof NodeRun) {
            return TagUtils.tagThing(getAble);
        }
        List<GETableIndex> getAbleIndices = GETableIndexRegistry
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
        GETableIndex getableIndex,
        GETable<?> geTable
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

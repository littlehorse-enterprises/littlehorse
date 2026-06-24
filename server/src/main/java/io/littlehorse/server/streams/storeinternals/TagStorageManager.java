package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.PartitionCountedTagModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.stores.ClusterScopedStore;
import io.littlehorse.server.streams.stores.PartitionLocalBuffer;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.littlehorse.server.streams.topology.core.Forwardable;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class TagStorageManager {

    private final TenantScopedStore lhStore;
    private final ProcessorContext<String, Forwardable> context;
    private final AuthorizationContext authContext;
    private final PartitionLocalBuffer<PartitionCountedTagModel> countedTags;
    private final ClusterScopedStore clusterScopedStore;

    public TagStorageManager(
            TenantScopedStore lhStore,
            ProcessorContext<String, Forwardable> context,
            CoreProcessorContext executionContext) {
        this.lhStore = lhStore;
        this.context = context;
        this.authContext = executionContext.authorization();
        this.countedTags = executionContext.getCountedTagsAccumulator();
        this.clusterScopedStore = ClusterScopedStore.newInstance(executionContext.nativeCoreStore(), executionContext);
    }

    public void store(Collection<Tag> newTags, TagsCache preExistingTags) {
        List<String> newTagIds = newTags.stream().map(Tag::getStoreKey).toList();
        List<Tag> tagsToAdd = new ArrayList<>();
        for (Tag newTag : newTags) {
            if (!preExistingTags.contains(newTag)) {
                tagsToAdd.add(newTag);
            }
        }

        List<CachedTag> tagsToRemove = preExistingTags.getTags().stream()
                .filter(cachedTag -> !newTagIds.contains(cachedTag.getId()))
                .toList();

        tagsToRemove.forEach(this::removeTag);
        tagsToAdd.forEach(this::createTag);
    }

    private void createTag(Tag tag) {
        if (tag.isCounted()) {
            PartitionCountedTagModel currentAggregation =
                    incrementCounted(authContext.tenantId(), tag.getAttributeString());
            clusterScopedStore.put(currentAggregation);
        } else {
            lhStore.put(tag);
        }
    }

    private void removeTag(CachedTag cachedTag) {
        if (cachedTag.isCounted()) {
            PartitionCountedTagModel currentAggregation =
                    decrementCounted(authContext.tenantId(), cachedTag.getAttributeString());
            clusterScopedStore.put(currentAggregation);
        } else {
            lhStore.delete(cachedTag.getId(), StoreableType.TAG);
        }
    }

    private PartitionCountedTagModel incrementCounted(TenantIdModel tenantId, String tagAttributes) {
        PartitionCountedTagModel current = getOrCreateCountedTag(tenantId, tagAttributes);
        current.increment();
        countedTags.put(current);
        return current;
    }

    private PartitionCountedTagModel decrementCounted(TenantIdModel tenantId, String tagAttributes) {
        PartitionCountedTagModel current = getOrCreateCountedTag(tenantId, tagAttributes);
        current.decrement();
        countedTags.put(current);
        return current;
    }

    private PartitionCountedTagModel getOrCreateCountedTag(TenantIdModel tenantId, String tagAttributes) {
        PartitionCountedTagModel current = countedTags.get(tagAttributes);
        if (current == null) {
            String storeKey = new PartitionCountedTagModel(tenantId, tagAttributes).getStoreKey();
            current = clusterScopedStore.get(storeKey, PartitionCountedTagModel.class);
        }
        if (current == null) {
            current = new PartitionCountedTagModel(tenantId, tagAttributes);
        }
        return current;
    }
}

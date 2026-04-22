package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.kafka.streams.processor.api.ProcessorContext;

public class TagStorageManager {

    private final TenantScopedStore lhStore;
    private final ProcessorContext<String, CommandProcessorOutput> context;
    private final LHServerConfig lhConfig;
    private final AuthorizationContext authContext;

    public TagStorageManager(
            TenantScopedStore lhStore,
            ProcessorContext<String, CommandProcessorOutput> context,
            LHServerConfig lhConfig,
            ExecutionContext executionContext) {
        this.lhStore = lhStore;
        this.context = context;
        this.lhConfig = lhConfig;
        this.authContext = executionContext.authorization();
    }

    public void store(Collection<Tag> newTags, TagsCache preExistingTags) {
        List<String> newTagIds = newTags.stream().map(tag -> tag.getStoreKey()).toList();
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
        if (tag.isRemote()) {
            throw new IllegalStateException("Remote tags are no longer possible");
        } else {
            lhStore.put(tag);
        }
    }

    private void removeTag(CachedTag cachedTag) {
        if (cachedTag.isRemote()) {
            throw new IllegalStateException("Remote tags are no longer possible");
        } else {
            lhStore.delete(cachedTag.getId(), StoreableType.TAG);
        }
    }
}

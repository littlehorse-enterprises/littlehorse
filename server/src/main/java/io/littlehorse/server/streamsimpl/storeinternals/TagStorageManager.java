package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.CachedTag;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@AllArgsConstructor
public class TagStorageManager {

    private LHStoreWrapper localStore;
    private ProcessorContext<String, CommandProcessorOutput> context;
    private LHConfig lhConfig;

    public void store(Collection<Tag> tags, String tagsCacheKey) {
        TagsCache tagsCache = localStore.getTagsCache(tagsCacheKey);
        tagsCache = tagsCache != null ? tagsCache : new TagsCache();
        List<String> existingTagIds = tagsCache.getTagIds();
        List<CachedTag> cachedTags = tags
            .stream()
            .map(tag -> {
                CachedTag cachedTag = new CachedTag();
                cachedTag.setId(tag.getStoreKey());
                cachedTag.setRemote(tag.isRemote());
                return cachedTag;
            })
            .toList();
        this.storeLocalOrRemoteTag(tags, existingTagIds);
        this.removeOldTags(tags, existingTagIds);
        tagsCache.setTags(cachedTags);
        localStore.putTagsCache(tagsCacheKey, tagsCache);
    }

    private void removeOldTags(Collection<Tag> newTags, List<String> cachedTagIds) {
        List<String> newTagIds = newTags.stream().map(Tag::getStoreKey).toList();
        List<String> tagsIdsToRemove = cachedTagIds
            .stream()
            .filter(cachedTagId -> !newTagIds.contains(cachedTagId))
            .toList();
        tagsIdsToRemove.forEach(this::removeTag);
    }

    private void removeTag(String tagStoreKey) {
        Tag tag = localStore.get(tagStoreKey, Tag.class);
        if (tag != null) {
            localStore.delete(tag);
        } else {
            String attributeString = extractAttributeStringFromStoreKey(tagStoreKey);
            sendRepartitionCommandForRemoveRemoteTag(tagStoreKey, attributeString);
        }
    }

    private String extractAttributeStringFromStoreKey(String tagStoreKey) {
        String[] splittedStoreKey = tagStoreKey.split("/");
        return splittedStoreKey[0] + "/" + splittedStoreKey[1];
    }

    private void storeLocalOrRemoteTag(
        Collection<Tag> tags,
        List<String> cachedTagIds
    ) {
        tags
            .stream()
            .filter(tag -> !cachedTagIds.contains(tag.getStoreKey()))
            .forEach(tag -> {
                if (tag.isRemote()) {
                    this.sendRepartitionCommandForCreateRemoteTag(tag);
                } else {
                    localStore.put(tag);
                }
            });
    }

    private void sendRepartitionCommandForRemoveRemoteTag(
        String tagStoreKey,
        String tagAttributeString
    ) {
        RemoveRemoteTag command = new RemoveRemoteTag(
            tagStoreKey,
            tagAttributeString
        );
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = tagAttributeString;
        cpo.topic = this.lhConfig.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(command, new Date(), tagStoreKey);
        Record<String, CommandProcessorOutput> out = new Record<>(
            tagAttributeString,
            cpo,
            System.currentTimeMillis()
        );
        this.context.forward(out);
    }

    private void sendRepartitionCommandForCreateRemoteTag(Tag tag) {
        CreateRemoteTag command = new CreateRemoteTag(tag);
        List<String> attributeStrings = tag
            .getAttributes()
            .stream()
            .map(Attribute::getEscapedKey)
            .collect(Collectors.toList());
        GETableIndex getableIndex = GETableIndexRegistry
            .getInstance()
            .findConfigurationForAttributes(
                GETable.getCls(tag.getObjectType()),
                attributeStrings
            );
        String partitionKey = getableIndex.getPartitionKeyForAttrs(
            tag.getAttributes()
        );
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.setPartitionKey(partitionKey);
        cpo.setTopic(this.lhConfig.getRepartitionTopicName());
        cpo.setPayload(new RepartitionCommand(command, new Date(), partitionKey));
        Record<String, CommandProcessorOutput> out = new Record<>(
            partitionKey,
            cpo,
            System.currentTimeMillis()
        );
        this.context.forward(out);
    }
}

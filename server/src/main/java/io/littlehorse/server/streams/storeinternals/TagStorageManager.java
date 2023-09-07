package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

@AllArgsConstructor
public class TagStorageManager {

    private RocksDBWrapper localStore;
    private ProcessorContext<String, CommandProcessorOutput> context;
    private LHServerConfig lhConfig;

    public void store(Collection<Tag> newTags, TagsCache preExistingTags) {
        List<String> newTagIds = newTags.stream().map(tag -> tag.getStoreKey()).toList();

        List<Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !preExistingTags.getTagIds().contains(tag.getStoreKey()))
                .toList();

        List<CachedTag> tagsToRemove = preExistingTags.getTags().stream()
                .filter(cachedTag -> !newTagIds.contains(cachedTag.getId()))
                .toList();

        tagsToRemove.forEach(this::removeTag);
        tagsToAdd.forEach(this::createTag);
    }

    private void createTag(Tag tag) {
        if (tag.isRemote()) {
            this.sendRepartitionCommandForCreateRemoteTag(tag);
        } else {
            localStore.put(tag);
        }
    }

    private void removeTag(CachedTag cachedTag) {
        if (cachedTag.isRemote()) {
            String attributeString = extractAttributeStringFromStoreKey(cachedTag.getId());
            sendRepartitionCommandForRemoveRemoteTag(cachedTag.getId(), attributeString);
        } else {
            localStore.delete(cachedTag.getId(), StoreableType.TAG);
        }
    }

    private String extractAttributeStringFromStoreKey(String tagStoreKey) {
        String[] splittedStoreKey = tagStoreKey.split("/");
        return splittedStoreKey[0] + "/" + splittedStoreKey[1];
    }

    private void sendRepartitionCommandForRemoveRemoteTag(String tagStoreKey, String tagAttributeString) {
        RemoveRemoteTag command = new RemoveRemoteTag(tagStoreKey, tagAttributeString);
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = tagAttributeString;
        cpo.topic = this.lhConfig.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(command, new Date(), tagStoreKey);
        Record<String, CommandProcessorOutput> out = new Record<>(tagAttributeString, cpo, System.currentTimeMillis());
        this.context.forward(out);
    }

    private void sendRepartitionCommandForCreateRemoteTag(Tag tag) {
        CreateRemoteTag command = new CreateRemoteTag(tag);
        String partitionKey = tag.getPartitionKey();
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.setPartitionKey(partitionKey);
        cpo.setTopic(this.lhConfig.getRepartitionTopicName());
        cpo.setPayload(new RepartitionCommand(command, new Date(), partitionKey));
        Record<String, CommandProcessorOutput> out = new Record<>(partitionKey, cpo, System.currentTimeMillis());
        this.context.forward(out);
    }
}

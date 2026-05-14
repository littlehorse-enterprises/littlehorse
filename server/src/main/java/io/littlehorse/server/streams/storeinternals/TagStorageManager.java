package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.UpdateCountedTagModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

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
        if (tag.isCounted()) {
            this.sendCountedTag(tag);
        } else {
            lhStore.put(tag);
        }
    }

    private void removeTag(CachedTag cachedTag) {
        if (cachedTag.isRemote()) {
            String attributeString = extractAttributeStringFromStoreKey(cachedTag.getId());
            sendRepartitionCommandForRemoveRemoteTag(cachedTag.getId(), attributeString);
        } else if (cachedTag.isCounted()) {
            sendDeleteCountedTag(cachedTag);
        } else {
            lhStore.delete(cachedTag.getId(), StoreableType.TAG);
        }
    }

    private String extractAttributeStringFromStoreKey(String tagStoreKey) {
        String[] splittedStoreKey = tagStoreKey.split("/");
        return splittedStoreKey[0] + "/" + splittedStoreKey[1];
    }

    private void sendRepartitionCommandForRemoveRemoteTag(String tagStoreKey, String tagAttributeString) {
        RemoveRemoteTag command = new RemoveRemoteTag(tagStoreKey, tagAttributeString);
        Headers metadata = HeadersUtil.metadataHeadersFor(authContext.tenantId(), authContext.principalId());
        RepartitionCommand repartitionCommand = new RepartitionCommand(command, new Date(), tagStoreKey);
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = tagAttributeString;
        cpo.topic = this.lhConfig.getRepartitionTopicName();
        cpo.payload = repartitionCommand;
        Record<String, CommandProcessorOutput> out =
                new Record<>(tagAttributeString, cpo, System.currentTimeMillis(), metadata);
        this.context.forward(out);
    }

    private void sendCountedTag(Tag tag) {
        UpdateCountedTagModel updateCountedTag = new UpdateCountedTagModel(tag.getAttributeString());
        CommandModel command = new CommandModel(updateCountedTag);
        LHTimer timer = new LHTimer(command, true);
        timer.topic = this.lhConfig.getCoreCmdTopicName();
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = timer.getPartitionKey();
        cpo.topic = this.lhConfig.getCoreCmdTopicName();
        cpo.payload = timer;
        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(authContext.tenantId(), authContext.principalId()));
        context.forward(out);
    }

    private void sendDeleteCountedTag(CachedTag cachedTag) {
        UpdateCountedTagModel updateCountedTag = new UpdateCountedTagModel(cachedTag.getAttributeString(), true);
        CommandModel command = new CommandModel(updateCountedTag);
        LHTimer timer = new LHTimer(command, true);
        timer.topic = this.lhConfig.getCoreCmdTopicName();
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = timer.getPartitionKey();
        cpo.topic = this.lhConfig.getCoreCmdTopicName();
        cpo.payload = timer;
        Record<String, CommandProcessorOutput> out = new Record<>(
                cpo.partitionKey,
                cpo,
                System.currentTimeMillis(),
                HeadersUtil.metadataHeadersFor(authContext.tenantId(), authContext.principalId()));
        context.forward(out);
    }
}

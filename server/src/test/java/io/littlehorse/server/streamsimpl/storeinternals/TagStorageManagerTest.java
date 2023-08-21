package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.server.streams.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streams.storeinternals.TagStorageManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import java.util.List;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagStorageManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHConfig lhConfig;

    private LHStoreWrapper localStore = new LHStoreWrapper(store, lhConfig);

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();

    @InjectMocks
    private TagStorageManager tagStorageManager = new TagStorageManager(localStore, mockProcessorContext, lhConfig);

    private Tag tag1 = TestUtil.tag();

    private Tag tag2 = TestUtil.tag();

    private List<Tag> tags;

    private Attribute wfSpecNameAttribute = new Attribute("wfSpecName", "test-name");
    private Attribute statusAttribute = new Attribute("status", "running");

    @BeforeEach
    void setup() {
        store.init(mockProcessorContext.getStateStoreContext(), store);
        tag1.setAttributes(List.of(wfSpecNameAttribute));
        tag2.setAttributes(List.of(wfSpecNameAttribute, statusAttribute));
        tags = List.of(tag1, tag2);
    }

    @Test
    void saveTagsWithNewTagsCache() {
        tagStorageManager.storeUsingCache(tags, "123456", WfRunModel.class);
        Tag tagResult1 = localStore.get(tag1.getStoreKey(), Tag.class);
        Tag tagResult2 = localStore.get(tag2.getStoreKey(), Tag.class);
        TagsCache tagsCache = localStore.getTagsCache("123456", WfRunModel.class);
        Assertions.assertThat(tagResult1).isNotNull();
        Assertions.assertThat(tagResult2).isNotNull();
        Assertions.assertThat(tagsCache).isNotNull();
        Assertions.assertThat(tagsCache.getTags()).hasSize(2);
        tagsCache.getTags().forEach(cachedTag -> {
            Assertions.assertThat(cachedTag.getId()).isNotNull();
        });
    }

    @Test
    void removeOldTags() {
        String wfRunId = "123456";
        Tag tag3 = TestUtil.tag();
        tags = List.of(tag2, tag3);
        localStore.put(tag1);
        TagsCache tagsCache = new TagsCache();
        CachedTag cachedTag = new CachedTag();
        cachedTag.setId(tag1.getStoreKey());
        tagsCache.setTags(List.of(cachedTag));
        localStore.putTagsCache(wfRunId, WfRunModel.class, tagsCache);

        tagStorageManager.storeUsingCache(tags, wfRunId, WfRunModel.class);
        TagsCache tagsCacheResult = localStore.getTagsCache(wfRunId, WfRunModel.class);
        Assertions.assertThat(localStore.get(tag2.getStoreKey(), Tag.class)).isNotNull();
        Assertions.assertThat(localStore.get(tag3.getStoreKey(), Tag.class)).isNotNull();
        Assertions.assertThat(localStore.get(tag1.getStoreKey(), Tag.class)).isNull();
        Assertions.assertThat(tagsCacheResult).isNotNull();
        Assertions.assertThat(tagsCacheResult.getTagIds())
                .containsExactlyInAnyOrder(tag2.getStoreKey(), tag3.getStoreKey());
    }

    @Test
    void sendRepartitionCommandForCreateRemoteTagSubCommand() {
        String expectedPartitionKey = "3/__wfSpecName_test-name";
        tag1.setTagType(TagStorageType.REMOTE);
        tags = List.of(tag1, tag2);
        tagStorageManager.storeUsingCache(tags, "test-wfrun-id", WfRunModel.class);
        List<? extends Record<? extends String, ? extends CommandProcessorOutput>> outputs =
                mockProcessorContext.forwarded().stream()
                        .map(MockProcessorContext.CapturedForward::record)
                        .toList();
        Assertions.assertThat(outputs).hasSize(1);
        outputs.forEach(record -> {
            Assertions.assertThat(record.key()).isEqualTo(expectedPartitionKey);
            Assertions.assertThat(record.value().getPartitionKey()).isEqualTo(expectedPartitionKey);
            Assertions.assertThat(record.value().getPayload()).isInstanceOf(RepartitionCommand.class);
            RepartitionCommand repartitionCommand =
                    (RepartitionCommand) record.value().getPayload();
            Assertions.assertThat(repartitionCommand.getSubCommand().getPartitionKey())
                    .isEqualTo(expectedPartitionKey);
        });
    }

    @Test
    void sendRepartitionCommandForRemoveRemoteTagSubCommand() {
        tag1.setTagType(TagStorageType.REMOTE);
        String wfRunId = "123456";
        Tag tag3 = TestUtil.tag();
        tag3.setTagType(TagStorageType.REMOTE);
        tags = List.of(tag1, tag2);
        TagsCache tagsCache = new TagsCache();
        CachedTag cachedTag1 = new CachedTag();
        cachedTag1.setId(tag3.getStoreKey());
        cachedTag1.setRemote(true);
        CachedTag cachedTag2 = new CachedTag();
        cachedTag2.setId(tag2.getStoreKey());
        tagsCache.setTags(List.of(cachedTag1, cachedTag2));
        localStore.putTagsCache(wfRunId, WfRunModel.class, tagsCache);

        tagStorageManager.storeUsingCache(tags, wfRunId, WfRunModel.class);
        List<? extends Record<? extends String, ? extends CommandProcessorOutput>> outputs =
                mockProcessorContext.forwarded().stream()
                        .map(MockProcessorContext.CapturedForward::record)
                        .toList();
        Assertions.assertThat(outputs).hasSize(2);
    }
}

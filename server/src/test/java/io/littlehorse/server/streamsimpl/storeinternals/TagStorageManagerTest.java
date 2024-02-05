package io.littlehorse.server.streamsimpl.storeinternals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.storeinternals.TagStorageManager;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.CachedTag;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagStorageManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.CORE_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    private final KeyValueStore<String, Bytes> globalMetadaataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.GLOBAL_METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHServerConfig lhConfig;

    @Mock
    private KafkaStreamsServerImpl server;

    private String tenantId = "myTenant";

    private TenantScopedStore localStore = TenantScopedStore.newInstance(store, new TenantIdModel(tenantId), mock());

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();

    @InjectMocks
    private TagStorageManager tagStorageManager;

    private Tag tag1 = TestUtil.tag();

    private Tag tag2 = TestUtil.tag();

    private List<Tag> tags;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ExecutionContext executionContext;

    private Attribute wfSpecNameAttribute = new Attribute("wfSpecName", "test-name");
    private Attribute statusAttribute = new Attribute("status", "running");

    @BeforeEach
    void setup() {
        store.init(mockProcessorContext.getStateStoreContext(), store);
        globalMetadaataStore.init(mockProcessorContext.getStateStoreContext(), globalMetadaataStore);
        tagStorageManager = new TagStorageManager(localStore, mockProcessorContext, lhConfig, mock());
        tag1.setAttributes(List.of(wfSpecNameAttribute));
        tag2.setAttributes(List.of(wfSpecNameAttribute, statusAttribute));
        tags = List.of(tag1, tag2);
    }

    @Test
    void saveNewTags() {
        tagStorageManager.store(tags, new TagsCache());
        Tag tagResult1 = localStore.get(tag1.getStoreKey(), Tag.class);
        Tag tagResult2 = localStore.get(tag2.getStoreKey(), Tag.class);
        assertThat(tagResult1).isNotNull();
        assertThat(tagResult2).isNotNull();
    }

    @Test
    void removeOldTags() {
        Tag tag3 = TestUtil.tag();
        tags = List.of(tag2, tag3);
        localStore.put(tag1);
        TagsCache tagsCache = new TagsCache();
        CachedTag cachedTag = new CachedTag();
        cachedTag.setId(tag1.getStoreKey());
        tagsCache.setTags(List.of(cachedTag));

        tagStorageManager.store(tags, tagsCache);
        assertThat(localStore.get(tag2.getStoreKey(), Tag.class)).isNotNull();
        assertThat(localStore.get(tag3.getStoreKey(), Tag.class)).isNotNull();
        assertThat(localStore.get(tag1.getStoreKey(), Tag.class)).isNull();
    }

    // @Test
    // void sendRepartitionCommandForCreateRemoteTagSubCommand() {
    //     when(processorDAO.context()).thenReturn(authorizationContext);
    //     String expectedPartitionKey = "3/__wfSpecName_test-name";
    //     tag1.setTagType(TagStorageType.REMOTE);
    //     tags = List.of(tag1, tag2);
    //     tagStorageManager.store(tags, new TagsCache());
    //     List<? extends Record<? extends String, ? extends CommandProcessorOutput>> outputs =
    //             mockProcessorContext.forwarded().stream()
    //                     .map(MockProcessorContext.CapturedForward::record)
    //                     .toList();
    //     assertThat(outputs).hasSize(1);
    //     outputs.forEach(record -> {
    //         assertThat(record.key()).isEqualTo(expectedPartitionKey);
    //         assertThat(record.value().getPartitionKey()).isEqualTo(expectedPartitionKey);
    //         assertThat(record.value().getPayload()).isInstanceOf(RepartitionCommand.class);
    //         RepartitionCommand repartitionCommand =
    //                 (RepartitionCommand) record.value().getPayload();
    //         assertThat(repartitionCommand.getSubCommand().getPartitionKey()).isEqualTo(expectedPartitionKey);
    //     });
    // }

    // @Test
    // void sendRepartitionCommandForRemoveRemoteTagSubCommand() {
    //     tag1.setTagType(TagStorageType.REMOTE);
    //     Tag tag3 = TestUtil.tag();
    //     tag3.setTagType(TagStorageType.REMOTE);
    //     tags = List.of(tag1, tag2);
    //     TagsCache tagsCache = new TagsCache();
    //     CachedTag cachedTag1 = new CachedTag();
    //     cachedTag1.setId(tag3.getStoreKey());
    //     cachedTag1.setRemote(true);
    //     CachedTag cachedTag2 = new CachedTag();
    //     cachedTag2.setId(tag2.getStoreKey());
    //     tagsCache.setTags(List.of(cachedTag1, cachedTag2));

    //     tagStorageManager.store(tags, tagsCache);
    //     List<? extends Record<? extends String, ? extends CommandProcessorOutput>> outputs =
    //             mockProcessorContext.forwarded().stream()
    //                     .map(MockProcessorContext.CapturedForward::record)
    //                     .toList();
    //     assertThat(outputs).hasSize(2);
    // }
}

package io.littlehorse.io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.RemoveRemoteTag;
import io.littlehorse.server.streamsimpl.storeinternals.GetableStorageManager;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetableStorageManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores
        .keyValueStoreBuilder(
            Stores.inMemoryKeyValueStore("myStore"),
            Serdes.String(),
            Serdes.Bytes()
        )
        .withLoggingDisabled()
        .build();

    @Mock
    private LHConfig lhConfig;

    private LHStoreWrapper localStoreWrapper;

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
    private GetableStorageManager geTableStorageManager;

    @BeforeEach
    void setup() {
        localStoreWrapper = new LHStoreWrapper(store, lhConfig);
        geTableStorageManager =
            new GetableStorageManager(
                localStoreWrapper,
                lhConfig,
                mockProcessorContext
            );
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    @ParameterizedTest
    @MethodSource("provideGetableObjectsAndIds")
    void storeNewGetableWithTags(
        Getable<?> getable,
        String storeKey,
        int expectedTagsCount
    ) {
        geTableStorageManager.store(getable);
        Assertions
            .assertThat(localStoreWrapper.get(storeKey, getable.getClass()))
            .isNotNull();
        TagsCache tagsCacheResult = localStoreWrapper.getTagsCache(
            getable.getStoreKey(),
            (Class<? extends Getable<?>>) getable.getClass()
        );
        Assertions.assertThat(tagsCacheResult).isNotNull();
        Assertions.assertThat(tagsCacheResult.getTagIds()).hasSize(expectedTagsCount);
    }

    @Test
    void deleteGetableWithTags() {
        WfRun wfRun = TestUtil.wfRun("0000000");
        geTableStorageManager.store(wfRun);
        TagsCache tagsCache = localStoreWrapper.getTagsCache(
            wfRun.getStoreKey(),
            WfRun.class
        );
        Map<Boolean, List<Tag>> localOrRemoteTags = tagsCache
            .getTagIds()
            .stream()
            .map(s -> localStoreWrapper.get(s, Tag.class))
            .collect(Collectors.groupingBy(Objects::nonNull));

        geTableStorageManager.delete(wfRun);
        Assertions
            .assertThat(localStoreWrapper.get(wfRun.getStoreKey(), WfRun.class))
            .isNull();
        TagsCache tagsCacheResult = localStoreWrapper.getTagsCache(
            wfRun.getStoreKey(),
            WfRun.class
        );
        List<Tag> localTagsToBeRemoved = localOrRemoteTags.get(true);
        List<Tag> remoteTagsToBeRemoved = localOrRemoteTags.get(false);

        long localTagsAfterDeletion = localTagsToBeRemoved
            .stream()
            .map(Tag::getStoreKey)
            .map(s -> localStoreWrapper.get(s, Tag.class))
            .filter(Objects::isNull)
            .count();
        long remoteTagsAfterDeletion = mockProcessorContext
            .forwarded()
            .stream()
            .map(MockProcessorContext.CapturedForward::record)
            .map(Record::value)
            .map(CommandProcessorOutput::getPayload)
            .map(lhSerializable -> (RepartitionCommand) lhSerializable)
            .filter(repartitionCommand ->
                repartitionCommand.getSubCommand() instanceof RemoveRemoteTag
            )
            .count();

        Assertions
            .assertThat(localTagsAfterDeletion)
            .isEqualTo(localTagsToBeRemoved.size());
        Assertions
            .assertThat(remoteTagsAfterDeletion)
            .isEqualTo(remoteTagsToBeRemoved.size());
        Assertions.assertThat(tagsCacheResult).isNull();
    }

    @Test
    void storeWfSpecWithBooleanVariables() {
        WfSpec wfSpec = TestUtil.wfSpec("test-name");
        String expectedStoreKey = "test-name/00000";
        String expectedTagId1 = "WF_SPEC/__taskDef_input-name1/";
        String expectedTagId2 = "WF_SPEC/__taskDef_input-name2/";
        ThreadSpec threadSpec1 = TestUtil.threadSpec();
        threadSpec1
            .getNodes()
            .forEach((s, node) -> {
                node.getTaskNode().setTaskDefName("input-name1");
            });
        ThreadSpec threadSpec2 = TestUtil.threadSpec();
        threadSpec2
            .getNodes()
            .forEach((s, node) -> {
                node.getTaskNode().setTaskDefName("input-name2");
            });
        wfSpec.setThreadSpecs(
            Map.of("thread-1", threadSpec1, "thread-2", threadSpec2)
        );
        geTableStorageManager.store(wfSpec);
        Assertions
            .assertThat(localStoreWrapper.get(expectedStoreKey, WfSpec.class))
            .isNotNull();
        TagsCache tagsCacheResult = localStoreWrapper.getTagsCache(
            wfSpec.getStoreKey(),
            WfSpec.class
        );
        Assertions.assertThat(tagsCacheResult.getTagIds()).hasSize(2);
        for (String tagId : tagsCacheResult.getTagIds()) {
            Assertions
                .assertThat(tagId)
                .containsAnyOf(expectedTagId1, expectedTagId2);
        }
    }

    private static Stream<Arguments> provideGetableObjectsAndIds() {
        WfRun wfRun = TestUtil.wfRun("0000000");
        Variable variable = TestUtil.variable("0000000");
        WfSpec wfSpec = TestUtil.wfSpec("testWfSpecName");
        ThreadSpec threadSpec1 = TestUtil.threadSpec();
        threadSpec1
            .getNodes()
            .forEach((s, node) -> {
                node.getTaskNode().setTaskDefName("input-name1");
            });
        ThreadSpec threadSpec2 = TestUtil.threadSpec();
        threadSpec2
            .getNodes()
            .forEach((s, node) -> {
                node.getTaskNode().setTaskDefName("input-name2");
            });
        wfSpec.setThreadSpecs(
            Map.of("thread-1", threadSpec1, "thread-2", threadSpec2)
        );
        return Stream.of(
            Arguments.of(wfRun, wfRun.getObjectId().getStoreKey(), 3),
            Arguments.of(variable, variable.getObjectId().getStoreKey(), 1),
            Arguments.of(wfSpec, wfSpec.getObjectId().getStoreKey(), 2)
        );
    }
}

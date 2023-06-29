package io.littlehorse.io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.GETableStorageManager;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
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
public class GETableStorageManagerTest {

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
    private GETableStorageManager geTableStorageManager;

    @BeforeEach
    void setup() {
        localStoreWrapper = new LHStoreWrapper(store, lhConfig);
        geTableStorageManager =
            new GETableStorageManager(
                localStoreWrapper,
                lhConfig,
                mockProcessorContext
            );
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    @ParameterizedTest
    @MethodSource("provideGetableObjectsAndIds")
    void storeNewGetableWithTags(
        GETable<?> getable,
        String storeKey,
        int expectedTagsCount
    ) {
        geTableStorageManager.store(getable);
        Assertions
            .assertThat(localStoreWrapper.get(storeKey, getable.getClass()))
            .isNotNull();
        TagsCache tagsCacheResult = localStoreWrapper.getTagsCache(getable);
        Assertions.assertThat(tagsCacheResult).isNotNull();
        Assertions.assertThat(tagsCacheResult.getTagIds()).hasSize(expectedTagsCount);
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
        TagsCache tagsCacheResult = localStoreWrapper.getTagsCache(wfSpec);
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

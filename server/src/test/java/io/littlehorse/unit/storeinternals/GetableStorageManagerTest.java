package io.littlehorse.unit.storeinternals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.JsonIndexModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.jetbrains.annotations.NotNull;
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

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHServerConfig lhConfig;

    private RocksDBWrapper localStoreWrapper;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private GetableStorageManager getableStorageManager;

    @BeforeEach
    void setup() {
        localStoreWrapper = new RocksDBWrapper(store, lhConfig);
        getableStorageManager =
                new GetableStorageManager(localStoreWrapper, mockProcessorContext, lhConfig, mock(), mock());
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    @ParameterizedTest
    @MethodSource("provideGetableObjectsAndIds")
    void storeNewGetableWithTags(CoreGetable<?> getable, int expectedTagsCount) {
        getableStorageManager.put(getable);
        getableStorageManager.commit();

        final var keys = getAllKeys(store);
        assertThat(localStoreWrapper.get(getable.getObjectId())).isNotNull();
        assertThat(keys).hasSize(1 + expectedTagsCount);
    }

    @Test
    void deleteGetableAndTags() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        getableStorageManager.put(wfRunModel);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("3/0000000", StoredGetable.class)).isNotNull();
        List<String> keysBeforeDelete = getAllKeys(store);
        assertThat(keysBeforeDelete)
                .hasSize(4)
                .anyMatch(key -> key.contains("0/3/0000000"))
                .anyMatch(key -> key.contains("5/3/__wfSpecName_test-spec-name"))
                .anyMatch(key -> key.contains("5/3/__wfSpecName_test-spec-name__status_RUNNING"))
                .anyMatch(key -> key.contains("5/3/__wfSpecName_test-spec-name__status_RUNNING__wfSpecVersion_00000"));

        getableStorageManager.delete(wfRunModel.getObjectId());
        getableStorageManager.commit();

        List<String> keysAfterDelete = getAllKeys(store);
        assertThat(keysAfterDelete).isEmpty();
    }

    @NotNull
    private List<String> getAllKeys(KeyValueStore<String, Bytes> store) {
        KeyValueIterator<String, Bytes> all = store.all();
        List<String> keys = new LinkedList<>();
        while (all.hasNext()) {
            keys.add(all.next().key);
        }
        return keys;
    }

    @Test
    void storeBooleanVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.BOOL);
        variable.getValue().setBoolVal(true);
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.BOOL);
            threadSpec.setVariableDefs(List.of(variableDef1));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(2)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000"));
    }

    @Test
    void storeLocalStringVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeLocal");
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.STR);
            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(2)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains(
                        "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_ThisShouldBeLocal"));
    }

    @Test
    void storeRemoteStringVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeRemote");
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.STR);
            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });
        String expectedTagKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_ThisShouldBeRemote";

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        List<RepartitionCommand> repartitionCommands = mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .filter(repartitionCommand -> repartitionCommand.getSubCommand() instanceof CreateRemoteTag)
                .toList();
        assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        assertThat(repartitionCommand.getSubCommand().getPartitionKey()).isEqualTo(expectedTagKey);
    }

    @Test
    void storeLocalIntVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.INT);
        variable.getValue().setIntVal(20L);
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.INT);
            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(2)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_20"));
    }

    @Test
    void storeRemoteIntVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.INT);
        variable.getValue().setIntVal(20L);
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.INT);
            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });
        String expectedTagKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_20";

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        List<RepartitionCommand> repartitionCommands = mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .filter(repartitionCommand -> repartitionCommand.getSubCommand() instanceof CreateRemoteTag)
                .toList();
        assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        assertThat(repartitionCommand.getSubCommand().getPartitionKey()).isEqualTo(expectedTagKey);
    }

    @Test
    void storeLocalDoubleVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.DOUBLE);
        variable.getValue().setDoubleVal(21.0);
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.DOUBLE);
            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(2)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0"));
    }

    @Test
    void storeRemoteDoubleVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.DOUBLE);
        variable.getValue().setDoubleVal(21.0);
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.DOUBLE);
            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0";

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        List<RepartitionCommand> repartitionCommands = mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .filter(repartitionCommand -> repartitionCommand.getSubCommand() instanceof CreateRemoteTag)
                .toList();
        assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        assertThat(repartitionCommand.getSubCommand().getPartitionKey()).isEqualTo(expectedStoreKey);
    }

    private List<RepartitionCommand> remoteTagsCreated() {
        return mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .filter(repartitionCommand -> repartitionCommand.getSubCommand() instanceof CreateRemoteTag)
                .toList();
    }

    @Test
    void storeLocalJsonVariablesWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.JSON_OBJ);
        variable.getValue()
                .setJsonObjVal(Map.of("name", "test", "age", 20, "car", Map.of("brand", "Ford", "model", "Escape")));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.JSON_OBJ);
            List<JsonIndexModel> indices = List.of(
                    new JsonIndexModel("$.name", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.age", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.car.brand", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.car.model", IndexType.LOCAL_INDEX));
            variableDef1.setJsonIndices(indices);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(5)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.name_test"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.age_20"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.brand_Ford"))
                .anyMatch(
                        key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.model_Escape"));
    }

    @Test
    void storeRemoteJsonVariablesWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.JSON_OBJ);
        variable.getValue()
                .setJsonObjVal(Map.of("name", "test", "age", 20, "car", Map.of("brand", "Ford", "model", "Escape")));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.JSON_OBJ);
            List<JsonIndexModel> indices = List.of(
                    new JsonIndexModel("$.name", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.age", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.car.brand", IndexType.LOCAL_INDEX),
                    new JsonIndexModel("$.car.model", IndexType.REMOTE_INDEX));
            variableDef1.setJsonIndices(indices);
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
        });

        getableStorageManager.put(variable);
        getableStorageManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var storedKeys = getAllKeys(store);
        assertThat(storedKeys)
                .hasSize(4)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.name_test"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.age_20"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.brand_Ford"));

        List<String> remoteTagsCreated = remoteTagsCreated().stream()
                .map(RepartitionCommand::getSubCommand)
                .map(RepartitionSubCommand::getPartitionKey)
                .toList();

        assertThat(remoteTagsCreated)
                .containsExactlyInAnyOrder("5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.model_Escape");
    }

    @ParameterizedTest
    @MethodSource("provideNodeRunObjects")
    void storeNodeRun(
            NodeRunModel nodeRunModel, List<Pair<String, TagStorageType>> expectedTagKeys, String expectedStoreKey) {

        List<String> expectedLocalTagKeys = expectedTagKeys.stream()
                .filter(stringTagStorageTypePbPair -> stringTagStorageTypePbPair.getValue() == TagStorageType.LOCAL)
                .map(Pair::getKey)
                .toList();

        List<String> expectedRemoteStoreKeys = expectedTagKeys.stream()
                .filter(stringTagStorageTypePbPair -> stringTagStorageTypePbPair.getValue() == TagStorageType.REMOTE)
                .map(Pair::getKey)
                .toList();

        getableStorageManager.put(nodeRunModel);
        getableStorageManager.commit();

        final var storedKeys = getAllKeys(store);
        assertThat(storedKeys).hasSize(expectedLocalTagKeys.size() + 1).anyMatch(key -> key.contains(expectedStoreKey));
        expectedLocalTagKeys.forEach(
                expectedTagKey -> assertThat(storedKeys).anyMatch(key -> key.contains(expectedStoreKey)));

        List<String> remoteTags = remoteTagsCreated().stream()
                .map(RepartitionCommand::getSubCommand)
                .map(RepartitionSubCommand::getPartitionKey)
                .toList();
        assertThat(remoteTags).containsExactlyInAnyOrderElementsOf(expectedRemoteStoreKeys);
    }

    private static Stream<Arguments> provideNodeRunObjects() {
        NodeRunModel nodeRunModel = TestUtil.nodeRun();
        nodeRunModel.setType(NodeRun.NodeTypeCase.TASK);
        return Stream.of(Arguments.of(
                nodeRunModel,
                List.of(Pair.of("4/__status_RUNNING__type_TASK", TagStorageType.LOCAL)),
                "4/0000000/1/0"));
    }

    private static Stream<Arguments> provideGetableObjectsAndIds() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        TaskRunModel taskRun = TestUtil.taskRun();
        VariableModel variable = TestUtil.variable("0000000");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeLocal");
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setType(VariableType.STR);
            threadSpec.setVariableDefs(List.of(variableDef1));
        });
        ExternalEventModel externalEvent = TestUtil.externalEvent();
        ThreadSpecModel threadSpecModel1 = TestUtil.threadSpec();
        threadSpecModel1.getNodes().forEach((s, node) -> {
            node.getTaskNode().setTaskDefName("input-name1");
        });
        ThreadSpecModel threadSpecModel2 = TestUtil.threadSpec();
        threadSpecModel2.getNodes().forEach((s, node) -> {
            node.getTaskNode().setTaskDefName("input-name2");
        });
        return Stream.of(
                Arguments.of(wfRunModel, 3),
                Arguments.of(taskRun, 2),
                Arguments.of(variable, 1),
                Arguments.of(externalEvent, 2));
    }
}

package io.littlehorse.io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.JsonIndexModel;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.server.streamsimpl.storeinternals.GetableStorageManager;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.Pair;
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

    private final KeyValueStore<String, Bytes> store =
            Stores.keyValueStoreBuilder(
                            Stores.inMemoryKeyValueStore("myStore"),
                            Serdes.String(),
                            Serdes.Bytes())
                    .withLoggingDisabled()
                    .build();

    @Mock private LHConfig lhConfig;

    private LHStoreWrapper localStoreWrapper;

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private GetableStorageManager geTableStorageManager;

    @BeforeEach
    void setup() {
        localStoreWrapper = new LHStoreWrapper(store, lhConfig);
        geTableStorageManager =
                new GetableStorageManager(localStoreWrapper, lhConfig, mockProcessorContext);
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    @ParameterizedTest
    @MethodSource("provideGetableObjectsAndIds")
    void storeNewGetableWithTags(Getable<?> getable, String storeKey, int expectedTagsCount) {
        geTableStorageManager.store(getable);
        Assertions.assertThat(localStoreWrapper.get(storeKey, getable.getClass())).isNotNull();
        TagsCache tagsCacheResult =
                localStoreWrapper.getTagsCache(
                        getable.getStoreKey(), (Class<? extends Getable<?>>) getable.getClass());
        Assertions.assertThat(tagsCacheResult).isNotNull();
        Assertions.assertThat(tagsCacheResult.getTagIds()).hasSize(expectedTagsCount);
    }

    @Test
    void deleteGetableWithTags() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        geTableStorageManager.store(wfRunModel);
        TagsCache tagsCache =
                localStoreWrapper.getTagsCache(wfRunModel.getStoreKey(), WfRunModel.class);
        Map<Boolean, List<Tag>> localOrRemoteTags =
                tagsCache.getTagIds().stream()
                        .map(s -> localStoreWrapper.get(s, Tag.class))
                        .collect(Collectors.groupingBy(Objects::nonNull));

        geTableStorageManager.deleteGetable(wfRunModel);
        Assertions.assertThat(localStoreWrapper.get(wfRunModel.getStoreKey(), WfRunModel.class))
                .isNull();
        TagsCache tagsCacheResult =
                localStoreWrapper.getTagsCache(wfRunModel.getStoreKey(), WfRunModel.class);
        List<Tag> localTagsToBeRemoved = localOrRemoteTags.get(true);

        long localTagsAfterDeletion =
                localTagsToBeRemoved.stream()
                        .map(Tag::getStoreKey)
                        .map(s -> localStoreWrapper.get(s, Tag.class))
                        .filter(Objects::isNull)
                        .count();

        Assertions.assertThat(localTagsAfterDeletion).isEqualTo(localTagsToBeRemoved.size());
        Assertions.assertThat(tagsCacheResult).isNull();
    }

    @Test
    void storeWfSpecWithBooleanVariables() {
        WfSpecModel wfSpecModel = TestUtil.wfSpec("test-name");
        String expectedStoreKey = "test-name/00000";
        String expectedTagId1 = "2/__taskDef_input-name1/";
        String expectedTagId2 = "2/__taskDef_input-name2/";
        ThreadSpecModel threadSpecModel1 = TestUtil.threadSpec();
        threadSpecModel1
                .getNodes()
                .forEach(
                        (s, node) -> {
                            node.getTaskNode().setTaskDefName("input-name1");
                        });
        ThreadSpecModel threadSpecModel2 = TestUtil.threadSpec();
        threadSpecModel2
                .getNodes()
                .forEach(
                        (s, node) -> {
                            node.getTaskNode().setTaskDefName("input-name2");
                        });
        wfSpecModel.setThreadSpecs(
                Map.of("thread-1", threadSpecModel1, "thread-2", threadSpecModel2));
        geTableStorageManager.store(wfSpecModel);
        Assertions.assertThat(localStoreWrapper.get(expectedStoreKey, WfSpecModel.class))
                .isNotNull();
        TagsCache tagsCacheResult =
                localStoreWrapper.getTagsCache(wfSpecModel.getStoreKey(), WfSpecModel.class);
        Assertions.assertThat(tagsCacheResult.getTagIds()).hasSize(2);
        for (String tagId : tagsCacheResult.getTagIds()) {
            Assertions.assertThat(tagId).containsAnyOf(expectedTagId1, expectedTagId2);
        }
    }

    @Test
    void storeBooleanVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.BOOL);
        variable.getValue().setBoolVal(true);
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.BOOL);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.BOOL);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey = "";
        geTableStorageManager.store(variable);
        Assertions.assertThat(this.localTagScan(expectedStoreKey))
                .allMatch(
                        tag -> {
                            Assertions.assertThat(tag).isNotNull();
                            return true;
                        });
    }

    @Test
    void storeLocalStringVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeLocal");
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.STR);
                            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_ThisShouldBeLocal";
        geTableStorageManager.store(variable);
        Assertions.assertThat(this.localTagScan(expectedStoreKey))
                .allMatch(
                        tag -> {
                            Assertions.assertThat(tag).isNotNull();
                            return true;
                        });
    }

    @Test
    void storeRemoteStringVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeRemote");
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.STR);
                            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_ThisShouldBeRemote";
        geTableStorageManager.store(variable);
        List<RepartitionCommand> repartitionCommands =
                mockProcessorContext.forwarded().stream()
                        .map(MockProcessorContext.CapturedForward::record)
                        .map(Record::value)
                        .map(CommandProcessorOutput::getPayload)
                        .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                        .filter(
                                repartitionCommand ->
                                        repartitionCommand.getSubCommand()
                                                instanceof CreateRemoteTag)
                        .toList();
        Assertions.assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        Assertions.assertThat(repartitionCommand.getSubCommand().getPartitionKey())
                .isEqualTo(expectedStoreKey);
    }

    @Test
    void storeLocalIntVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.INT);
        variable.getValue().setIntVal(20L);
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.INT);
                            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_20";
        geTableStorageManager.store(variable);
        Assertions.assertThat(this.localTagScan(expectedStoreKey))
                .allMatch(
                        tag -> {
                            Assertions.assertThat(tag).isNotNull();
                            return true;
                        });
    }

    @Test
    void storeRemoteIntVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.INT);
        variable.getValue().setIntVal(20L);
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.INT);
                            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_20";
        geTableStorageManager.store(variable);
        List<RepartitionCommand> repartitionCommands =
                mockProcessorContext.forwarded().stream()
                        .map(MockProcessorContext.CapturedForward::record)
                        .map(Record::value)
                        .map(CommandProcessorOutput::getPayload)
                        .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                        .filter(
                                repartitionCommand ->
                                        repartitionCommand.getSubCommand()
                                                instanceof CreateRemoteTag)
                        .toList();
        Assertions.assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        Assertions.assertThat(repartitionCommand.getSubCommand().getPartitionKey())
                .isEqualTo(expectedStoreKey);
    }

    @Test
    void storeLocalDoubleVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.DOUBLE);
        variable.getValue().setDoubleVal(21.0);
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.DOUBLE);
                            variableDef1.setIndexType(IndexType.LOCAL_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0";
        geTableStorageManager.store(variable);
        Assertions.assertThat(this.localTagScan(expectedStoreKey))
                .allMatch(
                        tag -> {
                            Assertions.assertThat(tag).isNotNull();
                            return true;
                        });
    }

    @Test
    void storeRemoteDoubleVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.DOUBLE);
        variable.getValue().setDoubleVal(21.0);
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.DOUBLE);
                            variableDef1.setIndexType(IndexType.REMOTE_INDEX);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__variableName_21.0";
        geTableStorageManager.store(variable);
        List<RepartitionCommand> repartitionCommands =
                mockProcessorContext.forwarded().stream()
                        .map(MockProcessorContext.CapturedForward::record)
                        .map(Record::value)
                        .map(CommandProcessorOutput::getPayload)
                        .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                        .filter(
                                repartitionCommand ->
                                        repartitionCommand.getSubCommand()
                                                instanceof CreateRemoteTag)
                        .toList();
        Assertions.assertThat(repartitionCommands).hasSize(1);
        RepartitionCommand repartitionCommand = repartitionCommands.get(0);
        Assertions.assertThat(repartitionCommand.getSubCommand().getPartitionKey())
                .isEqualTo(expectedStoreKey);
    }

    private List<RepartitionCommand> remoteTagsCreated() {
        return mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .filter(
                        repartitionCommand ->
                                repartitionCommand.getSubCommand() instanceof CreateRemoteTag)
                .toList();
    }

    @Test
    void storeLocalJsonVariablesWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.JSON_OBJ);
        variable.getValue()
                .setJsonObjVal(
                        Map.of(
                                "name",
                                "test",
                                "age",
                                20,
                                "car",
                                Map.of("brand", "Ford", "model", "Escape")));
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.JSON_OBJ);
                            List<JsonIndexModel> indices =
                                    List.of(
                                            new JsonIndexModel("$.name", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel("$.age", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel(
                                                    "$.car.brand", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel(
                                                    "$.car.model", IndexType.LOCAL_INDEX));
                            variableDef1.setJsonIndices(indices);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey1 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.name_test";
        String expectedStoreKey2 = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.age_20";
        String expectedStoreKey3 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.brand_Ford";
        String expectedStoreKey4 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.model_Escape";
        geTableStorageManager.store(variable);
        List<String> storedTags =
                localTagScan("5/")
                        .map(LHIterKeyValue::getValue)
                        .map(Tag::getStoreKey)
                        .map(s -> s.split("/"))
                        .map(strings -> strings[0] + "/" + strings[1])
                        .toList();
        Assertions.assertThat(storedTags)
                .containsExactlyInAnyOrder(
                        expectedStoreKey1, expectedStoreKey2, expectedStoreKey3, expectedStoreKey4);
    }

    @Test
    void storeRemoteJsonVariablesWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.JSON_OBJ);
        variable.getValue()
                .setJsonObjVal(
                        Map.of(
                                "name",
                                "test",
                                "age",
                                20,
                                "car",
                                Map.of("brand", "Ford", "model", "Escape")));
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.JSON_OBJ);
                            List<JsonIndexModel> indices =
                                    List.of(
                                            new JsonIndexModel("$.name", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel("$.age", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel(
                                                    "$.car.brand", IndexType.LOCAL_INDEX),
                                            new JsonIndexModel(
                                                    "$.car.model", IndexType.REMOTE_INDEX));
                            variableDef1.setJsonIndices(indices);
                            VariableDefModel variableDef2 = new VariableDefModel();
                            variableDef2.setName("variableName2");
                            variableDef2.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1, variableDef2));
                        });
        String expectedStoreKey1 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.name_test";
        String expectedStoreKey2 = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.age_20";
        String expectedStoreKey3 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.brand_Ford";
        String expectedStoreKey4 =
                "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.car.model_Escape";
        geTableStorageManager.store(variable);
        List<String> remoteTagsCreated =
                remoteTagsCreated().stream()
                        .map(RepartitionCommand::getSubCommand)
                        .map(RepartitionSubCommand::getPartitionKey)
                        .toList();

        List<String> storedTags =
                localTagScan("5/")
                        .map(LHIterKeyValue::getValue)
                        .map(Tag::getStoreKey)
                        .map(s -> s.split("/"))
                        .map(strings -> strings[0] + "/" + strings[1])
                        .toList();
        Assertions.assertThat(storedTags)
                .containsExactlyInAnyOrder(expectedStoreKey1, expectedStoreKey2, expectedStoreKey3);
        Assertions.assertThat(remoteTagsCreated).containsExactlyInAnyOrder(expectedStoreKey4);
    }

    @ParameterizedTest
    @MethodSource("provideNodeRunObjects")
    void storeNodeRun(
            NodeRunModel nodeRunModel,
            List<Pair<String, TagStorageType>> expectedStoreKeys,
            NodeRun.NodeTypeCase nodeTypeCase) {
        List<String> expectedLocalStoreKeys =
                expectedStoreKeys.stream()
                        .filter(
                                stringTagStorageTypePbPair ->
                                        stringTagStorageTypePbPair.getValue()
                                                == TagStorageType.LOCAL)
                        .map(Pair::getKey)
                        .toList();
        List<String> expectedRemoteStoreKeys =
                expectedStoreKeys.stream()
                        .filter(
                                stringTagStorageTypePbPair ->
                                        stringTagStorageTypePbPair.getValue()
                                                == TagStorageType.REMOTE)
                        .map(Pair::getKey)
                        .toList();
        nodeRunModel.setType(nodeTypeCase);
        geTableStorageManager.store(nodeRunModel);
        List<String> localTags =
                localTagScan("4/")
                        .map(LHIterKeyValue::getValue)
                        .map(Tag::getStoreKey)
                        .map(s -> s.split("/"))
                        .map(strings -> strings[0] + "/" + strings[1])
                        .toList();
        Assertions.assertThat(localTags)
                .containsExactlyInAnyOrderElementsOf(expectedLocalStoreKeys);
        List<String> remoteTags =
                remoteTagsCreated().stream()
                        .map(RepartitionCommand::getSubCommand)
                        .map(RepartitionSubCommand::getPartitionKey)
                        .toList();
        Assertions.assertThat(remoteTags)
                .containsExactlyInAnyOrderElementsOf(expectedRemoteStoreKeys);
    }

    private static Stream<Arguments> provideNodeRunObjects() {
        NodeRunModel nodeRunModel = TestUtil.nodeRun();
        return Stream.of(
                Arguments.of(
                        nodeRunModel,
                        List.of(Pair.of("4/__status_RUNNING__type_TASK", TagStorageType.LOCAL)),
                        NodeRun.NodeTypeCase.TASK));
    }

    private Stream<LHIterKeyValue<Tag>> localTagScan(String keyPrefix) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        localStoreWrapper.prefixScan(keyPrefix, Tag.class), Spliterator.ORDERED),
                false);
    }

    private static Stream<Arguments> provideGetableObjectsAndIds() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        TaskRunModel taskRun = TestUtil.taskRun();
        VariableModel variable = TestUtil.variable("0000000");
        variable.setName("variableName");
        variable.getValue().setType(VariableType.STR);
        variable.getValue().setStrVal("ThisShouldBeLocal");
        variable.getWfSpecModel()
                .getThreadSpecs()
                .forEach(
                        (s, threadSpec) -> {
                            VariableDefModel variableDef1 = new VariableDefModel();
                            variableDef1.setName("variableName");
                            variableDef1.setType(VariableType.STR);
                            threadSpec.setVariableDefs(List.of(variableDef1));
                        });
        ExternalEventModel externalEvent = TestUtil.externalEvent();
        WfSpecModel wfSpecModel = TestUtil.wfSpec("testWfSpecName");
        ThreadSpecModel threadSpecModel1 = TestUtil.threadSpec();
        threadSpecModel1
                .getNodes()
                .forEach(
                        (s, node) -> {
                            node.getTaskNode().setTaskDefName("input-name1");
                        });
        ThreadSpecModel threadSpecModel2 = TestUtil.threadSpec();
        threadSpecModel2
                .getNodes()
                .forEach(
                        (s, node) -> {
                            node.getTaskNode().setTaskDefName("input-name2");
                        });
        wfSpecModel.setThreadSpecs(
                Map.of("thread-1", threadSpecModel1, "thread-2", threadSpecModel2));
        return Stream.of(
                Arguments.of(wfRunModel, wfRunModel.getObjectId().getStoreKey(), 3),
                Arguments.of(taskRun, taskRun.getObjectId().getStoreKey(), 2),
                Arguments.of(variable, variable.getObjectId().getStoreKey(), 1),
                Arguments.of(wfSpecModel, wfSpecModel.getObjectId().getStoreKey(), 2),
                Arguments.of(externalEvent, externalEvent.getObjectId().getStoreKey(), 2));
    }
}

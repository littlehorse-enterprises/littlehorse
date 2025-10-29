package io.littlehorse.storeinternals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.TestUtil;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.AuthorizationContextImpl;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.JsonIndexModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetableManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHServerConfig lhConfig;

    private String tenantId = "myTenant";

    private TenantScopedStore localStoreWrapper;

    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();
    private GetableManager getableManager;

    @Mock
    private CoreProcessorContext executionContext;

    private AuthorizationContext testContext = new AuthorizationContextImpl(
            new PrincipalIdModel("my-principal-id"), new TenantIdModel(tenantId), List.of(), false);

    @BeforeEach
    void setup() {
        localStoreWrapper = TenantScopedStore.newInstance(store, new TenantIdModel(tenantId), executionContext);
        getableManager =
                new GetableManager(localStoreWrapper, mockProcessorContext, lhConfig, mock(), executionContext, null);
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    @ParameterizedTest
    @MethodSource("provideGetableObjectsAndIds")
    void storeNewGetableWithTags(CoreGetable<?> getable, int expectedTagsCount) {
        getableManager.put(getable);
        getableManager.commit();

        final List<String> keys = getAllKeys(store);
        assertThat(localStoreWrapper.get(getable.getObjectId().getStoreableKey(), StoredGetable.class))
                .isNotNull();
        assertThat(keys).hasSize(1 + expectedTagsCount);
    }

    @Test
    void deleteGetableAndTags() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        wfRunModel.status = LHStatus.RUNNING;
        getableManager.put(wfRunModel);
        getableManager.commit();

        assertThat(localStoreWrapper.get("3/0000000", StoredGetable.class)).isNotNull();
        List<String> keysBeforeDelete = getAllKeys(store);
        assertThat(keysBeforeDelete)
                .hasSize(7)
                .anyMatch(key -> key.contains("0/3/0000000"))
                .anyMatch(key -> key.contains("5/3/__majorVersion_test-spec-name"))
                .anyMatch(key -> key.contains("5/3/__majorVersion_test-spec-name/00000__status_RUNNING"))
                .anyMatch(key -> key.contains("5/3/__wfSpecName_test-spec-name"))
                .anyMatch(key -> key.contains("5/3/__wfSpecName_test-spec-name__status_RUNNING"))
                .anyMatch(key -> key.contains("5/3/__wfSpecId_test-spec-name/00000/00000"))
                .anyMatch(key -> key.contains("5/3/__wfSpecId_test-spec-name/00000/00000__status_RUNNING"));
        getableManager.get(wfRunModel.getObjectId());
        getableManager.delete(wfRunModel.getObjectId());
        getableManager.commit();

        List<String> keysAfterDelete = getAllKeys(store);
        assertThat(keysAfterDelete).isEmpty();
    }

    @Test
    void deleteAllByPrefix() {
        WfRunModel wfRunModel = TestUtil.wfRun("1234");
        TaskRunModel taskRunModel = TestUtil.taskRun();

        getableManager.put(taskRunModel);
        getableManager.commit();

        getableManager.deleteAllByPrefix(wfRunModel.getPartitionKey().get(), TaskRunModel.class);
        getableManager.commit();

        TaskRunModel storedTaskRunModel = getableManager.get(taskRunModel.getObjectId());
        assertThat(storedTaskRunModel).isNull();
    }

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
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel(true));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.BOOL));
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        getableManager.put(variable);
        getableManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(4)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName_true"))
                .anyMatch(key -> key.contains("5/5/__majorVersion_testWfSpecName/00000__variableName_true"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__variableName_true"));
    }

    @Test
    void storeLocalStringVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel("ThisShouldBeLocal"));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(List.of(
                    new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR),
                    new ThreadVarDefModel(variableDef2, false, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        getableManager.put(variable);
        getableManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(4)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__majorVersion_testWfSpecName/00000__variableName_ThisShouldBeLocal"))
                .anyMatch(
                        key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName_ThisShouldBeLocal"));
    }

    @Test
    void storeLocalIntVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel(20L));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.INT));
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(List.of(
                    new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR),
                    new ThreadVarDefModel(variableDef2, false, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        getableManager.put(variable);
        getableManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(4)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName_20"))
                .anyMatch(key -> key.contains("5/__majorVersion_testWfSpecName/00000__variableName_20"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__variableName_20"));
    }

    @Test
    void storeLocalDoubleVariableWithUserDefinedStorageType() {
        VariableModel variable = TestUtil.variable("test-id");
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel(21.0));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.DOUBLE));
            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(List.of(
                    new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR),
                    new ThreadVarDefModel(variableDef2, false, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        getableManager.put(variable);
        getableManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(4)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName_21.0"))
                .anyMatch(key -> key.contains("5/__majorVersion_testWfSpecName/00000__variableName_21.0"))
                .anyMatch(key -> key.contains("5/__wfSpecName_testWfSpecName__variableName_21.0"));
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
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel(
                Map.of("name", "test", "age", 20, "car", Map.of("brand", "Ford", "model", "Escape"))));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.JSON_OBJ));
            List<JsonIndexModel> indices = List.of(
                    new JsonIndexModel("$.name", VariableType.STR),
                    new JsonIndexModel("$.age", VariableType.INT),
                    new JsonIndexModel("$.car.brand", VariableType.STR),
                    new JsonIndexModel("$.car.model", VariableType.STR));

            VariableDefModel variableDef2 = new VariableDefModel();
            variableDef2.setName("variableName2");
            variableDef2.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(List.of(
                    new ThreadVarDefModel(variableDef1, indices, false, WfRunVariableAccessLevel.PRIVATE_VAR),
                    new ThreadVarDefModel(variableDef2, true, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        getableManager.put(variable);
        getableManager.commit();

        assertThat(localStoreWrapper.get("5/test-id/0/variableName", StoredGetable.class))
                .isNotNull();

        final var keys = getAllKeys(store);
        assertThat(keys)
                .hasSize(13)
                .anyMatch(key -> key.contains("5/test-id/0/variableName"))
                .anyMatch(key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName\\_$.name_test"))
                .anyMatch(key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName\\_$.age_20"))
                .anyMatch(
                        key -> key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName\\_$.car.brand_Ford"))
                .anyMatch(key ->
                        key.contains("5/__wfSpecId_testWfSpecName/00000/00000__variableName\\_$.car.model_Escape"));
    }

    @ParameterizedTest
    @MethodSource("provideNodeRunObjects")
    void storeNodeRun(NodeRunModel nodeRunModel, String expectedStoreKey) {
        List<String> expectedLocalTagKeys = List.of();
        List<String> expectedRemoteStoreKeys = List.of();

        getableManager.put(nodeRunModel);
        getableManager.commit();

        final var storedKeys = getAllKeys(store);
        assertThat(storedKeys).hasSize(expectedLocalTagKeys.size() + 1).anyMatch(key -> key.contains(expectedStoreKey));

        List<String> remoteTags = remoteTagsCreated().stream()
                .map(RepartitionCommand::getSubCommand)
                .map(RepartitionSubCommand::getPartitionKey)
                .toList();
        assertThat(remoteTags).containsExactlyInAnyOrderElementsOf(expectedRemoteStoreKeys);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findUnclaimedEvents(boolean useInMemoryBuffer) {
        WfRunIdModel wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        VariableValueModel content = new VariableValueModel();
        NodeRunIdModel nodeRunId = new NodeRunIdModel(wfRunId, 1, 2);
        ExternalEventDefIdModel externalEventDefId =
                new ExternalEventDefIdModel(UUID.randomUUID().toString());
        String guid = LHUtil.generateGuid();
        int threadRunNumber = 1;
        int nodeRunPosition = 2;
        ExternalEventModel event = new ExternalEventModel(
                content,
                new ExternalEventIdModel(wfRunId, externalEventDefId, guid),
                threadRunNumber,
                nodeRunPosition,
                new Date());
        getableManager.put(event);
        if (!useInMemoryBuffer) {
            getableManager.commit();
        }
        ExternalEventModel unclaimedEvent = getableManager.getUnclaimedEvent(nodeRunId, externalEventDefId);
        assertThat(unclaimedEvent).isNotNull();
        assertThat(unclaimedEvent.getId().getExternalEventDefId()).isEqualTo(externalEventDefId);
        assertThat(unclaimedEvent.getId().getWfRunId()).isEqualTo(wfRunId);
        assertThat(unclaimedEvent.isClaimed()).isFalse();
    }

    @Test
    void findFirstUnclaimedEvents() {
        WfRunIdModel wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        VariableValueModel content = new VariableValueModel();
        ExternalEventDefIdModel externalEventDefId =
                new ExternalEventDefIdModel(UUID.randomUUID().toString());
        NodeRunIdModel nodeRunId = new NodeRunIdModel(wfRunId, 1, 2);
        int threadRunNumber = 1;
        int nodeRunPosition = 2;
        ExternalEventModel expectedEvent = new ExternalEventModel(
                content,
                new ExternalEventIdModel(wfRunId, externalEventDefId, "expectedEvent"),
                threadRunNumber,
                nodeRunPosition,
                new Date(1));
        ExternalEventModel olderEvent = new ExternalEventModel(
                content,
                new ExternalEventIdModel(wfRunId, externalEventDefId, "olderEvent"),
                threadRunNumber,
                ++nodeRunPosition,
                new Date());
        getableManager.put(expectedEvent);
        getableManager.put(olderEvent);
        getableManager.commit();
        ExternalEventModel firstUnclaimedEvent = getableManager.getUnclaimedEvent(nodeRunId, externalEventDefId);
        assertThat(firstUnclaimedEvent).isNotNull();
        assertThat(firstUnclaimedEvent.getId().getGuid())
                .isEqualTo(expectedEvent.getId().getGuid());
    }

    @Test
    void respectTheNodeRunNumberOnExternalEvent() {
        WfRunIdModel wfRunId = new WfRunIdModel(UUID.randomUUID().toString());
        VariableValueModel content = new VariableValueModel();
        ExternalEventDefIdModel externalEventDefId =
                new ExternalEventDefIdModel(UUID.randomUUID().toString());
        NodeRunIdModel nodeRunId = new NodeRunIdModel(wfRunId, 0, 1);
        int threadRunNumber = 1;
        int nodeRunPosition = 2;
        ExternalEventModel fooEvent = new ExternalEventModel(
                content,
                new ExternalEventIdModel(wfRunId, externalEventDefId, "expectedEvent"),
                threadRunNumber,
                nodeRunPosition,
                new Date(1));
        getableManager.put(fooEvent);
        getableManager.commit();
        ExternalEventModel firstUnclaimedEvent = getableManager.getUnclaimedEvent(nodeRunId, externalEventDefId);
        assertThat(firstUnclaimedEvent).isNull();
    }

    @Test
    public void findScheduledTaskByTaskRun() {
        ScheduledTaskModel scheduledTask = TestUtil.scheduledTaskModel("wf-1");
        scheduledTask.setCreatedAt(new Date(new Date().getTime() + 2000L));
        TaskRunModel taskRun = TestUtil.taskRun(scheduledTask.getTaskRunId(), new TaskDefIdModel("asdf"));
        getableManager.put(taskRun);
        taskRun.setAttempts(List.of(new TaskAttemptModel()));
        Date taskCreatedAt = new Date();
        taskRun.getAttempts().get(0).setScheduleTime(taskCreatedAt);
        scheduledTask.setCreatedAt(taskCreatedAt);
        localStoreWrapper.put(scheduledTask);
        ScheduledTaskModel result = getableManager.getScheduledTask(taskRun.getId());
        assertThat(result).isNotNull();
    }

    private static Stream<Arguments> provideNodeRunObjects() {
        NodeRunModel nodeRunModel = TestUtil.nodeRun();
        nodeRunModel.setType(NodeRun.NodeTypeCase.TASK);
        return Stream.of(Arguments.of(nodeRunModel, "4/0000000/1/0"));
    }

    private static Stream<Arguments> provideGetableObjectsAndIds() {
        WfRunModel wfRunModel = TestUtil.wfRun("0000000");
        TaskRunModel taskRun = TestUtil.taskRun();
        VariableModel variable = TestUtil.variable("0000000");
        variable.getId().setName("variableName");
        variable.setValue(new VariableValueModel("ThisShouldBeLocal"));
        variable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName("variableName");
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(variableDef1, false, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });
        ExternalEventModel externalEvent = TestUtil.externalEvent();
        ThreadSpecModel threadSpecModel1 = TestUtil.threadSpec();
        threadSpecModel1.getNodes().forEach((s, node) -> {
            node.getTaskNode().getTaskDefId().setName("input-name1");
        });
        ThreadSpecModel threadSpecModel2 = TestUtil.threadSpec();
        threadSpecModel2.getNodes().forEach((s, node) -> {
            node.getTaskNode().getTaskDefId().setName("input-name2");
        });
        return Stream.of(
                Arguments.of(wfRunModel, 6),
                Arguments.of(taskRun, 2),
                Arguments.of(variable, 3),
                Arguments.of(externalEvent, 4));
    }

    @Test
    void dontStoreGetableWhenNotModified() {
        String varName = "my-str";
        String wfRunId = "my-wf-run-id";
        String valueBefore = "valueBefore";
        String anotherValue = "anotherValue";
        VariableModel actualVariable = TestUtil.variable(wfRunId);
        actualVariable.getId().setName(varName);
        actualVariable.setValue(new VariableValueModel(valueBefore));
        actualVariable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName(varName);
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        VariableModel anotherVariable = TestUtil.variable(wfRunId);
        anotherVariable.getId().setName(varName);
        anotherVariable.setValue(new VariableValueModel(anotherValue));
        anotherVariable.setWfSpec(actualVariable.getWfSpec());

        // As setup, we store the actual variable.
        getableManager.put(actualVariable);
        getableManager.commit();

        // Sanity check that the variable has the "valueBefore"
        String key = new StoredGetable(actualVariable).getStoreKey();
        StoredGetable storedVariable = localStoreWrapper.get(key, StoredGetable.class);
        assertThat(storedVariable).isNotNull();
        assertThat(((VariableModel) storedVariable.getStoredObject()).getValue().getStrVal())
                .isEqualTo(valueBefore);

        // Now we "process another command" that reads the variable but doesn't modify it
        getableManager.get(actualVariable.getObjectId());

        // bypass the security of the test by corrupting it
        StoredGetable fakeOne = new StoredGetable(anotherVariable);
        localStoreWrapper.put(fakeOne);

        // Commit the getable manager. If everything goes well, it won't have called put() on the actualVariable,
        // so we should still see the "anotherValue" which we put two lines above.
        getableManager.commit();
        StoredGetable storedVariableAfterCommit = localStoreWrapper.get(key, StoredGetable.class);
        assertThat(storedVariableAfterCommit).isNotNull();
        assertThat(((VariableModel) storedVariableAfterCommit.getStoredObject())
                        .getValue()
                        .getStrVal())
                .isEqualTo(anotherValue);
    }

    @Test
    void doStoreGetableWhenModified() {
        String varName = "my-str";
        String wfRunId = "my-wf-run-id";
        String valueBefore = "valueBefore";
        String valueAfterModify = "valueAfterModify";
        String anotherValue = "anotherValue";
        VariableModel actualVariable = TestUtil.variable(wfRunId);
        actualVariable.getId().setName(varName);
        actualVariable.setValue(new VariableValueModel(valueBefore));
        actualVariable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            VariableDefModel variableDef1 = new VariableDefModel();
            variableDef1.setName(varName);
            variableDef1.setTypeDef(new TypeDefinitionModel(VariableType.STR));
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(variableDef1, true, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });

        VariableModel anotherVariable = TestUtil.variable(wfRunId);
        anotherVariable.getId().setName(varName);
        anotherVariable.setValue(new VariableValueModel(anotherValue));
        anotherVariable.setWfSpec(actualVariable.getWfSpec());

        // As setup, we store the actual variable.
        getableManager.put(actualVariable);
        getableManager.commit();

        // Sanity check that the variable has the "valueBefore"
        String key = new StoredGetable(actualVariable).getStoreKey();
        StoredGetable storedVariable = localStoreWrapper.get(key, StoredGetable.class);
        assertThat(storedVariable).isNotNull();
        assertThat(((VariableModel) storedVariable.getStoredObject()).getValue().getStrVal())
                .isEqualTo(valueBefore);

        // Now we "process another command" that reads the variable and do modify the value
        VariableModel variableDuringProcess = getableManager.get(actualVariable.getObjectId());
        variableDuringProcess.setWfSpec(actualVariable.getWfSpec());
        variableDuringProcess.setValue(new VariableValueModel(valueAfterModify));

        // bypass the security of the test by corrupting it
        StoredGetable fakeOne = new StoredGetable(anotherVariable);
        localStoreWrapper.put(fakeOne);

        // Commit the getable manager. If everything goes well, it will notice that we modified the variable
        // and will save it.
        getableManager.commit();
        StoredGetable storedVariableAfterCommit = localStoreWrapper.get(key, StoredGetable.class);
        assertThat(storedVariableAfterCommit).isNotNull();
        assertThat(((VariableModel) storedVariableAfterCommit.getStoredObject())
                        .getValue()
                        .getStrVal())
                .isEqualTo(valueAfterModify);
    }

    @Test
    void storeNodeRunWithExternalEventDefNameTag() {
        String eventName = "test-name";
        NodeRunModel nodeRunModel = TestUtil.nodeRun();
        nodeRunModel.setType(NodeRun.NodeTypeCase.EXTERNAL_EVENT);
        ExternalEventNodeRunModel extEvtRun =
                new ExternalEventNodeRunModel(new ExternalEventDefIdModel(eventName), executionContext);
        nodeRunModel.setExternalEventRun(extEvtRun);

        getableManager.put(nodeRunModel);
        getableManager.commit();

        List<String> storedKeys = getAllKeys(store);
        assertThat(storedKeys).hasSize(2);
        assertThat(storedKeys).anyMatch(key -> key.contains("myTenant/0/4/0000000/1/0"));
        assertThat(storedKeys)
                .anyMatch(key -> key.contains("myTenant/5/4/__status_RUNNING__extEvtDefName_" + eventName));
    }

    @Test
    void storeWfRunWithParentWfRunIdTag() {
        String parentWfRunId = "parent-wf-run-id";
        WfRunModel wfRunModel = TestUtil.wfRun("child-wf-run");
        wfRunModel.getId().setParentWfRunId(new WfRunIdModel(parentWfRunId));

        getableManager.put(wfRunModel);
        getableManager.commit();

        List<String> storedKeys = getAllKeys(store);
        assertThat(storedKeys).hasSize(11);
        assertThat(storedKeys).anyMatch(key -> key.contains("myTenant/0/3/parent-wf-run-id_child-wf-run"));
        assertThat(storedKeys)
                .anyMatch(key ->
                        key.contains("myTenant/5/3/__wfSpecName_test-spec-name__parentWfRunId_" + parentWfRunId));
    }

    @Test
    void storeWfRunWithParentWfRunIdAndStatusTag() {
        String parentWfRunId = "parent-wf-run-id-2";
        WfRunModel wfRunModel = TestUtil.wfRun("child-wf-run-2");
        wfRunModel.getId().setParentWfRunId(new WfRunIdModel(parentWfRunId));
        wfRunModel.status = LHStatus.COMPLETED;

        getableManager.put(wfRunModel);
        getableManager.commit();

        List<String> storedKeys = getAllKeys(store);
        assertThat(storedKeys).hasSize(11);
        assertThat(storedKeys).anyMatch(key -> key.contains("myTenant/0/3/parent-wf-run-id-2_child-wf-run-2"));
        assertThat(storedKeys)
                .anyMatch(key ->
                        key.contains("myTenant/5/3/__wfSpecName_test-spec-name__parentWfRunId_" + parentWfRunId));
        assertThat(storedKeys)
                .anyMatch(key -> key.contains("myTenant/5/3/__wfSpecName_test-spec-name__status_COMPLETED"));
        assertThat(storedKeys)
                .anyMatch(key -> key.contains(
                        "myTenant/5/3/__wfSpecName_test-spec-name__status_COMPLETED__parentWfRunId_" + parentWfRunId));
    }
}

package io.littlehorse.common.model.corecommand.subcommand;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.TestCoreProcessorContext;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.util.HeadersUtil;
import java.util.Date;
import java.util.function.Function;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SaveUserTaskRunProgressRequestModelTest {

    private static final String RESULT_STRUCT_DEF_NAME = "item-request-form";
    private static final String NESTED_STRUCT_DEF_NAME = "item-request-details";
    private static final int STRUCT_DEF_VERSION = 0;

    private static final String STR_FIELD = "str-field";
    private static final TypeDefinition STR_FIELD_TYPE =
            TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR).build();
    private static final String STRUCT_FIELD = "struct-field";
    private static final TypeDefinition STRUCT_FIELD_TYPE = TypeDefinition.newBuilder()
            .setStructDefId(new StructDefIdModel(NESTED_STRUCT_DEF_NAME, STRUCT_DEF_VERSION).toProto())
            .build();
    private static final String NESTED_BOOL_FIELD = "bool-field";
    private static final TypeDefinition NESTED_BOOL_FIELD_TYPE =
            TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL).build();

    @Test
    void shouldSavePartialStructProgress() {
        VariableValue requestedItem =
                VariableValue.newBuilder().setStr("a laptop").build();
        TestData testData = arrangeScenarioWithOutput(inlineStruct(STR_FIELD, requestedItem));
        TestCoreProcessorContext freshContext = testData.context();
        UserTaskRunModel originalTask = freshContext.getableManager().get(testData.taskId());
        Assertions.assertThat(originalTask.getResults()).isEmpty();
        Assertions.assertThat(originalTask.getOutput()).isNull();
        freshContext.endExecution();

        SaveUserTaskRunProgressRequestModel request =
                freshContext.currentCommand().getSaveUserTaskRunProgress();

        request.process(freshContext, freshContext.getLhConfig());
        freshContext.endExecution();

        UserTaskRunModel storedTask = freshContext.getableManager().get(testData.taskId());
        Assertions.assertThat(storedTask.getId()).isEqualTo(testData.taskId());
        Assertions.assertThat(storedTask.getResults()).isEmpty();
        Assertions.assertThat(storedTask.getOutput().toProto().build())
                .isEqualTo(testData.progress().getOutput());
        Assertions.assertThat(storedTask.getEvents()).singleElement().satisfies(event -> {
            Assertions.assertThat(event.getType()).isEqualTo(EventCase.SAVED);
            Assertions.assertThat(event.getTime()).isEqualTo(testData.savedAt());
            Assertions.assertThat(event.getSaved().getUserId()).isEqualTo("anakin");
            Assertions.assertThat(event.getSaved().toProto().getResultsMap())
                    .containsOnly(java.util.Map.entry(STR_FIELD, requestedItem));
        });
    }

    @Test
    void shouldRejectUnknownStructField() {
        TestData testData = arrangeScenarioWithOutput(inlineStruct(
                "unknownField", VariableValue.newBuilder().setStr("value").build()));

        assertInvalidField(testData, "Field 'unknownField' is not defined");
    }

    @Test
    void shouldRejectStructFieldWithWrongType() {
        TestData testData = arrangeScenarioWithOutput(inlineStruct(
                STRUCT_FIELD, VariableValue.newBuilder().setStr("not-a-struct").build()));

        assertInvalidField(testData, "Field '%s' is invalid".formatted(STRUCT_FIELD));
    }

    @Test
    void shouldRejectInvalidNestedStructField() {
        VariableValue invalidDetails = structOutput(inlineStruct(
                NESTED_BOOL_FIELD,
                VariableValue.newBuilder().setStr("not-a-boolean").build()));
        TestData testData = arrangeScenarioWithOutput(inlineStruct(STRUCT_FIELD, invalidDetails));

        assertInvalidField(testData, "Field '%s' is invalid".formatted(STRUCT_FIELD));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldRejectLegacyResultsForStructBackedUserTaskDef() {
        VariableValue partialResult =
                VariableValue.newBuilder().setStr("partial value").build();
        TestData testData = arrangeStructScenario(taskId -> SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                .setUserId("anakin")
                .putResults(STR_FIELD, partialResult)
                .build());

        assertInvalidField(testData, "Use output instead of results for a struct-backed UserTaskDef");
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldSaveProgressForLegacyUserTaskDef() {
        VariableValue result = VariableValue.newBuilder().setStr("legacy value").build();
        TestData testData = arrangeLegacyScenarioWithResult(STR_FIELD, result);

        testData.request().process(testData.context(), testData.context().getLhConfig());
        testData.context().endExecution();

        UserTaskRunModel storedTask = testData.context().getableManager().get(testData.taskId());
        Assertions.assertThat(storedTask.getOutput()).isNull();
        Assertions.assertThat(storedTask.getResults()).containsOnlyKeys(STR_FIELD);
        Assertions.assertThat(storedTask.getResults().get(STR_FIELD).toProto().build())
                .isEqualTo(result);
    }

    @Test
    void shouldSaveStructOutputForLegacyUserTaskDef() {
        TestData testData = arrangeLegacyScenario(STR_FIELD, taskId -> SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                .setUserId("anakin")
                .setOutput(structOutput(inlineStruct(
                        STR_FIELD,
                        VariableValue.newBuilder().setStr("partial value").build())))
                .build());

        testData.request().process(testData.context(), testData.context().getLhConfig());
        testData.context().endExecution();
        UserTaskRunModel stored = testData.context().getableManager().get(testData.taskId());
        Assertions.assertThat(stored.getOutput()).isNull();
        Assertions.assertThat(stored.getStatus()).isEqualTo(UserTaskRunStatus.ASSIGNED);
        Assertions.assertThat(stored.getResults()).containsOnlyKeys(STR_FIELD);
        Assertions.assertThat(stored.getResults().get(STR_FIELD).toProto().getStr())
                .isEqualTo("partial value");
        Assertions.assertThat(stored.getEvents()).singleElement().satisfies(event -> {
            Assertions.assertThat(event.getType()).isEqualTo(EventCase.SAVED);
            Assertions.assertThat(event.getSaved().toProto().getResultsMap())
                    .isEqualTo(stored.toProto().getResultsMap());
        });
    }

    @Test
    void shouldAllowMissingRequiredLegacyFieldWhenSavingStruct() {
        TestData data = arrangeLegacyScenarioWithOutput(structOutput(InlineStruct.getDefaultInstance()));
        data.request().process(data.context(), data.context().getLhConfig());
        data.context().endExecution();
        UserTaskRunModel stored = data.context().getableManager().get(data.taskId());
        Assertions.assertThat(stored.getStatus()).isEqualTo(UserTaskRunStatus.ASSIGNED);
        Assertions.assertThat(stored.getResults()).isEmpty();
        Assertions.assertThat(stored.getOutput()).isNull();
        Assertions.assertThat(stored.getEvents()).hasSize(1);
    }

    @Test
    void shouldRejectIncompatibleStructFieldForLegacyProgress() {
        TestData data = arrangeLegacyScenarioWithOutput(structOutput(
                inlineStruct(STR_FIELD, VariableValue.newBuilder().setBool(true).build())));
        assertInvalidField(data, "is not defined in UserTask schema or has different type");
    }

    @Test
    void shouldRejectUnknownStructFieldForLegacyProgress() {
        TestData data = arrangeLegacyScenarioWithOutput(structOutput(inlineStruct(
                "unknownField", VariableValue.newBuilder().setStr("value").build())));
        assertInvalidField(data, "is not defined in UserTask schema or has different type");
    }

    @Test
    void shouldRejectNonStructOutputForLegacyProgress() {
        TestData data = arrangeLegacyScenarioWithOutput(
                VariableValue.newBuilder().setStr("value").build());
        assertInvalidField(data, "must contain a Struct value");
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldRejectBothRepresentationsForLegacyProgress() {
        TestData data = arrangeLegacyScenario(STR_FIELD, taskId -> SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setUserId("anakin")
                .putResults(
                        STR_FIELD, VariableValue.newBuilder().setStr("value").build())
                .setOutput(structOutput(InlineStruct.getDefaultInstance()))
                .build());
        assertInvalidField(data, "Cannot supply both results and output");
    }

    private TestData arrangeLegacyScenarioWithOutput(VariableValue output) {
        return arrangeLegacyScenario(STR_FIELD, taskId -> SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setUserId("anakin")
                .setOutput(output)
                .build());
    }

    private void assertInvalidField(TestData testData, String expectedMessage) {
        Throwable thrown = Assertions.catchThrowable(() -> testData.request()
                .process(testData.context(), testData.context().getLhConfig()));

        Assertions.assertThat(thrown).isInstanceOf(LHApiException.class).hasMessageContaining(expectedMessage);
        Assertions.assertThat(((LHApiException) thrown).getStatus().getCode())
                .isEqualTo(io.grpc.Status.Code.INVALID_ARGUMENT);

        UserTaskRunModel storedTask = testData.context().getableManager().get(testData.taskId());
        Assertions.assertThat(storedTask.getOutput()).isNull();
        Assertions.assertThat(storedTask.getResults()).isEmpty();
        Assertions.assertThat(storedTask.getEvents()).isEmpty();
    }

    private TestData arrangeScenarioWithOutput(InlineStruct output) {
        return arrangeStructScenario(taskId -> createProgressRequest(taskId, output));
    }

    private TestData arrangeStructScenario(
            Function<UserTaskRunIdModel, SaveUserTaskRunProgressRequest> requestFactory) {
        Date savedAt = new Date(1_000L);
        NodeRunIdModel nodeRunId = new NodeRunIdModel("wf-run", 0, 1);
        UserTaskRunIdModel taskId = new UserTaskRunIdModel(nodeRunId);
        StructDefIdModel structDefId = resultStructDefId();
        SaveUserTaskRunProgressRequest progress = requestFactory.apply(taskId);
        MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
        // The shared harness registers Kafka Streams in-memory stores with this
        // MockProcessorContext and uses real metadata and Getable managers.
        TestCoreProcessorContext freshContext = freshContext(
                Command.newBuilder()
                        .setTime(LHUtil.fromDate(savedAt))
                        .setSaveUserTaskRunProgress(progress)
                        .build(),
                mockProcessorContext);

        UserTaskDefModel taskDef = createUserTaskDef(structDefId);
        freshContext.metadataManager().put(taskDef);
        freshContext.metadataManager().put(createStructDef(structDefId, freshContext));
        freshContext.metadataManager().put(createNestedStructDef(freshContext));
        freshContext.getableManager().put(createUserTaskRun(nodeRunId, taskDef.getObjectId(), freshContext));
        freshContext.endExecution();

        return new TestData(
                freshContext, taskId, progress, freshContext.currentCommand().getSaveUserTaskRunProgress(), savedAt);
    }

    @SuppressWarnings("deprecation")
    private TestData arrangeLegacyScenarioWithResult(String fieldName, VariableValue result) {
        return arrangeLegacyScenario(fieldName, taskId -> SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                .setUserId("anakin")
                .putResults(fieldName, result)
                .build());
    }

    private TestData arrangeLegacyScenario(
            String fieldName, Function<UserTaskRunIdModel, SaveUserTaskRunProgressRequest> requestFactory) {
        Date savedAt = new Date(2_000L);
        NodeRunIdModel nodeRunId = new NodeRunIdModel("legacy-wf-run", 0, 1);
        UserTaskRunIdModel taskId = new UserTaskRunIdModel(nodeRunId);
        SaveUserTaskRunProgressRequest progress = requestFactory.apply(taskId);
        MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
        TestCoreProcessorContext freshContext = freshContext(
                Command.newBuilder()
                        .setTime(LHUtil.fromDate(savedAt))
                        .setSaveUserTaskRunProgress(progress)
                        .build(),
                mockProcessorContext);

        UserTaskDefModel taskDef = createLegacyUserTaskDef(fieldName);
        freshContext.metadataManager().put(taskDef);
        freshContext.getableManager().put(createUserTaskRun(nodeRunId, taskDef.getObjectId(), freshContext));
        freshContext.endExecution();

        return new TestData(
                freshContext, taskId, progress, freshContext.currentCommand().getSaveUserTaskRunProgress(), savedAt);
    }

    private SaveUserTaskRunProgressRequest createProgressRequest(UserTaskRunIdModel taskId, InlineStruct output) {
        return SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                .setUserId("anakin")
                .setOutput(structOutput(output))
                .build();
    }

    private VariableValue structOutput(InlineStruct output) {
        return VariableValue.newBuilder()
                .setStruct(Struct.newBuilder().setStruct(output))
                .build();
    }

    private InlineStruct inlineStruct(String fieldName, VariableValue value) {
        return InlineStruct.newBuilder()
                .putFields(fieldName, StructField.newBuilder().setValue(value).build())
                .build();
    }

    private UserTaskDefModel createUserTaskDef(StructDefIdModel structDefId) {
        UserTaskDefModel taskDef = new UserTaskDefModel();
        taskDef.name = "it-request";
        taskDef.version = 0;
        taskDef.createdAt = new Date(0);
        taskDef.setResultStructDefId(structDefId);
        return taskDef;
    }

    private UserTaskDefModel createLegacyUserTaskDef(String fieldName) {
        UserTaskDefModel taskDef = new UserTaskDefModel();
        taskDef.name = "legacy-user-task";
        taskDef.version = 0;
        taskDef.createdAt = new Date(0);

        UserTaskFieldModel field = new UserTaskFieldModel();
        field.setName(fieldName);
        field.setDisplayName(fieldName);
        field.setType(VariableType.STR);
        field.setRequired(true);
        taskDef.getFields().add(field);
        return taskDef;
    }

    private StructDefModel createStructDef(StructDefIdModel structDefId, TestCoreProcessorContext context) {
        return StructDefModel.fromProto(
                StructDef.newBuilder()
                        .setId(structDefId.toProto())
                        .setCreatedAt(LHUtil.fromDate(new Date(0)))
                        .setStructDef(InlineStructDef.newBuilder()
                                .putFields(
                                        STR_FIELD,
                                        StructFieldDef.newBuilder()
                                                .setFieldType(STR_FIELD_TYPE)
                                                .build())
                                .putFields(
                                        STRUCT_FIELD,
                                        StructFieldDef.newBuilder()
                                                .setFieldType(STRUCT_FIELD_TYPE)
                                                .build()))
                        .build(),
                context);
    }

    private StructDefModel createNestedStructDef(TestCoreProcessorContext context) {
        return StructDefModel.fromProto(
                StructDef.newBuilder()
                        .setId(nestedStructDefId().toProto())
                        .setCreatedAt(LHUtil.fromDate(new Date(0)))
                        .setStructDef(InlineStructDef.newBuilder()
                                .putFields(
                                        NESTED_BOOL_FIELD,
                                        StructFieldDef.newBuilder()
                                                .setFieldType(NESTED_BOOL_FIELD_TYPE)
                                                .build()))
                        .build(),
                context);
    }

    private StructDefIdModel resultStructDefId() {
        return new StructDefIdModel(RESULT_STRUCT_DEF_NAME, STRUCT_DEF_VERSION);
    }

    private StructDefIdModel nestedStructDefId() {
        return new StructDefIdModel(NESTED_STRUCT_DEF_NAME, STRUCT_DEF_VERSION);
    }

    private UserTaskRunModel createUserTaskRun(
            NodeRunIdModel nodeRunId, UserTaskDefIdModel taskDefId, TestCoreProcessorContext context) {
        UserTaskRunModel task = new UserTaskRunModel();
        task.setId(new UserTaskRunIdModel(nodeRunId));
        task.setNodeRunId(nodeRunId);
        task.setScheduledTime(new Date(0));
        task.setExecutionContext(context);
        task.setUserTaskDefId(taskDefId);
        task.setStatus(UserTaskRunStatus.ASSIGNED);
        task.setUserId("anakin");

        return task;
    }

    private TestCoreProcessorContext freshContext(
            Command command, MockProcessorContext<String, CommandProcessorOutput> processorContext) {
        return TestCoreProcessorContext.create(
                command,
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL),
                processorContext);
    }

    private record TestData(
            TestCoreProcessorContext context,
            UserTaskRunIdModel taskId,
            SaveUserTaskRunProgressRequest progress,
            SaveUserTaskRunProgressRequestModel request,
            Date savedAt) {}
}

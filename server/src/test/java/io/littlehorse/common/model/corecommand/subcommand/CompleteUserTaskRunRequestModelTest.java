package io.littlehorse.common.model.corecommand.subcommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.protobuf.Empty;
import io.grpc.Status.Code;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LHStatus;
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
import java.util.Map;
import java.util.function.Consumer;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class CompleteUserTaskRunRequestModelTest {

    private static final String RESULT_STRUCT_DEF_NAME = "item-request-form";
    private static final String NESTED_STRUCT_DEF_NAME = "item-request-details";
    private static final int STRUCT_DEF_VERSION = 0;
    private static final String STR_FIELD = "strField";
    private static final TypeDefinition STR_FIELD_TYPE =
            TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR).build();
    private static final String STRUCT_FIELD = "structField";
    private static final TypeDefinition STRUCT_FIELD_TYPE = TypeDefinition.newBuilder()
            .setStructDefId(new StructDefIdModel(NESTED_STRUCT_DEF_NAME, STRUCT_DEF_VERSION).toProto())
            .build();
    private static final String NESTED_BOOL_FIELD = "boolField";
    private static final TypeDefinition NESTED_BOOL_FIELD_TYPE =
            TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL).build();

    @Test
    void shouldCompleteWithStructOutput() {
        VariableValue submitted = structOutput(validFields());
        TestData data = arrangeStructScenario(request -> request.setOutput(submitted));

        UserTaskRunModel stored = completeAndReload(data);

        assertThat(stored.getResults()).isEmpty();
        Struct output = stored.getOutput().toProto().getStruct();
        assertThat(output.getStructDefId())
                .isEqualTo(resultStructDefId().toProto().build());
        assertThat(output.getStruct().getFieldsOrThrow(STR_FIELD).getValue())
                .isEqualTo(submitted
                        .getStruct()
                        .getStruct()
                        .getFieldsOrThrow(STR_FIELD)
                        .getValue());
        Struct nested =
                output.getStruct().getFieldsOrThrow(STRUCT_FIELD).getValue().getStruct();
        assertThat(nested.getStructDefId())
                .isEqualTo(nestedStructDefId().toProto().build());
        assertThat(nested.getStruct()
                        .getFieldsOrThrow(NESTED_BOOL_FIELD)
                        .getValue()
                        .getBool())
                .isTrue();
        assertThat(submitted.getStruct().hasStructDefId()).isFalse();
    }

    @Test
    void shouldAcceptExtraStructFields() {
        VariableValue extra =
                VariableValue.newBuilder().setStr("new client field").build();
        TestData data = arrangeStructScenario(request -> request.setOutput(structOutput(
                validFields().toBuilder().putFields("extraField", field(extra)).build())));

        UserTaskRunModel stored = completeAndReload(data);

        assertThat(stored.getOutput()
                        .toProto()
                        .getStruct()
                        .getStruct()
                        .getFieldsOrThrow("extraField")
                        .getValue())
                .isEqualTo(extra);
    }

    @Test
    void shouldRejectMissingOutput() {
        TestData data = arrangeStructScenario(request -> {});

        assertInvalidRequest(data, "Missing output for struct-backed UserTaskDef");
    }

    @Test
    void shouldRejectNonStructOutput() {
        TestData data = arrangeStructScenario(
                request -> request.setOutput(VariableValue.newBuilder().setStr("not-a-struct")));

        assertInvalidRequest(data, "must contain a Struct value");
    }

    @Test
    void shouldRejectMissingRequiredStructField() {
        TestData data = arrangeStructScenario(request -> request.setOutput(
                structOutput(validFields().toBuilder().removeFields(STR_FIELD).build())));

        assertInvalidRequest(data, "Missing required field " + STR_FIELD);
    }

    @Test
    void shouldRejectStructFieldWithWrongType() {
        TestData data = arrangeStructScenario(request -> request.setOutput(structOutput(validFields().toBuilder()
                .putFields(
                        STRUCT_FIELD,
                        field(VariableValue.newBuilder().setStr("not-a-struct").build()))
                .build())));

        assertInvalidRequest(data, "Field '" + STRUCT_FIELD + "' is invalid");
    }

    @Test
    void shouldRejectInvalidNestedStructField() {
        VariableValue nested = structOutput(InlineStruct.newBuilder()
                .putFields(
                        NESTED_BOOL_FIELD,
                        field(VariableValue.newBuilder().setStr("not-a-boolean").build()))
                .build());
        TestData data = arrangeStructScenario(request -> request.setOutput(structOutput(
                validFields().toBuilder().putFields(STRUCT_FIELD, field(nested)).build())));

        assertInvalidRequest(data, "Field '" + NESTED_BOOL_FIELD + "' is invalid");
    }

    @Test
    void shouldRejectMissingRequiredNestedField() {
        TestData data = arrangeStructScenario(request -> request.setOutput(structOutput(validFields().toBuilder()
                .putFields(STRUCT_FIELD, field(structOutput(InlineStruct.getDefaultInstance())))
                .build())));

        assertInvalidRequest(data, "Missing required field " + NESTED_BOOL_FIELD);
    }

    @Test
    void shouldRejectLegacyResultsForStructBackedUserTaskDef() {
        TestData data = arrangeStructScenario(request -> request.putResults(
                STR_FIELD, VariableValue.newBuilder().setStr("legacy value").build()));

        assertInvalidRequest(data, "Use output instead of results for a struct-backed UserTaskDef");
    }

    @Test
    void shouldRejectBothResultsAndOutput() {
        TestData data = arrangeStructScenario(request -> request.putResults(
                        STR_FIELD,
                        VariableValue.newBuilder().setStr("legacy value").build())
                .setOutput(structOutput(validFields())));

        assertInvalidRequest(data, "Use output instead of results for a struct-backed UserTaskDef");
    }

    @Test
    void shouldCompleteLegacyUserTaskDef() {
        VariableValue result = VariableValue.newBuilder().setStr("legacy value").build();
        TestData data = arrangeLegacyScenario(request -> request.putResults(STR_FIELD, result));

        UserTaskRunModel stored = completeAndReload(data);

        assertThat(stored.getOutput()).isNull();
        assertThat(stored.toProto().getResultsMap()).containsOnly(Map.entry(STR_FIELD, result));
    }

    @Test
    void shouldCompleteLegacyUserTaskDefWithStructOutput() {
        VariableValue value = VariableValue.newBuilder().setStr("legacy value").build();
        TestData data = arrangeLegacyScenario(request -> request.setOutput(structOutput(
                InlineStruct.newBuilder().putFields(STR_FIELD, field(value)).build())));

        UserTaskRunModel stored = completeAndReload(data);
        assertThat(stored.getOutput()).isNull();
        assertThat(stored.toProto().getResultsMap()).containsOnly(Map.entry(STR_FIELD, value));
    }

    @Test
    void shouldRejectMissingRequiredLegacyFieldInStructOutput() {
        TestData data =
                arrangeLegacyScenario(request -> request.setOutput(structOutput(InlineStruct.getDefaultInstance())));
        assertInvalidRequest(data, "[" + STR_FIELD + "] are mandatory fields");
    }

    @Test
    void shouldRejectIncompatibleStructFieldForLegacyCompletion() {
        TestData data = arrangeLegacyScenario(request -> request.setOutput(structOutput(InlineStruct.newBuilder()
                .putFields(
                        STR_FIELD,
                        field(VariableValue.newBuilder().setBool(true).build()))
                .build())));
        assertInvalidRequest(data, "is not defined in UserTask schema or has different type");
    }

    @Test
    void shouldRejectUnknownStructFieldForLegacyCompletion() {
        TestData data = arrangeLegacyScenario(request -> request.setOutput(structOutput(InlineStruct.newBuilder()
                .putFields(
                        "unknownField",
                        field(VariableValue.newBuilder().setStr("value").build()))
                .build())));
        assertInvalidRequest(data, "is not defined in UserTask schema or has different type");
    }

    @Test
    void shouldRejectNonStructOutputForLegacyCompletion() {
        TestData data = arrangeLegacyScenario(
                request -> request.setOutput(VariableValue.newBuilder().setStr("value")));
        assertInvalidRequest(data, "must contain a Struct value");
    }

    @Test
    void shouldRejectBothRepresentationsForLegacyCompletion() {
        TestData data = arrangeLegacyScenario(request -> request.putResults(
                        STR_FIELD, VariableValue.newBuilder().setStr("value").build())
                .setOutput(structOutput(InlineStruct.getDefaultInstance())));
        assertInvalidRequest(data, "Cannot supply both results and output");
    }

    @Test
    void shouldRejectMissingRequiredLegacyField() {
        TestData data = arrangeLegacyScenario(request -> {});

        assertInvalidRequest(data, "[" + STR_FIELD + "] are mandatory fields");
    }

    @Test
    void shouldRejectLegacyFieldWithWrongType() {
        TestData data = arrangeLegacyScenario(request -> request.putResults(
                STR_FIELD, VariableValue.newBuilder().setBool(true).build()));

        assertInvalidRequest(data, "is not defined in UserTask schema or has different type");
    }

    @Test
    void shouldRejectUnknownLegacyField() {
        TestData data = arrangeLegacyScenario(request -> request.putResults(
                "unknownField", VariableValue.newBuilder().setStr("value").build()));

        assertInvalidRequest(data, "is not defined in UserTask schema or has different type");
    }

    private UserTaskRunModel completeAndReload(TestData data) {
        assertThat(data.request().process(data.context(), data.context().getLhConfig()))
                .isEqualTo(Empty.getDefaultInstance());
        data.context().endExecution();
        UserTaskRunModel stored = data.context().getableManager().get(data.taskId());
        assertThat(stored.getStatus()).isEqualTo(UserTaskRunStatus.DONE);
        assertThat(stored.getUserId()).isEqualTo("anakin");
        assertThat(stored.getEvents()).singleElement().satisfies(event -> {
            assertThat(event.getType()).isEqualTo(EventCase.COMPLETED);
            assertThat(event.getTime()).isEqualTo(data.completedAt());
        });
        return stored;
    }

    private void assertInvalidRequest(TestData data, String message) {
        Throwable thrown = catchThrowable(
                () -> data.request().process(data.context(), data.context().getLhConfig()));
        assertThat(thrown).isInstanceOf(LHApiException.class).hasMessageContaining(message);
        assertThat(((LHApiException) thrown).getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
        UserTaskRunModel task = data.context().getableManager().get(data.taskId());
        assertThat(task.getStatus()).isEqualTo(UserTaskRunStatus.ASSIGNED);
        assertThat(task.getOutput()).isNull();
        assertThat(task.getResults()).isEmpty();
        assertThat(task.getEvents()).isEmpty();
    }

    private TestData arrangeStructScenario(Consumer<CompleteUserTaskRunRequest.Builder> configureRequest) {
        return arrangeScenario(createUserTaskDef(true), configureRequest);
    }

    private TestData arrangeLegacyScenario(Consumer<CompleteUserTaskRunRequest.Builder> configureRequest) {
        return arrangeScenario(createUserTaskDef(false), configureRequest);
    }

    private TestData arrangeScenario(
            UserTaskDefModel taskDef, Consumer<CompleteUserTaskRunRequest.Builder> configureRequest) {
        Date completedAt = new Date(1_000L);
        NodeRunIdModel nodeId = new NodeRunIdModel("wf-run", 0, 1);
        UserTaskRunIdModel taskId = new UserTaskRunIdModel(nodeId);
        CompleteUserTaskRunRequest.Builder completion = CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(taskId.toProto())
                .setUserId("anakin");
        configureRequest.accept(completion);
        TestCoreProcessorContext freshContext = TestCoreProcessorContext.create(
                Command.newBuilder()
                        .setTime(LHUtil.fromDate(completedAt))
                        .setCompleteUserTaskRun(completion)
                        .build(),
                HeadersUtil.metadataHeadersFor(LHConstants.DEFAULT_TENANT, LHConstants.ANONYMOUS_PRINCIPAL),
                new MockProcessorContext<String, CommandProcessorOutput>());
        freshContext.metadataManager().put(taskDef);
        if (taskDef.getResultStructDefId() != null) {
            freshContext
                    .metadataManager()
                    .put(createStructDef(
                            nestedStructDefId(),
                            InlineStructDef.newBuilder()
                                    .putFields(NESTED_BOOL_FIELD, fieldDef(NESTED_BOOL_FIELD_TYPE))
                                    .build(),
                            freshContext));
            freshContext
                    .metadataManager()
                    .put(createStructDef(
                            resultStructDefId(),
                            InlineStructDef.newBuilder()
                                    .putFields(STR_FIELD, fieldDef(STR_FIELD_TYPE))
                                    .putFields(STRUCT_FIELD, fieldDef(STRUCT_FIELD_TYPE))
                                    .build(),
                            freshContext));
        }
        freshContext.getableManager().put(createWfRun(taskId, freshContext));
        freshContext.getableManager().put(createUserTaskRun(nodeId, taskDef, freshContext));
        freshContext.endExecution();
        CompleteUserTaskRunRequestModel request = freshContext.currentCommand().getCompleteUserTaskRun();
        request.setTime(completedAt);
        return new TestData(freshContext, taskId, request, completedAt);
    }

    private WfRunModel createWfRun(UserTaskRunIdModel taskId, TestCoreProcessorContext context) {
        // A minimal real WfRun lets the subcommand call advance() without mocking it.
        WfRunModel wfRun = new WfRunModel(context);
        wfRun.setId(taskId.getWfRunId());
        wfRun.setWfSpecId(new WfSpecIdModel("test-workflow", 0, 0));
        wfRun.setStatus(LHStatus.RUNNING);
        wfRun.setStartTime(new Date(1));
        return wfRun;
    }

    private UserTaskRunModel createUserTaskRun(
            NodeRunIdModel nodeId, UserTaskDefModel taskDef, TestCoreProcessorContext context) {
        UserTaskRunModel task = new UserTaskRunModel();
        task.setId(new UserTaskRunIdModel(nodeId));
        task.setNodeRunId(nodeId);
        task.setScheduledTime(new Date(1));
        task.setExecutionContext(context);
        task.setUserTaskDefId(taskDef.getObjectId());
        task.setStatus(UserTaskRunStatus.ASSIGNED);
        task.setUserId("anakin");
        return task;
    }

    private UserTaskDefModel createUserTaskDef(boolean structBacked) {
        UserTaskDefModel taskDef = new UserTaskDefModel();
        taskDef.name = "it-request";
        taskDef.version = 0;
        taskDef.createdAt = new Date(1);
        if (structBacked) {
            taskDef.setResultStructDefId(resultStructDefId());
        } else {
            UserTaskFieldModel field = new UserTaskFieldModel();
            field.setName(STR_FIELD);
            field.setDisplayName(STR_FIELD);
            field.setType(STR_FIELD_TYPE.getPrimitiveType());
            field.setRequired(true);
            taskDef.getFields().add(field);
        }
        return taskDef;
    }

    private StructDefModel createStructDef(
            StructDefIdModel id, InlineStructDef fields, TestCoreProcessorContext context) {
        return StructDefModel.fromProto(
                StructDef.newBuilder()
                        .setId(id.toProto())
                        .setCreatedAt(LHUtil.fromDate(new Date(1)))
                        .setStructDef(fields)
                        .build(),
                context);
    }

    private StructFieldDef fieldDef(TypeDefinition type) {
        return StructFieldDef.newBuilder().setFieldType(type).build();
    }

    private InlineStruct validFields() {
        return InlineStruct.newBuilder()
                .putFields(
                        STR_FIELD,
                        field(VariableValue.newBuilder().setStr("a laptop").build()))
                .putFields(
                        STRUCT_FIELD,
                        field(structOutput(InlineStruct.newBuilder()
                                .putFields(
                                        NESTED_BOOL_FIELD,
                                        field(VariableValue.newBuilder()
                                                .setBool(true)
                                                .build()))
                                .build())))
                .build();
    }

    private StructField field(VariableValue value) {
        return StructField.newBuilder().setValue(value).build();
    }

    private VariableValue structOutput(InlineStruct fields) {
        return VariableValue.newBuilder()
                .setStruct(Struct.newBuilder().setStruct(fields))
                .build();
    }

    private StructDefIdModel resultStructDefId() {
        return new StructDefIdModel(RESULT_STRUCT_DEF_NAME, STRUCT_DEF_VERSION);
    }

    private StructDefIdModel nestedStructDefId() {
        return new StructDefIdModel(NESTED_STRUCT_DEF_NAME, STRUCT_DEF_VERSION);
    }

    private record TestData(
            TestCoreProcessorContext context,
            UserTaskRunIdModel taskId,
            CompleteUserTaskRunRequestModel request,
            Date completedAt) {}
}

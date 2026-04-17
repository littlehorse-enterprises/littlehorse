package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import e2e.Struct.PinStructV0;
import e2e.Struct.PinStructV1;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventRetentionPolicy;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WorkflowEventDef;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@LHTest
public class StructDefLifecycleTest {
    private LittleHorseBlockingStub client;

    @Test
    void shouldStoreStructDefDescription() {
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car-11")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build()))
                .setDescription("This StructDef describes a car")
                .build());

        waitForStructDef("car-11", null);

        StructDef structDef =
                client.getStructDef(StructDefId.newBuilder().setName("car-11").build());

        assertThat(structDef.getDescription()).isEqualTo("This StructDef describes a car");
    }

    @Test
    void shouldBumpVersionWhenPuttingCompatibleStructDefChanges() {
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car-0")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build()))
                .build());

        waitForStructDef("car-0", 0);

        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("car-0")
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .putFields(
                                "year",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setInt(1970))
                                        .build()))
                .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .build());

        waitForStructDef("car-0", 1);
    }

    @Test
    void shouldNotBumpVersionWhenPuttingIncompatibleStructDefChanges() {
        String structDefName = UUID.randomUUID().toString();

        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(structDefName)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "model",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build()))
                .build());

        waitForStructDef(structDefName, 0);

        assertThatThrownBy(() -> {
                    client.putStructDef(PutStructDefRequest.newBuilder()
                            .setName(structDefName)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.INT))
                                                    .build()))
                            .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                            .build());
                })
                .isInstanceOf(StatusRuntimeException.class);

        assertThatThrownBy(() -> {
                    waitForStructDef(structDefName, 1);
                })
                .isInstanceOf(ConditionTimeoutException.class);
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @WithStructDefs({PinStructV0.class, PinStructV1.class})
    class StructDefVersionPinningTest {
        final String WF_SPEC_NAME = "structdef-pin-test";
        final String STRUCT_DEF_NAME = "pin-struct";

        @LHWorkflow("wf-pin-test")
        public Workflow wfPinTest;

        @Test
        void shouldPinStructDefVersionWhenPuttingTaskDef() {
            final String TASK_DEF_NAME = "task-pin-test";

            PutTaskDefRequest.Builder taskReq = PutTaskDefRequest.newBuilder()
                    .setName(TASK_DEF_NAME)
                    .addInputVars(VariableDef.newBuilder()
                            .setTypeDef(TypeDefinition.newBuilder()
                                    .setStructDefId(StructDefId.newBuilder()
                                            .setName(STRUCT_DEF_NAME)
                                            .setVersion(-1))));

            client.putTaskDef(taskReq.build());

            TaskDefId id = TaskDefId.newBuilder().setName(TASK_DEF_NAME).build();

            waitForTaskDef(id);
            TaskDef taskDef = client.getTaskDef(id);

            StructDefId sdid = taskDef.getInputVars(0).getTypeDef().getStructDefId();
            assertThat(sdid.getName()).isEqualTo(STRUCT_DEF_NAME);
            assertThat(sdid.getVersion()).isEqualTo(1);
        }

        @Test
        void shouldPinStructDefWhenPuttingExternalAndWorkflowEventDefs() {
            final String EXT_EVT_DEF_NAME = "ext-pin";

            // Put ExternalEventDef referencing the struct (no version)
            PutExternalEventDefRequest eReq = PutExternalEventDefRequest.newBuilder()
                    .setName(EXT_EVT_DEF_NAME)
                    .setContentType(ReturnType.newBuilder()
                            .setReturnType(TypeDefinition.newBuilder()
                                    .setStructDefId(StructDefId.newBuilder()
                                            .setName(STRUCT_DEF_NAME)
                                            .setVersion(-1))))
                    .setRetentionPolicy(
                            ExternalEventRetentionPolicy.newBuilder().build())
                    .build();

            client.putExternalEventDef(eReq);

            ExternalEventDefId id =
                    ExternalEventDefId.newBuilder().setName(EXT_EVT_DEF_NAME).build();

            waitForExternalEventDef(id);
            ExternalEventDef gotExt = client.getExternalEventDef(id);

            StructDefId sdidExt = gotExt.getTypeInformation().getReturnType().getStructDefId();
            assertThat(sdidExt.getVersion()).isEqualTo(1);
        }

        @Test
        void shouldPinStructDefWhenPuttingWorkflowEventDef() {
            final String WORKFLOW_EVT_DEF_NAME = "wed-pin";

            // Put WorkflowEventDef
            PutWorkflowEventDefRequest wReq = PutWorkflowEventDefRequest.newBuilder()
                    .setName(WORKFLOW_EVT_DEF_NAME)
                    .setContentType(ReturnType.newBuilder()
                            .setReturnType(TypeDefinition.newBuilder()
                                    .setStructDefId(StructDefId.newBuilder()
                                            .setName(STRUCT_DEF_NAME)
                                            .setVersion(-1))))
                    .build();

            client.putWorkflowEventDef(wReq);

            WorkflowEventDefId id = WorkflowEventDefId.newBuilder()
                    .setName(WORKFLOW_EVT_DEF_NAME)
                    .build();

            waitForWorkflowEventDef(id);
            WorkflowEventDef gotWed = client.getWorkflowEventDef(id);

            StructDefId sdidWed = gotWed.getContentType().getReturnType().getStructDefId();
            assertThat(sdidWed.getVersion()).isEqualTo(1);
        }

        public Workflow getWfPinTest() {
            return Workflow.newWorkflow(WF_SPEC_NAME, w -> {
                w.declareStruct("in", STRUCT_DEF_NAME).required();
            });
        }
    }

    @Nested
    class NoSchemaUpdatesEvolutionTest {
        @Test
        void shouldThrowErrorWhenPuttingNewField() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                    .setName("car")
                    .setAllowedUpdates(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build())
                            .putFields(
                                    "year",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                            .build()))
                    .build();

            assertThatThrownBy(() -> client.putStructDef(updatedStructDef))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage(
                            "INVALID_ARGUMENT: Incompatible StructDef evolution on field(s): [year] using NO_SCHEMA_UPDATES compatibility type");
        }

        @Test
        void shouldValidateInvalidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-4")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-4", 0);

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-4"))
                            .setCompatibilityType(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.INT))
                                                    .setDefaultValue(VariableValue.newBuilder()
                                                            .setInt(1970))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isFalse();
        }

        @Test
        void shouldValidateValidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-4")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-4", 0);

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-4"))
                            .setCompatibilityType(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.STR))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isTrue();
        }
    }

    @Nested
    class FullyCompatibleSchemaUpdatesEvolutionTest {
        @Test
        void shouldAllowCompatibleStructDefEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-2")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                    .setName("car-2")
                    .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build())
                            .putFields(
                                    "year",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                            .setDefaultValue(
                                                    VariableValue.newBuilder().setInt(10))
                                            .build())
                            .putFields(
                                    "sold",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                            .setDefaultValue(
                                                    VariableValue.newBuilder().setBool(false))
                                            .build()))
                    .build();

            assertThat(client.putStructDef(updatedStructDef))
                    .extracting(item -> item.getId().getVersion())
                    .isEqualTo(1);
        }

        @Test
        void shouldThrowErrorWhenPuttingIncompatibleStructDefEvolutions() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-3")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build())
                            .putFields(
                                    "isSold",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                            .build()))
                    .build());

            PutStructDefRequest updatedStructDef = PutStructDefRequest.newBuilder()
                    .setName("car-3")
                    .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                            .build())
                            .putFields(
                                    "year",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                            .build()))
                    .build();

            assertThatThrownBy(() -> client.putStructDef(updatedStructDef))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage(
                            "INVALID_ARGUMENT: Incompatible StructDef evolution on field(s): [year, isSold, model] using FULLY_COMPATIBLE_SCHEMA_UPDATES compatibility type");
        }

        @Test
        void shouldValidateValidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-5")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-5", 0);

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-5"))
                            .setCompatibilityType(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.INT))
                                                    .setDefaultValue(VariableValue.newBuilder()
                                                            .setInt(1970))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isTrue();
        }

        @Test
        void shouldValidateInvalidSchemaEvolution() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-6")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "model",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());

            waitForStructDef("car-6", 0);

            ValidateStructDefEvolutionResponse resp =
                    client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                            .setStructDefId(StructDefId.newBuilder().setName("car-6"))
                            .setCompatibilityType(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                            .setStructDef(InlineStructDef.newBuilder()
                                    .putFields(
                                            "model",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.STR))
                                                    .build())
                                    .putFields(
                                            "year",
                                            StructFieldDef.newBuilder()
                                                    .setFieldType(TypeDefinition.newBuilder()
                                                            .setPrimitiveType(VariableType.INT))
                                                    .build()))
                            .build());

            assertThat(resp.getIsValid()).isFalse();
        }
    }

    @Nested
    class StructDefFieldNameValidationTest {
        @Test
        public void shouldAcceptStructDefFieldWithCamelCase() {
            client.putStructDef(PutStructDefRequest.newBuilder()
                    .setName("car-67")
                    .setStructDef(InlineStructDef.newBuilder()
                            .putFields(
                                    "brandName9734",
                                    StructFieldDef.newBuilder()
                                            .setFieldType(
                                                    TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                            .build()))
                    .build());
        }

        @Test
        public void shouldRejectStructDefFieldWithUnderscore() {
            assertThatThrownBy(() -> {
                        client.putStructDef(PutStructDefRequest.newBuilder()
                                .setName("car-68")
                                .setStructDef(InlineStructDef.newBuilder()
                                        .putFields(
                                                "brand_name",
                                                StructFieldDef.newBuilder()
                                                        .setFieldType(TypeDefinition.newBuilder()
                                                                .setPrimitiveType(VariableType.STR))
                                                        .build()))
                                .build());
                    })
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("cannot include underscores");
        }

        @Test
        public void shouldRejectStructDefFieldWithNumericFirstCharacter() {
            assertThatThrownBy(() -> {
                        client.putStructDef(PutStructDefRequest.newBuilder()
                                .setName("car-70")
                                .setStructDef(InlineStructDef.newBuilder()
                                        .putFields(
                                                "8D",
                                                StructFieldDef.newBuilder()
                                                        .setFieldType(TypeDefinition.newBuilder()
                                                                .setPrimitiveType(VariableType.STR))
                                                        .build()))
                                .build());
                    })
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("first character must be a letter");
        }

        @Test
        public void shouldRejectStructDefFieldWithCapitalFirstLetter() {
            assertThatThrownBy(() -> {
                        client.putStructDef(PutStructDefRequest.newBuilder()
                                .setName("car-71")
                                .setStructDef(InlineStructDef.newBuilder()
                                        .putFields(
                                                "BrandName",
                                                StructFieldDef.newBuilder()
                                                        .setFieldType(TypeDefinition.newBuilder()
                                                                .setPrimitiveType(VariableType.STR))
                                                        .build()))
                                .build());
                    })
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("first letter must be lowercase");
        }

        @Test
        public void shouldRejectStructDefFieldWithDollarSign() {
            assertThatThrownBy(() -> {
                        client.putStructDef(PutStructDefRequest.newBuilder()
                                .setName("car-72")
                                .setStructDef(InlineStructDef.newBuilder()
                                        .putFields(
                                                "brandName$",
                                                StructFieldDef.newBuilder()
                                                        .setFieldType(TypeDefinition.newBuilder()
                                                                .setPrimitiveType(VariableType.STR))
                                                        .build()))
                                .build());
                    })
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("cannot include special characters");
        }
    }

    private void waitForStructDef(String name, Integer version) {
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    StructDefId.Builder structDefId = StructDefId.newBuilder().setName(name);
                    if (version != null) {
                        structDefId.setVersion(version);
                    }
                    client.getStructDef(structDefId.build());
                    return true;
                });
    }

    private void waitForWfSpec(WfSpecId id) {
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getWfSpec(id);
                    return true;
                });
    }

    private void waitForTaskDef(TaskDefId id) {
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getTaskDef(id);
                    return true;
                });
    }

    private void waitForExternalEventDef(ExternalEventDefId id) {
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getExternalEventDef(id);
                    return true;
                });
    }

    private void waitForWorkflowEventDef(WorkflowEventDefId id) {
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    client.getWorkflowEventDef(id);
                    return true;
                });
    }
}

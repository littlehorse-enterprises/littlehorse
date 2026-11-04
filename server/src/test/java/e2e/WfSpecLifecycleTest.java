package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import e2e.Struct.PinStructV0;
import e2e.Struct.PinStructV1;
import e2e.Struct.UnknownStructDef;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@LHTest
public class WfSpecLifecycleTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldRejectPutWfSpecRequestWithInvalidStructDef() {
        Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
            wf.declareStruct("my-struct", UnknownStructDef.class).required();
        });

        assertThatThrownBy(() -> {
                    client.putWfSpec(originalWorkflow.compileWorkflow());
                })
                .hasMessageContaining("Refers to non-existent StructDef");
    }

    @Test
    void shouldRejectMissingStructDefInsideInlineStruct() {
        TypeDefinition missingStructType = TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder()
                        .setName("missing-nested-struct-def")
                        .setVersion(-1))
                .build();
        PutWfSpecRequest request =
                getWfSpecWithInlineStructVariable("wfspec-missing-nested-struct", "details", missingStructType);

        assertThatThrownBy(() -> client.putWfSpec(request))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        exn -> assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT))
                .hasMessageContaining("missing-nested-struct-def");
    }

    @Test
    void shouldRejectForbiddenJsonInsideInlineStruct() {
        TypeDefinition jsonType = TypeDefinition.newBuilder()
                .setPrimitiveType(VariableType.JSON_OBJ)
                .build();
        PutWfSpecRequest request = getWfSpecWithInlineStructVariable("wfspec-invalid-inline-json", "payload", jsonType);

        assertThatThrownBy(() -> client.putWfSpec(request))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        exn -> assertThat(exn.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT))
                .hasMessageContaining("Forbidden JSON type: JSON_OBJ");
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class AllowAllUpdates {
        @Test
        void shouldBeIdempotent() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion());
            assertThat(updatedSpec.getId().getRevision())
                    .isEqualTo(originalSpec.getId().getRevision());
        }

        @Test
        void shouldCreateMinorRevisionWhenUpdatingTheWorkflow() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
                wf.addVariable("optional", VariableType.STR);
            });

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion());
            assertThat(updatedSpec.getId().getRevision())
                    .isEqualTo(originalSpec.getId().getRevision() + 1);
        }

        @Test
        void shouldAddMajorVersionWhenWfSpecHasBreakingChanges() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
                wf.addVariable("second-required", VariableType.BOOL).required();
            });

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion() + 1);
            assertThat(updatedSpec.getId().getRevision()).isEqualTo(0);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @WithStructDefs({PinStructV0.class, PinStructV1.class})
    class StructDefPinning {
        final String WF_SPEC_NAME = "wfspec-pin-test";
        final String STRUCT_DEF_NAME = "pin-struct";

        @Test
        void shouldPinStructDefVersionWhenPuttingWfSpec() {
            String name = WF_SPEC_NAME + "-implicit";
            client.putWfSpec(getWfPinTestImplicitVersion(name).compileWorkflow());

            WfSpecId id = WfSpecId.newBuilder().setName(name).build();

            waitForWfSpec(id);
            WfSpec got = client.getWfSpec(id);

            StructDefId sdid = got.getThreadSpecsMap()
                    .get("entrypoint")
                    .getVariableDefs(0)
                    .getVarDef()
                    .getTypeDef()
                    .getStructDefId();
            assertThat(sdid.getName()).isEqualTo(STRUCT_DEF_NAME);
            assertThat(sdid.getVersion()).isEqualTo(1);
        }

        @Test
        void shouldPinStructDefVersionInsideInlineStruct() {
            String name = WF_SPEC_NAME + "-inline";
            TypeDefinition structType = TypeDefinition.newBuilder()
                    .setStructDefId(
                            StructDefId.newBuilder().setName(STRUCT_DEF_NAME).setVersion(-1))
                    .build();
            client.putWfSpec(getWfSpecWithInlineStructVariable(name, "details", structType));

            WfSpecId id = WfSpecId.newBuilder().setName(name).build();
            waitForWfSpec(id);
            StructDefId nestedStructDefId = client.getWfSpec(id)
                    .getThreadSpecsOrThrow("entrypoint")
                    .getVariableDefs(0)
                    .getVarDef()
                    .getTypeDef()
                    .getInlineStructDef()
                    .getFieldsOrThrow("details")
                    .getFieldType()
                    .getStructDefId();

            assertThat(nestedStructDefId.getName()).isEqualTo(STRUCT_DEF_NAME);
            assertThat(nestedStructDefId.getVersion()).isEqualTo(1);
        }

        @Test
        void shouldHonorExplicitStructDefVersionWhenPuttingWfSpec() {
            // Compile the workflow proto and explicitly pin the Struct version to 0
            String name = WF_SPEC_NAME + "-explicit";
            PutWfSpecRequest req = getWfPinTestExplicitVersionV0(name).compileWorkflow();

            client.putWfSpec(req);

            WfSpecId id = WfSpecId.newBuilder().setName(name).build();
            waitForWfSpec(id);
            WfSpec got = client.getWfSpec(id);

            StructDefId sdid = got.getThreadSpecsMap()
                    .get("entrypoint")
                    .getVariableDefs(0)
                    .getVarDef()
                    .getTypeDef()
                    .getStructDefId();
            assertThat(sdid.getName()).isEqualTo(STRUCT_DEF_NAME);
            assertThat(sdid.getVersion()).isEqualTo(0);
        }

        @Test
        void shouldRejectWhenExplicitStructDefVersionNotFound() {
            // Build a workflow that pins to a non-existent version (e.g., 999)
            String name = WF_SPEC_NAME + "-notfound";
            PutWfSpecRequest.Builder reqBuilder = getWfPinTestExplicitVersionV2(name).compileWorkflow().toBuilder();

            // Expect the server to reject the WfSpec because that struct version doesn't exist
            assertThatThrownBy(() -> client.putWfSpec(reqBuilder.build())).isInstanceOf(StatusRuntimeException.class);
        }

        public Workflow getWfPinTestImplicitVersion(String name) {
            return new WorkflowImpl(name, thread -> {
                thread.declareStruct("in", STRUCT_DEF_NAME).required();
            });
        }

        public Workflow getWfPinTestExplicitVersionV0(String name) {
            return new WorkflowImpl(name, thread -> {
                thread.declareStruct("in", STRUCT_DEF_NAME, 0).required();
            });
        }

        public Workflow getWfPinTestExplicitVersionV2(String name) {
            return new WorkflowImpl(name, thread -> {
                thread.declareStruct("in", STRUCT_DEF_NAME, 999).required();
            });
        }
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

    private PutWfSpecRequest getWfSpecWithInlineStructVariable(
            String wfSpecName, String fieldName, TypeDefinition fieldType) {
        PutWfSpecRequest.Builder request = Workflow.newWorkflow(wfSpecName, wf -> {
                    wf.addVariable("input", VariableType.STR).required();
                })
                .compileWorkflow()
                .toBuilder();
        String entrypointName = request.getEntrypointThreadName();
        ThreadSpec.Builder entrypoint = request.getThreadSpecsOrThrow(entrypointName).toBuilder();
        ThreadVarDef.Builder variable = entrypoint.getVariableDefs(0).toBuilder();
        variable.getVarDefBuilder()
                .setTypeDef(TypeDefinition.newBuilder()
                        .setInlineStructDef(InlineStructDef.newBuilder()
                                .putFields(
                                        fieldName,
                                        StructFieldDef.newBuilder()
                                                .setFieldType(fieldType)
                                                .build())));
        entrypoint.setVariableDefs(0, variable.build());
        request.putThreadSpecs(entrypointName, entrypoint.build());
        return request.build();
    }

    @Nested
    class AllowNone {
        @Test
        void shouldReturnBeIdempotent() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                    })
                    .withUpdateType(AllowedUpdateType.NO_UPDATES);

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion());
            assertThat(updatedSpec.getId().getRevision())
                    .isEqualTo(originalSpec.getId().getRevision());
        }

        @Test
        void shouldThrowAlreadyExistsWhenUpdatingWfSpec() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("some", VariableType.STR);
                    })
                    .withUpdateType(AllowedUpdateType.NO_UPDATES);

            assertThatThrownBy(() -> client.putWfSpec(updatedWorkflow.compileWorkflow()))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage("ALREADY_EXISTS: WfSpec already exists.");
        }
    }

    @Nested
    class MinorRevisionOnly {
        @Test
        void shouldReturnBeIdempotent() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion());
            assertThat(updatedSpec.getId().getRevision())
                    .isEqualTo(originalSpec.getId().getRevision());
        }

        @Test
        void shouldCreateMinorRevisionWhenUpdatingTheWorkflow() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("optional", VariableType.STR);
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            WfSpec updatedSpec = client.putWfSpec(updatedWorkflow.compileWorkflow());

            assertThat(updatedSpec.getId().getMajorVersion())
                    .isEqualTo(originalSpec.getId().getMajorVersion());
            assertThat(updatedSpec.getId().getRevision())
                    .isEqualTo(originalSpec.getId().getRevision() + 1);
        }

        @Test
        void shouldThrowFailedPreconditionWhenWfSpecHasBreakingChanges() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            client.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("searchable", VariableType.BOOL).required();
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            assertThatThrownBy(() -> client.putWfSpec(updatedWorkflow.compileWorkflow()))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage("FAILED_PRECONDITION: The resulting WfSpec has a breaking change.");
        }
    }
}

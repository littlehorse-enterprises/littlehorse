package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import e2e.Struct.UnknownStructDef;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
                .hasMessageContaining("refers to non-existent StructDef");
    }

    @Nested
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

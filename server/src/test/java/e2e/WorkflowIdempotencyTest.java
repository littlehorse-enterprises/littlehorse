package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowIdempotencyTest {

    private LHPublicApiBlockingStub lhClient;

    @Nested
    class AllowAllUpdates {
        @Test
        void shouldBeIdempotent() {
            Workflow originalWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
            });

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
                wf.addVariable("optional", VariableType.STR);
            });

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                wf.addVariable("variable", VariableType.BOOL).required();
                wf.addVariable("searchable", VariableType.BOOL).searchable();
            });

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                    })
                    .withUpdateType(AllowedUpdateType.NO_UPDATES);

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("some", VariableType.STR);
                    })
                    .withUpdateType(AllowedUpdateType.NO_UPDATES);

            assertThatThrownBy(() -> lhClient.putWfSpec(updatedWorkflow.compileWorkflow()))
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

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            WfSpec originalSpec = lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("optional", VariableType.STR);
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            WfSpec updatedSpec = lhClient.putWfSpec(updatedWorkflow.compileWorkflow());

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

            lhClient.putWfSpec(originalWorkflow.compileWorkflow());

            Workflow updatedWorkflow = Workflow.newWorkflow("sample", wf -> {
                        wf.addVariable("variable", VariableType.BOOL).required();
                        wf.addVariable("searchable", VariableType.BOOL).searchable();
                    })
                    .withUpdateType(AllowedUpdateType.MINOR_REVISION_UPDATES);

            assertThatThrownBy(() -> lhClient.putWfSpec(updatedWorkflow.compileWorkflow()))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessage("FAILED_PRECONDITION: The resulting WfSpec has a breaking change.");
        }
    }
}

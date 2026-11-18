package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.ERROR;
import static io.littlehorse.sdk.common.proto.LHStatus.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.FormDef;
import io.littlehorse.sdk.common.proto.FormDefReference;
import io.littlehorse.sdk.common.proto.FormField;
import io.littlehorse.sdk.common.proto.FormFieldDef;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.sdk.common.proto.GetFormDefRequest;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutFormDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchFormDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class FormDefTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    @Test
        void shouldVersionGetAndSearchFormDefs() {
        StructDef structDef = putStructDef();
        String name = "form-" + UUID.randomUUID();
        PutFormDefRequest request = formDefRequest(name, structDef, literal("Approve?"));

        FormDef original = client.putFormDef(request);
        FormDef copy = client.putFormDef(request);
        FormDef updated = client.putFormDef(formDefRequest(name, structDef, literal("Approve this order?")));

        assertThat(original.getId().getVersion()).isEqualTo(0);
        assertThat(copy.getId()).isEqualTo(original.getId());
        assertThat(updated.getId().getVersion()).isEqualTo(1);

        awaitFormDef(name);
        assertThat(client.getFormDef(
                                GetFormDefRequest.newBuilder().setName(name).build())
                        .getId())
                .isEqualTo(updated.getId());
        assertThat(client.getFormDef(GetFormDefRequest.newBuilder()
                                .setName(name)
                                .setVersion(0)
                                .build())
                        .getFields(0)
                        .getMetadataOrThrow("label"))
                .isEqualTo(literal("Approve?"));
        assertThat(client.searchFormDef(
                                SearchFormDefRequest.newBuilder().setName(name).build())
                        .getResultsList())
                .containsExactly(original.getId(), updated.getId());

    }

    @Test
    void shouldRejectInvalidFormDefs() {
        StructDef structDef = putStructDef();

        assertThatThrownBy(() -> client.putFormDef(PutFormDefRequest.newBuilder()
                        .setName("form-" + UUID.randomUUID())
                        .setResultStructDefId(structDef.getId())
                        .addFields(field("missing", literal("x")))
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Invalid FormDef field path");

        assertThatThrownBy(() -> client.putFormDef(PutFormDefRequest.newBuilder()
                        .setName("form-" + UUID.randomUUID())
                        .setResultStructDefId(structDef.getId())
                        .addFields(field("approved", literal("x")))
                        .addFields(field("approved", literal("y")))
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Duplicate FormDef field path");

        assertThatThrownBy(() -> client.putFormDef(formDefRequest(
                        "form-" + UUID.randomUUID(),
                        structDef.toBuilder()
                                .setId(structDef.getId().toBuilder().setName("missing-" + UUID.randomUUID()))
                                .build(),
                        literal("x"))))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
        void shouldRejectChangingResultStructDefNameAcrossFormVersions() {
        String name = "form-" + UUID.randomUUID();
        StructDef originalSchema = putStructDef();
        StructDef unrelatedSchema = putStructDef();
        FormDef original = client.putFormDef(formDefRequest(name, originalSchema, literal("Approve?")));

        assertThatThrownBy(() -> client.putFormDef(formDefRequest(name, unrelatedSchema, literal("Approve?"))))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("cannot change its result StructDef");
        assertThat(client.getFormDef(GetFormDefRequest.newBuilder().setName(name).build()))
                .isEqualTo(original);
    }

    @Test
    void shouldRejectFormDefForDifferentStructDef() {
        FormDef formDef = client.putFormDef(formDefRequest("form-" + UUID.randomUUID(), putStructDef(), literal("x")));
        StructDef otherStructDef = putStructDef();

        assertThatThrownBy(() -> client.putUserTaskDef(PutUserTaskDefRequest.newBuilder()
                        .setName("form-task-" + UUID.randomUUID())
                        .setResultStructDefId(otherStructDef.getId())
                        .setFormDef(FormDefReference.newBuilder()
                                .setName(formDef.getId().getName()))
                        .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("describes StructDef");
    }

    @Test
    void shouldRejectPlaceholderFromMaskedVariable() {
        String userTaskDefName = putFormTask(placeholder("greeting"));
        Workflow workflow = Workflow.newWorkflow("form-wf-" + UUID.randomUUID(), thread -> {
            WfRunVariable ssn = thread.declareStr("ssn").masked();
            thread.assignUserTask(userTaskDefName, "obiwan", null)
                    .withPlaceholder("greeting", thread.format("SSN: {0}", ssn));
        });

        assertThatThrownBy(() -> client.putWfSpec(workflow.compileWorkflow()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("exposes a masked value");
    }

    @Test
    void shouldRejectFormattedMaskedStructFieldInPlaceholder() {
        String profileName = "profile-" + UUID.randomUUID();
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(profileName)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "secret",
                                StructFieldDef.newBuilder()
                                        .setFieldType(TypeDefinition.newBuilder()
                                                .setPrimitiveType(VariableType.STR)
                                                .setMasked(true))
                                        .build()))
                .build());
        String taskName = putFormTask(placeholder("greeting"));
        Workflow workflow = Workflow.newWorkflow("form-wf-" + UUID.randomUUID(), thread -> {
            WfRunVariable profile = thread.declareStruct("profile", profileName, 0);
            thread.assignUserTask(taskName, "obiwan", null)
                    .withPlaceholder("greeting", thread.format("Hello {0}", profile.get("secret")));
        });

        assertThatThrownBy(() -> client.putWfSpec(workflow.compileWorkflow()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Placeholder greeting exposes a masked value");
    }

    @Test
    void shouldRejectWfSpecWithMissingPlaceholder() {
        String userTaskDefName = putFormTask(placeholder("greeting"));
        Workflow workflow = Workflow.newWorkflow(
                "form-wf-" + UUID.randomUUID(), thread -> thread.assignUserTask(userTaskDefName, "obiwan", null));

        assertThatThrownBy(() -> client.putWfSpec(workflow.compileWorkflow()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Missing FormDef placeholders");
    }

    @Test
        void shouldDeleteWfRunAfterFormPlaceholderEvaluationFails() {
                String taskName = putFormTask(placeholder("greeting"));
                Workflow workflow = Workflow.newWorkflow("form-wf-" + UUID.randomUUID(), thread -> {
                        WfRunVariable customer = thread.declareStr("customer");
                        thread.assignUserTask(taskName, "obiwan", null)
                                        .withPlaceholder("greeting", thread.format("{0,number}", customer));
                });
                var id = workflowVerifier
                                .prepareRun(workflow, Arg.of("customer", "Jake"))
                                .waitForStatus(ERROR)
                                .thenVerifyNodeRun(0, 1, node -> {
                                        assertThat(node.getFailures(0).getMessage()).contains("Failed evaluating placeholder greeting");
                                        assertThat(node.getUserTask().hasUserTaskRunId()).isFalse();
                                })
                                .start();

                client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(id).build());
                assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
                            client.getWfRun(id);
                            return null;
                        }))
                        .isTrue();
        }

        @Test
    void shouldResolveFormOnUserTaskRun() {
        String userTaskDefName = putFormTask(placeholder("greeting"));
        Workflow workflow = Workflow.newWorkflow("form-wf-" + UUID.randomUUID(), thread -> {
            WfRunVariable customer = thread.declareStr("customer");
            var displayName = thread.execute("form-display-name", customer);
            thread.assignUserTask(userTaskDefName, "obiwan", null)
                    .withPlaceholder("greeting", thread.format("Hello {0}, approve?", displayName));
        });

        workflowVerifier
                .prepareRun(workflow, Arg.of("customer", "Jake"))
                .waitForNodeRunStatus(0, 2, RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    UserTaskRun run = client.getUserTaskRun(client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .build())
                            .getResults(0)
                            .getId());
                    FormField field = run.getForm().getFields(0);
                    assertThat(field.getMetadataOrThrow("label").getStr()).isEqualTo("Hello Jake, approve?");
                    assertThat(field.getMetadataOrThrow("widget").getStr()).isEqualTo("checkbox");
                    assertThat(run.getForm().getMetadataOrThrow("title").getStr())
                            .isEqualTo("Review");
                })
                .start();
    }

        @LHTaskMethod("form-display-name")
        public String displayName(String customer) {
                return customer;
        }

        private String putFormTask(FormMetadataValue label) {
        StructDef structDef = putStructDef();
        FormDef formDef = client.putFormDef(formDefRequest("form-" + UUID.randomUUID(), structDef, label));
        String userTaskDefName = "form-task-" + UUID.randomUUID();
        client.putUserTaskDef(PutUserTaskDefRequest.newBuilder()
                .setName(userTaskDefName)
                .setResultStructDefId(structDef.getId())
                .setFormDef(
                        FormDefReference.newBuilder().setName(formDef.getId().getName()))
                .build());
        return userTaskDefName;
    }

    private StructDef putStructDef() {
        return client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("form-output-" + UUID.randomUUID())
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "approved",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                        .build()))
                .build());
    }

    private static PutFormDefRequest formDefRequest(String name, StructDef structDef, FormMetadataValue label) {
        return PutFormDefRequest.newBuilder()
                .setName(name)
                .setResultStructDefId(structDef.getId())
                .putMetadata("title", literal("Review"))
                .addFields(field("approved", label).toBuilder().putMetadata("widget", literal("checkbox")))
                .build();
    }

    private static FormFieldDef field(String key, FormMetadataValue label) {
        return FormFieldDef.newBuilder()
                .setFieldPath(
                        LHPath.newBuilder().addPath(LHPath.Selector.newBuilder().setKey(key)))
                .putMetadata("label", label)
                .build();
    }

    private static FormMetadataValue literal(String value) {
        return FormMetadataValue.newBuilder()
                .setLiteral(VariableValue.newBuilder().setStr(value))
                .build();
    }

    private static FormMetadataValue placeholder(String name) {
        return FormMetadataValue.newBuilder().setPlaceholder(name).build();
    }

    private void awaitFormDef(String name) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .ignoreExceptionsMatching(LHTestExceptionUtil::isNotFoundException)
                .until(() -> client.getFormDef(
                                GetFormDefRequest.newBuilder().setName(name).build())
                        != null);
    }
}

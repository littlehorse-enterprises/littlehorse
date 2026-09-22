package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.COMPLETED;
import static io.littlehorse.sdk.common.proto.LHStatus.ERROR;
import static io.littlehorse.sdk.common.proto.LHStatus.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WorkflowVerifier;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class StructBackedUserTaskTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    @Test
    void shouldCompleteWithStructOutput() {
        StructBackedTask task = registerStructBackedTask();
        Workflow workflow = workflow(task.userTaskDefName());

        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    UserTaskRunId userTaskRunId = getUserTaskRunId(wfRun.getId());
                    SaveUserTaskRunProgressRequest.Builder progress = SaveUserTaskRunProgressRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId("obiwan");
                    assertThatThrownBy(() -> client.saveUserTaskRunProgress(progress.clone()
                                    .putResults(
                                            "approved",
                                            VariableValue.newBuilder()
                                                    .setBool(true)
                                                    .build())
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("Use output instead of results");
                    assertThatThrownBy(() -> client.saveUserTaskRunProgress(progress.clone()
                                    .setOutput(VariableValue.newBuilder().setBool(true))
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("must contain a Struct output");
                    assertThatThrownBy(() -> client.saveUserTaskRunProgress(progress.clone()
                                    .setOutput(VariableValue.newBuilder()
                                            .setStruct(Struct.newBuilder()
                                                    .setStructDefId(StructDefId.newBuilder()
                                                            .setName("wrong-schema"))
                                                    .setStruct(InlineStruct.getDefaultInstance())))
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("result StructDefId");
                    client.saveUserTaskRunProgress(SaveUserTaskRunProgressRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId("obiwan")
                            .setOutput(VariableValue.newBuilder()
                                    .setStruct(Struct.newBuilder()
                                            .setStructDefId(task.structDef().getId())
                                            .setStruct(InlineStruct.newBuilder()
                                                    .putFields(
                                                            "approved",
                                                            StructField.newBuilder()
                                                                    .setValue(VariableValue.newBuilder()
                                                                            .setBool(true))
                                                                    .build()))))
                            .build());

                    UserTaskRun saved = client.getUserTaskRun(userTaskRunId);
                    assertThat(saved.getResultsMap()).isEmpty();
                    assertThat(saved.getOutput().getStruct().getStruct().getFieldsMap())
                            .containsOnlyKeys("approved");

                    client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId("obiwan")
                            .setOutput(structOutput(task.structDef(), true))
                            .build());
                })
                .waitForStatus(COMPLETED)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    UserTaskRun userTaskRun =
                            client.getUserTaskRun(nodeRun.getUserTask().getUserTaskRunId());
                    assertThat(userTaskRun.getStatus()).isEqualTo(UserTaskRunStatus.DONE);
                    assertThat(userTaskRun.getResultsMap()).isEmpty();
                    assertThat(userTaskRun
                                    .getOutput()
                                    .getStruct()
                                    .getStruct()
                                    .getFieldsMap()
                                    .get("approved")
                                    .getValue()
                                    .getBool())
                            .isTrue();
                    assertThat(userTaskRun
                                    .getOutput()
                                    .getStruct()
                                    .getStruct()
                                    .getFieldsMap()
                                    .get("reviewer")
                                    .getValue()
                                    .getStr())
                            .isEqualTo("obiwan");
                    assertThat(userTaskRun
                                    .getOutput()
                                    .getStruct()
                                    .getStruct()
                                    .getFieldsMap()
                                    .get("source")
                                    .getValue()
                                    .getStr())
                            .isEqualTo("manual");
                })
                .start();
    }

    @Test
    void shouldRejectOutputWithWrongFieldType() {
        StructBackedTask task = registerStructBackedTask();
        Workflow workflow = workflow(task.userTaskDefName());

        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    UserTaskRunId userTaskRunId = getUserTaskRunId(wfRun.getId());
                    VariableValue invalidOutput = VariableValue.newBuilder()
                            .setStruct(Struct.newBuilder()
                                    .setStructDefId(task.structDef().getId())
                                    .setStruct(InlineStruct.newBuilder()
                                            .putFields(
                                                    "approved",
                                                    StructField.newBuilder()
                                                            .setValue(VariableValue.newBuilder()
                                                                    .setStr("not-a-boolean"))
                                                            .build())))
                            .build();

                    assertThatThrownBy(() -> client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                                    .setUserTaskRunId(userTaskRunId)
                                    .setUserId("obiwan")
                                    .setOutput(invalidOutput)
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("Invalid UserTaskRun output");

                    assertThatThrownBy(() -> client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                                    .setUserTaskRunId(userTaskRunId)
                                    .setUserId("obiwan")
                                    .setOutput(VariableValue.getDefaultInstance())
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("must contain a Struct value");

                    client.cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .build());
                })
                .waitForStatus(ERROR)
                .start();
    }

    @Test
    void shouldRejectInvalidCompletionRepresentations() {
        StructBackedTask task = registerStructBackedTask();
        Workflow workflow = workflow(task.userTaskDefName());

        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    UserTaskRunId userTaskRunId = getUserTaskRunId(wfRun.getId());

                    assertThatThrownBy(() -> client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                                    .setUserTaskRunId(userTaskRunId)
                                    .setUserId("obiwan")
                                    .putResults(
                                            "approved",
                                            VariableValue.newBuilder()
                                                    .setBool(true)
                                                    .build())
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("Use output instead of results");

                    VariableValue missingRequiredField = VariableValue.newBuilder()
                            .setStruct(Struct.newBuilder()
                                    .setStructDefId(task.structDef().getId())
                                    .setStruct(InlineStruct.getDefaultInstance()))
                            .build();
                    assertThatThrownBy(() -> client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                                    .setUserTaskRunId(userTaskRunId)
                                    .setUserId("obiwan")
                                    .setOutput(missingRequiredField)
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("Missing required field approved");

                    VariableValue wrongStructDef = VariableValue.newBuilder()
                            .setStruct(Struct.newBuilder()
                                    .setStructDefId(StructDefId.newBuilder()
                                            .setName("another-struct")
                                            .setVersion(7))
                                    .setStruct(InlineStruct.getDefaultInstance()))
                            .build();
                    assertThatThrownBy(() -> client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                                    .setUserTaskRunId(userTaskRunId)
                                    .setUserId("obiwan")
                                    .setOutput(wrongStructDef)
                                    .build()))
                            .isInstanceOf(StatusRuntimeException.class)
                            .hasMessageContaining("Invalid UserTaskRun output");

                    client.cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .build());
                })
                .waitForStatus(ERROR)
                .start();
    }

    private StructBackedTask registerStructBackedTask() {
        String suffix = UUID.randomUUID().toString();
        StructDef structDef = client.putStructDef(PutStructDefRequest.newBuilder()
                .setName("approval-output-" + suffix)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "approved",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                        .build())
                        .putFields(
                                "reviewer",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .putFields(
                                "source",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setStr("manual"))
                                        .build()))
                .build());
        String userTaskDefName = "struct-user-task-" + suffix;
        client.putUserTaskDef(PutUserTaskDefRequest.newBuilder()
                .setName(userTaskDefName)
                .setResultStructDefId(structDef.getId())
                .build());
        return new StructBackedTask(userTaskDefName, structDef);
    }

    private Workflow workflow(String userTaskDefName) {
        return new WorkflowImpl(
                "struct-user-task-wf-" + UUID.randomUUID(),
                thread -> thread.assignUserTask(userTaskDefName, "obiwan", null));
    }

    private UserTaskRunId getUserTaskRunId(io.littlehorse.sdk.common.proto.WfRunId wfRunId) {
        return client.listUserTaskRuns(
                        ListUserTaskRunRequest.newBuilder().setWfRunId(wfRunId).build())
                .getResults(0)
                .getId();
    }

    private VariableValue structOutput(StructDef structDef, boolean approved) {
        return VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(structDef.getId())
                        .setStruct(InlineStruct.newBuilder()
                                .putFields(
                                        "approved",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setBool(approved))
                                                .build())
                                .putFields(
                                        "reviewer",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setStr("obiwan"))
                                                .build())))
                .build();
    }

    private record StructBackedTask(String userTaskDefName, StructDef structDef) {}
}

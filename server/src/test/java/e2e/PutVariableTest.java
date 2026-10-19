package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutVariableRequest;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@LHTest
public class PutVariableTest {
    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("put-variable")
    private Workflow workflow;

    @LHWorkflow("put-variable-sleep")
    private Workflow sleepWorkflow;

    @LHWorkflow("put-variable-sleep")
    public Workflow buildSleepWorkflow() {
        return Workflow.newWorkflow("put-variable-sleep", thread -> {
            var duration = thread.declareInt("duration").withDefault(3600);
            thread.sleepSeconds(duration);
        });
    }

    @Test
    void shouldAdvanceWorkflowAfterReplacingVariable() {
        verifier.prepareRun(sleepWorkflow)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> client.putVariable(PutVariableRequest.newBuilder()
                        .setId(VariableId.newBuilder()
                                .setWfRunId(wfRun.getId())
                                .setThreadRunNumber(0)
                                .setName("duration"))
                        .setValue(VariableValue.newBuilder().setInt(0))
                        .build()))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldExtendRelativeSleepFromNodeArrivalTime() {
        verifier.prepareRun(sleepWorkflow, Arg.of("duration", 1))
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> client.putVariable(PutVariableRequest.newBuilder()
                        .setId(VariableId.newBuilder()
                                .setWfRunId(wfRun.getId())
                                .setThreadRunNumber(0)
                                .setName("duration"))
                        .setValue(VariableValue.newBuilder().setInt(3))
                        .build()))
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(5))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    long arrivalTime =
                            LHLibUtil.fromProtoTs(nodeRun.getArrivalTime()).getTime();
                    long endTime = LHLibUtil.fromProtoTs(nodeRun.getEndTime()).getTime();
                    assertThat(endTime - arrivalTime).isGreaterThanOrEqualTo(3000);
                })
                .start();
    }

    @LHWorkflow("put-variable")
    public Workflow buildWorkflow() {
        return Workflow.newWorkflow("put-variable", thread -> {
            var value = thread.declareStr("value").withDefault("original");
            var observed = thread.declareStr("observed");
            thread.declareStr("secret").withDefault("sensitive").masked();
            thread.waitForEvent("put-variable-continue").registeredAs(String.class);
            observed.assign(value);
        });
    }

    @Test
    void shouldReplaceExistingVariableAndUseItAfterEvent() {
        verifier.prepareRun(workflow)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    VariableId id = VariableId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(0)
                            .setName("value")
                            .build();
                    Variable before = client.getVariable(id);
                    VariableValue replacement =
                            VariableValue.newBuilder().setStr("replacement").build();
                    VariableValue previousValue = client.putVariable(PutVariableRequest.newBuilder()
                            .setId(id)
                            .setValue(replacement)
                            .build());
                    assertThat(previousValue).isEqualTo(before.getValue());
                    VariableValue maskedPreviousValue = client.putVariable(PutVariableRequest.newBuilder()
                            .setId(id.toBuilder().setName("secret"))
                            .setValue(replacement)
                            .build());
                    assertThat(maskedPreviousValue.getStr()).isEqualTo(LHConstants.STRING_MASK);
                    assertThat(client.getVariable(id))
                            .isEqualTo(before.toBuilder().setValue(replacement).build());
                    assertThat(client.getWfRun(wfRun.getId()).getStatus()).isEqualTo(LHStatus.RUNNING);

                    StatusRuntimeException missing = assertThrows(
                            StatusRuntimeException.class,
                            () -> client.putVariable(PutVariableRequest.newBuilder()
                                    .setId(id.toBuilder().setName("missing"))
                                    .setValue(replacement)
                                    .build()));
                    assertThat(missing.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
                    StatusRuntimeException malformed = assertThrows(
                            StatusRuntimeException.class,
                            () -> client.putVariable(
                                    PutVariableRequest.newBuilder().setId(id).build()));
                    assertThat(malformed.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                })
                .thenSendExternalEventWithContent("put-variable-continue", "continue")
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(
                        0, "observed", value -> assertThat(value.getStr()).isEqualTo("replacement"))
                .start();
    }

    @Test
    void shouldAllowExplicitNull() {
        verifier.prepareRun(workflow)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    VariableId id = VariableId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(0)
                            .setName("value")
                            .build();
                    VariableValue previousValue = client.putVariable(PutVariableRequest.newBuilder()
                            .setId(id)
                            .setValue(VariableValue.getDefaultInstance())
                            .build());
                    assertThat(previousValue.getStr()).isEqualTo("original");
                    assertThat(client.getVariable(id).getValue()).isEqualTo(VariableValue.getDefaultInstance());
                    assertThat(client.putVariable(PutVariableRequest.newBuilder()
                                    .setId(id)
                                    .setValue(VariableValue.newBuilder().setStr("restored"))
                                    .build()))
                            .isEqualTo(VariableValue.getDefaultInstance());
                })
                .thenSendExternalEventWithContent("put-variable-continue", "continue")
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldRejectUpdateAfterWorkflowCompletes() {
        verifier.prepareRun(workflow)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenSendExternalEventWithContent("put-variable-continue", "continue")
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    VariableId id = VariableId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(0)
                            .setName("value")
                            .build();
                    Variable before = client.getVariable(id);
                    StatusRuntimeException error = assertThrows(
                            StatusRuntimeException.class,
                            () -> client.putVariable(PutVariableRequest.newBuilder()
                                    .setId(id)
                                    .setValue(VariableValue.newBuilder().setStr("updated"))
                                    .build()));
                    assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.FAILED_PRECONDITION);
                    assertThat(error.getStatus().getDescription())
                            .isEqualTo("Cannot update variables on a completed WfRun");
                    assertThat(client.getVariable(id)).isEqualTo(before);
                })
                .start();
    }
}

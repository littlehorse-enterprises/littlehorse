package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskAttempt.ResultCase;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class VarSubErrorTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("var-type-validations")
    private Workflow varTypeValidationsWf;

    //    @Test
    void shouldThrowInvalidArgumentIfRequiredVarMissing() {
        // The RunWf RPC would throw an error, and currently the test framework doesn't have
        // a way to test this. So we first run a simple test with the framework to ensure
        // that the WfSpec gets created.
        verifier.prepareRun(
                        varTypeValidationsWf,
                        Arg.of("input-int", 2),
                        Arg.of("input-json", Map.of("int", 1, "string", "hello")))
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        String wfRunId = UUID.randomUUID().toString();

        // Now we run our test manually.
        assertThatThrownBy(() -> {
                    client.runWf(RunWfRequest.newBuilder()
                            .setWfSpecName("var-type-validations")
                            .setId(wfRunId)
                            .build());
                })
                .matches(exn -> {
                    assertThat(exn).isInstanceOf(StatusRuntimeException.class);
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);

                    return sre.getMessage().toLowerCase().contains("input-int");
                });

        // Next, we need to make sure the WfRun wasn't actually saved since it had invalid variables.
        assertThatThrownBy(() -> {
                    client.getWfRun(WfRunId.newBuilder().setId(wfRunId).build());
                })
                .matches(exn -> {
                    assertThat(exn).isInstanceOf(StatusRuntimeException.class);
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.NOT_FOUND;
                });

        // Finally, we are going to test that RunWf throws an error when we provide the wrong types.
        assertThatThrownBy(() -> {
                    client.runWf(RunWfRequest.newBuilder()
                            .setWfSpecName("var-type-validations")
                            .setId(wfRunId)
                            .putVariables("input-int", LHLibUtil.objToVarVal("not-an-int"))
                            .putVariables("input-json", LHLibUtil.objToVarVal(Map.of("int", 1, "string", "hello")))
                            .build());
                })
                .matches(exn -> {
                    assertThat(exn).isInstanceOf(StatusRuntimeException.class);
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);

                    return sre.getMessage().toLowerCase().contains("input-int");
                });
    }

    //    @Test
    void shouldFindVarSubErrorOnFirstNodeRun() {
        verifier.prepareRun(
                        varTypeValidationsWf,
                        Arg.of("input-int", 123),
                        Arg.of("input-json", Map.of("int", "notanint", "str", "yod")))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nr -> {
                    assertThat(nr.getFailuresCount()).isEqualTo(1);
                    Failure failure = nr.getFailures(0);
                    assertThat(failure.getFailureName()).isEqualTo(LHErrorType.VAR_SUB_ERROR.toString());
                })
                .start();
    }

    @Test
    void shouldBeAbleToPassNullAsIntIntoTaskRunAndTaskRunShouldFail() {
        Map<String, String> jsonStuff = new HashMap<>();
        jsonStuff.put("str", "my-str");
        jsonStuff.put("int", null);

        verifier.prepareRun(varTypeValidationsWf, Arg.of("input-int", 123), Arg.of("input-json", jsonStuff))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    assertThat(taskRun.getStatus()).isEqualTo(TaskStatus.TASK_FAILED);

                    assertThat(taskRun.getInputVariables(0).getValue().getValueCase())
                            .isEqualTo(ValueCase.VALUE_NOT_SET);
                    TaskAttempt attempt = taskRun.getAttempts(0);
                    assertThat(attempt.getResultCase()).isEqualTo(ResultCase.ERROR);
                })
                .start();
    }

    //    @Test
    void shouldAllowPrimitiveToString() {
        verifier.prepareRun(
                        varTypeValidationsWf,
                        Arg.of("input-int", 123),
                        Arg.of("input-json", Map.of("int", 1234, "string", 5432)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 3, taskRun -> {
                    assertThat(taskRun.getInputVariables(0).getValue().getStr()).isEqualTo("5432");
                })
                .thenVerifyTaskRunResult(
                        0, 3, result -> assertThat(result.getStr()).isEqualTo("5432!"))
                .start();
    }

    //    @Test
    void shouldNotCastDoubleToInt() {
        verifier.prepareRun(
                        varTypeValidationsWf,
                        Arg.of("input-int", 123),
                        Arg.of("input-json", Map.of("int", 12.3, "string", 5432)))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(0).getCurrentNodePosition()).isEqualTo(1);
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    assertThat(nodeRun.getFailuresCount()).isEqualTo(1);
                    Failure failure = nodeRun.getFailures(0);
                    assertThat(failure.getFailureName()).isEqualTo(LHErrorType.VAR_SUB_ERROR.toString());
                    assertThat(failure.getMessage()).contains("DOUBLE");
                })
                .start();
    }

    /*
     * Allows testers to check that variable type validations are working properly on the following:
     * - input variables
     * - input to first task run
     * - input to later task runs
     */
    @LHWorkflow("var-type-validations")
    public Workflow getVarTypeValidationsWf() {
        return Workflow.newWorkflow("var-type-validations", wf -> {
            WfRunVariable inputInt =
                    wf.addVariable("input-int", VariableType.INT).required();
            WfRunVariable inputJson = wf.addVariable("input-json", VariableType.JSON_OBJ);

            wf.execute("takes-in-int", inputJson.jsonPath("$.int"));
            wf.execute("takes-in-int", inputInt);
            wf.execute("takes-in-string", inputJson.jsonPath("$.string"));
        });
    }

    @LHTaskMethod("takes-in-int")
    public int obiWan(int input) {
        return input + 1;
    }

    @LHTaskMethod("takes-in-string")
    public String anakin(String input) {
        return input + "!";
    }
}

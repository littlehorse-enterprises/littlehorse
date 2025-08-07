package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class TypeValidationsTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    void cannotPassStrExtEvtOutputIntoTaskNeedingInt() {
        Workflow badWorkflow = Workflow.newWorkflow("shouldnt-work", wf -> {
            NodeOutput result =
                    wf.waitForEvent("validations-some-event-return-str").registeredAs(String.class);
            wf.execute("validations-accept-int", result);
        });

        assertThatThrownBy(() -> {
                    badWorkflow.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("expects type int but is type str");
                });
    }

    @Test
    void cannotPassDoubleOutputIntoTaskNeedingString() {
        Workflow badWorkflow = Workflow.newWorkflow("shouldnt-work-double-str", wf -> {
            NodeOutput result = wf.execute("validations-return-double", "input");
            wf.execute("validations-accept-string", result);
        });
        assertThatThrownBy(() -> {
                    badWorkflow.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("expects type str but is type double");
                });
    }

    @Test
    void cannotPassStringOutputIntoTaskNeedingDouble() {
        Workflow badWorkflow = Workflow.newWorkflow("shouldnt-work-str-double", wf -> {
            NodeOutput result = wf.execute("validations-return-string");
            wf.execute("validations-accept-double", result);
        });
        assertThatThrownBy(() -> {
                    badWorkflow.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("expects type double but is type str");
                });
    }

    @Test
    void canPassIntOutputIntoTaskNeedingInt() {
        Workflow goodWorkflow = Workflow.newWorkflow("should-work-int-int", wf -> {
            NodeOutput result = wf.execute("validations-return-int", "input");
            wf.execute("validations-accept-int", result);
        });
        // Should not throw
        goodWorkflow.registerWfSpec(client);
    }

    @Test
    void cannotMultiplyAStr() {
        Workflow badWorkflow = Workflow.newWorkflow("shouldnt-work-multiply-str", wf -> {
            WfRunVariable myStr = wf.declareStr("foo");
            wf.execute("validations-return-int", myStr.multiply(5));
        });
        assertThatThrownBy(() -> {
                    badWorkflow.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("cannot multiply to a str");
                });
    }

    @Test
    void cannotAssignAStrToADoubleVar() {
        Workflow badWorkflow = Workflow.newWorkflow("shouldnt-work-multiply-str", wf -> {
            WfRunVariable myStr = wf.declareStr("foo");
            WfRunVariable myInt = wf.declareInt("my-int");
            myInt.assign(myStr);
        });
        assertThatThrownBy(() -> {
                    badWorkflow.registerWfSpec(client);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage()
                                    .toLowerCase()
                                    .contains("mutation of variable my-int invalid: cannot use a str as a int");
                });
    }

    @LHTaskMethod("validations-return-int")
    public int returnInt(String input) {
        return 1234;
    }

    @LHTaskMethod("validations-return-double")
    public double returnDouble(String input) {
        return 1.5;
    }

    @LHTaskMethod("validations-return-string")
    public String returnString() {
        return "hello there";
    }

    @LHTaskMethod("validations-accept-int")
    public String result(int input) {
        return String.valueOf(input);
    }

    @LHTaskMethod("validations-accept-double")
    public String result(double input) {
        return String.valueOf(input);
    }

    @LHTaskMethod("validations-accept-string")
    public String result(String input) {
        return input;
    }
}

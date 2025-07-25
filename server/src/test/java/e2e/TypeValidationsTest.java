package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.NodeOutput;
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
}

package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import e2e.Struct.Person;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest
public class LHPathTest {
    @LHWorkflow("test-lh-path")
    private Workflow lhPathWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    public void shouldPerformGetOnStruct() {
        client.putStructDef(new LHStructDefType(Person.class).toPutStructDefRequest());

        Person person = new Person(
                "Obi-Wan Kenobi",
                List.of("Yoda", "Anakin Skywalker"),
                Map.of("home", "111-222-3344", "work", "555-667-7788"));

        verifier.prepareRun(lhPathWf, Arg.of("person", person))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Greetings Obi-Wan Kenobi"))
                .thenVerifyTaskRunResult(0, 2, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Looking up Yoda"))
                .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Calling 111-222-3344"))
                .start();
    }

    @LHWorkflow("test-lh-path")
    public Workflow testLhPathWorkflow() {
        return new WorkflowImpl("test-basic", wf -> {
            WfRunVariable personStruct =
                    wf.declareStruct("person", Person.class).required();

            wf.execute("greet", personStruct.get("name"));
            wf.execute("lookup-friend", personStruct.get("friends").get(0));
            wf.execute("call-phone", personStruct.get("phoneNumbers").get("home"));
        });
    }

    @LHTaskMethod("greet")
    public String greet(String name) {
        return "Greetings " + name;
    }

    @LHTaskMethod("lookup-friend")
    public String lookupFriend(String friend) {
        return "Looking up " + friend;
    }

    @LHTaskMethod("call-phone")
    public String callPhone(String phoneNumber) {
        return "Calling " + phoneNumber;
    }
}

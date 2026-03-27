package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import e2e.Struct.Person;
import e2e.Struct.PersonPojo;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class LHPathTest {
    @LHWorkflow("lh-path-structs")
    private Workflow lhPathStructsWf;

    @LHWorkflow("lh-path-json-obj")
    private Workflow lhPathJsonObjWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    public void shouldPerformGetOnStruct() {
        client.putStructDef(new LHStructDefType(Person.class).toPutStructDefRequest());

        Person person = new Person(
                "Obi-Wan Kenobi",
                List.of("Yoda", "Anakin Skywalker"),
                Map.of("home", "111-222-3344", "work", "555-667-7788"));

        verifier.prepareRun(lhPathStructsWf, Arg.of("person", person))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Greetings Obi-Wan Kenobi"))
                .thenVerifyTaskRunResult(0, 2, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Looking up Yoda"))
                .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Calling 111-222-3344"))
                .start();
    }

    @Test
    void shouldFailGetOnStructWithInvalidField() {
        client.putStructDef(new LHStructDefType(Person.class).toPutStructDefRequest());

        Workflow invalid = Workflow.newWorkflow("fail-get-on-struct", wf -> {
            WfRunVariable person = wf.declareStruct("person", Person.class);
            wf.execute("greet-lh-path", person.get("age"));
        });

        Assertions.assertThatThrownBy(() -> {
                    invalid.registerWfSpec(client);
                })
                .matches(exn -> {
                    return exn instanceof StatusRuntimeException;
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                            && sre.getMessage().toLowerCase().contains("could not find field 'age'");
                });
    }

    @LHWorkflow("lh-path-structs")
    public Workflow testLhPathWorkflowOnStructs() {
        return new WorkflowImpl("lh-path-structs", wf -> {
            WfRunVariable personStruct =
                    wf.declareStruct("person", Person.class).required();

            wf.execute("greet-lh-path", personStruct.get("name"));
            wf.execute("lookup-friend", personStruct.get("friends").get(0));
            wf.execute("call-phone", personStruct.get("phoneNumbers").get("home"));
        });
    }

    @Test
    public void shouldPerformGetOnJsonObj() {
        PersonPojo person = new PersonPojo(
                "Obi-Wan Kenobi",
                List.of("Yoda", "Anakin Skywalker"),
                Map.of("home", "111-222-3344", "work", "555-667-7788"));

        verifier.prepareRun(lhPathJsonObjWf, Arg.of("person", person))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Greetings Obi-Wan Kenobi"))
                .thenVerifyTaskRunResult(0, 2, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Looking up Yoda"))
                .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("Calling 111-222-3344"))
                .start();
    }

    @LHWorkflow("lh-path-json-obj")
    public Workflow testLhPathWorkflowOnJSONObj() {
        return new WorkflowImpl("lh-path-json-obj", wf -> {
            WfRunVariable personJsonObj = wf.declareJsonObj("person").required();

            wf.execute("greet-lh-path", personJsonObj.get("name"));
            wf.execute("lookup-friend", personJsonObj.get("friends").get(0));
            wf.execute("call-phone", personJsonObj.get("phoneNumbers").get("home"));
        });
    }

    @LHTaskMethod("greet-lh-path")
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

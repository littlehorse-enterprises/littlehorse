package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructOneOf;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs(StructOneOfJavaTest.Payment.class)
public class StructOneOfJavaTest {
    @LHStructOneOf
    public record Method(String card, String bank) {}

    @LHStructDef("oneof-java-payment")
    public record Payment(String orderId, Method method) {}

    private WorkflowVerifier verifier;

    @LHWorkflow("oneof-java-roundtrip")
    private Workflow workflow;

    @LHWorkflow("oneof-java-switch")
    private Workflow switchWorkflow;

    @LHWorkflow("oneof-java-switch")
    public Workflow switchWorkflow() {
        return Workflow.newWorkflow(
                "oneof-java-switch",
                wf -> {
                    var payment = wf.declareStruct("payment", Payment.class).required();
                    // Selecting before clearing works because only the edge's final value is validated.
                    payment.get("bank").assign("acct");
                    payment.get("card").assign(null);
                },
                new LHConfig());
    }

    @LHWorkflow("oneof-java-roundtrip")
    public Workflow workflow() {
        return Workflow.newWorkflow(
                "oneof-java-roundtrip",
                wf -> {
                    var input = wf.declareStruct("payment", Payment.class).required();
                    var output = wf.declareStruct("output", Payment.class);
                    output.assign(wf.execute("oneof-java-echo", input));
                },
                new LHConfig());
    }

    @LHTaskMethod("oneof-java-echo")
    public Payment echo(Payment payment) {
        assertThat(payment.method()).isNotNull();
        return payment;
    }

    @Test
    void helperRoundTripsThroughWorkerAndUsesFlatPaths() {
        verifier.prepareRun(workflow, Arg.of("payment", new Payment("order", new Method("token", null))))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "output", value -> {
                    var fields = value.getStruct().getStruct().getFieldsMap();
                    assertThat(fields).containsOnlyKeys("orderId", "card");
                    assertThat(fields.get("card").getValue().getStr()).isEqualTo("token");
                })
                .start();
    }

    @Test
    void switchesAlternativesOnOneEdgeInEitherOrder() {
        verifier.prepareRun(switchWorkflow, Arg.of("payment", new Payment("order", new Method("token", null))))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "payment", value -> {
                    var fields = value.getStruct().getStruct().getFieldsMap();
                    assertThat(fields).containsOnlyKeys("orderId", "bank");
                    assertThat(fields.get("bank").getValue().getStr()).isEqualTo("acct");
                })
                .start();
    }

    @Test
    void nullHelperRoundTripsThroughWorkerAsEmptySelection() {
        verifier.prepareRun(workflow, Arg.of("payment", new Payment("order", null)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "output", value -> assertThat(
                                value.getStruct().getStruct().getFieldsMap())
                        .containsOnlyKeys("orderId"))
                .start();
    }
}

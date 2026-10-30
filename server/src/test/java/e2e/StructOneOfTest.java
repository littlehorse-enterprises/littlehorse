package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
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
@WithStructDefs({StructOneOfTest.Payment.class})
public class StructOneOfTest {
    @LHStructDef("oneof-server-payment")
    public record Payment(
            String orderId,
            @LHStructOneOf(group = "method", required = true) String card,
            @LHStructOneOf(group = "method", required = true) String bank) {}

    @LHStructDef("oneof-server-payment")
    public record NestedPayment(String orderId, Method method) {}

    @LHStructOneOf(required = true)
    public record Method(String card, String bank) {}

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("oneof-round-trip")
    private Workflow roundTrip;

    @LHWorkflow("oneof-path-mutation")
    private Workflow mutation;

    @LHWorkflow("oneof-builder")
    private Workflow builder;

    @Test
    void bothJavaDeclarationsUseTheSameServerSchema() {
        for (Object payment :
                new Object[] {new Payment("order", "token", null), new NestedPayment("order", new Method("token", null))
                }) {
            verifier.prepareRun(roundTrip, Arg.of("payment", payment))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(
                            0, "result", result -> assertThat(method(result).getFieldsMap())
                                    .containsOnlyKeys("card"))
                    .thenVerifyVariable(0, "unused", result -> assertThat(result.getValueCase())
                            .isEqualTo(VariableValue.ValueCase.VALUE_NOT_SET))
                    .start();
        }
    }

    @Test
    void serverRejectsInvalidSelectionsFromRawProtobuf() {
        StructField selected = StructField.newBuilder()
                .setValue(VariableValue.newBuilder().setStr("value"))
                .build();
        for (InlineStruct choice : new InlineStruct[] {
            InlineStruct.getDefaultInstance(),
            InlineStruct.newBuilder()
                    .putFields("card", selected)
                    .putFields("bank", selected)
                    .build(),
            InlineStruct.newBuilder().putFields("unknown", selected).build(),
            InlineStruct.newBuilder()
                    .putFields("card", StructField.getDefaultInstance())
                    .build()
        }) {
            VariableValue valid = LHLibUtil.objToVarVal(new Payment("order", "token", null));
            VariableValue invalid = valid.toBuilder()
                    .setStruct(valid.getStruct().toBuilder()
                            .setStruct(valid.getStruct().getStruct().toBuilder()
                                    .putFields(
                                            "method",
                                            StructField.newBuilder()
                                                    .setValue(VariableValue.newBuilder()
                                                            .setStruct(Struct.newBuilder()
                                                                    .setStruct(choice)))
                                                    .build())))
                    .build();
            StatusRuntimeException error = assertThrows(
                    StatusRuntimeException.class,
                    () -> client.runWf(RunWfRequest.newBuilder()
                            .setWfSpecName(roundTrip.getName())
                            .putVariables("payment", invalid)
                            .build()));
            assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        }
    }

    @Test
    void selectingASecondAlternativeFailsWithoutChangingTheValue() {
        verifier.prepareRun(mutation, Arg.of("payment", new Payment("order", "token", null)))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyVariable(
                        0, "payment", result -> assertThat(method(result).getFieldsMap())
                                .containsOnlyKeys("card"))
                .start();
    }

    @Test
    void builderCanReplaceTheSelection() {
        verifier.prepareRun(
                        builder, Arg.of("payment", new Payment("order", "token", null)), Arg.of("account", "account-1"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(
                        0, "payment", result -> assertThat(method(result).getFieldsMap())
                                .containsOnlyKeys("bank"))
                .start();
    }

    @Test
    void registrationPreservesChoicesAndRejectsIncompatibleEvolution() {
        var stored = client.getStructDef(
                StructDefId.newBuilder().setName("oneof-server-payment").build());
        var field = stored.getStructDef().getFieldsOrThrow("method");
        var choice = field.getFieldType().getInlineStructDef();
        assertThat(choice.hasOneofConfig()).isTrue();
        assertThat(choice.getOneofConfig().getSelectionRequired()).isTrue();
        var changed = stored.getStructDef().toBuilder()
                .putFields(
                        "method",
                        field.toBuilder()
                                .setFieldType(field.getFieldType().toBuilder()
                                        .setInlineStructDef(choice.toBuilder()
                                                .putFields("wallet", choice.getFieldsOrThrow("card"))))
                                .build())
                .build();
        var validation = client.validateStructDefEvolution(ValidateStructDefEvolutionRequest.newBuilder()
                .setStructDefId(stored.getId())
                .setStructDef(changed)
                .setCompatibilityType(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .build());
        assertThat(validation.getIsValid()).isFalse();
        var updateError = assertThrows(
                StatusRuntimeException.class,
                () -> client.putStructDef(PutStructDefRequest.newBuilder()
                        .setName(stored.getId().getName())
                        .setStructDef(changed)
                        .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                        .build()));
        assertThat(updateError.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        var rootError = assertThrows(
                StatusRuntimeException.class,
                () -> client.putStructDef(PutStructDefRequest.newBuilder()
                        .setName("oneof-invalid-root")
                        .setStructDef(choice)
                        .build()));
        assertThat(rootError.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    private InlineStruct method(VariableValue payment) {
        return payment.getStruct()
                .getStruct()
                .getFieldsOrThrow("method")
                .getValue()
                .getStruct()
                .getStruct();
    }

    @LHWorkflow("oneof-round-trip")
    public Workflow roundTrip() {
        return Workflow.newWorkflow("oneof-round-trip", wf -> {
            var payment = wf.declareStruct("payment", Payment.class).required();
            var result = wf.declareStruct("result", NestedPayment.class);
            var unused = wf.declareStr("unused");
            unused.assign(payment.get("method").get("bank"));
            result.assign(wf.execute("oneof-echo", payment));
        });
    }

    @LHWorkflow("oneof-path-mutation")
    public Workflow mutation() {
        return Workflow.newWorkflow("oneof-path-mutation", wf -> {
            var payment = wf.declareStruct("payment", Payment.class).required();
            payment.get("method").get("bank").assign("account");
        });
    }

    @LHWorkflow("oneof-builder")
    public Workflow builder() {
        return Workflow.newWorkflow("oneof-builder", wf -> {
            var payment = wf.declareStruct("payment", Payment.class).required();
            var account = wf.declareStr("account").required();
            payment.assign(wf.buildStruct("oneof-server-payment")
                    .put("orderId", payment.get("orderId"))
                    .put("method", wf.buildInlineStruct().put("bank", account)));
            wf.execute("oneof-echo", payment);
        });
    }

    @LHTaskMethod("oneof-echo")
    public NestedPayment echo(NestedPayment payment) {
        return payment;
    }
}

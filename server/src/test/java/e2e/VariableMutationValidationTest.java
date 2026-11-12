package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import e2e.Struct.MutationProfile;
import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.exception.LHJsonProcessingException;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs({MutationProfile.class})
public class VariableMutationValidationTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("mutation-validation-valid-mutations")
    private Workflow validMutationsWf;

    @LHWorkflow("mutation-validation-null-non-nullable-struct-field")
    private Workflow nullNonNullableStructFieldWf;

    @LHWorkflow("mutation-validation-rollback-on-validation-failure")
    private Workflow rollbackOnValidationFailureWf;

    @LHWorkflow("mutation-validation-masked-struct-field-path-mutation")
    private Workflow maskedStructFieldPathMutationWf;

    @Test
    void shouldKeepApplyingValidMutations() {
        verifier.prepareRun(
                        validMutationsWf,
                        Arg.of("my-int", 5),
                        Arg.of("my-double", 1.5),
                        Arg.of("my-json-arr", List.of("a")),
                        Arg.of("my-json-obj", Map.of("name", "old", "count", 1)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-int", val -> assertThat(val.getInt()).isEqualTo(14))
                .thenVerifyVariable(
                        0, "my-double", val -> assertThat(val.getDouble()).isEqualTo(6.0))
                .thenVerifyVariable(
                        0, "my-arr", val -> assertThat(val.getArray().getItemsList())
                                .extracting(VariableValue::getInt)
                                .containsExactly(1L, 2L, 3L, 4L))
                .thenVerifyVariable(0, "my-json-arr", val -> {
                    JsonArray jsonArray = parseJson(val.getJsonArr(), JsonArray.class);
                    assertThat(jsonArray.size()).isEqualTo(2);
                    assertThat(jsonArray.get(0).getAsString()).isEqualTo("a");
                    assertThat(jsonArray.get(1).getAsString()).isEqualTo("b");
                })
                .thenVerifyVariable(0, "my-json-obj", val -> {
                    JsonObject jsonObject = parseJson(val.getJsonObj(), JsonObject.class);
                    assertThat(jsonObject.get("name").getAsString()).isEqualTo("new");
                    assertThat(jsonObject.get("count").getAsLong()).isEqualTo(3L);
                    assertThat(jsonObject.get("added").getAsBoolean()).isTrue();
                })
                .start();
    }

    @Test
    void shouldFailWhenAssigningNullToNonNullableStructField() {
        verifier.prepareRun(
                        nullNonNullableStructFieldWf,
                        Arg.of("profile", new MutationProfile("Obi-Wan", "hello there")))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> assertThat(nodeRun.getFailuresList())
                        .extracting(Failure::getFailureName)
                        .contains(LHErrorType.VAR_SUB_ERROR.name()))
                .thenVerifyVariable(0, "profile", val -> assertThat(
                                val.getStruct().getStruct().getFieldsMap().get("name"))
                        .extracting(field -> field.getValue().getStr())
                        .isEqualTo("Obi-Wan"))
                .start();
    }

    @Test
    void shouldRollbackAllMutationsWhenValidationFails() {
        verifier.prepareRun(
                        rollbackOnValidationFailureWf,
                        Arg.of("profile", new MutationProfile("Obi-Wan", "hello there")))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> assertThat(nodeRun.getFailuresList())
                        .extracting(Failure::getFailureName)
                        .contains(LHErrorType.VAR_SUB_ERROR.name()))
                .thenVerifyVariable(0, "counter", val -> assertThat(val.getInt()).isZero())
                .thenVerifyVariable(0, "profile", val -> assertThat(
                                val.getStruct().getStruct().getFieldsMap().get("name"))
                        .extracting(field -> field.getValue().getStr())
                        .isEqualTo("Obi-Wan"))
                .start();
    }

    @Test
    void shouldMaskStructFieldCreatedByPathMutation() {
        VariableValue profile = LHLibUtil.objToVarVal(new MutationProfile("Obi-Wan", null));
        VariableValue profileWithoutSecret = profile.toBuilder()
                .setStruct(profile.getStruct().toBuilder()
                        .setStruct(profile.getStruct().getStruct().toBuilder().removeFields("secret")))
                .build();

        verifier.prepareRun(maskedStructFieldPathMutationWf, Arg.of("profile", profileWithoutSecret))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "profile", val -> {
                    StructField secret =
                            val.getStruct().getStruct().getFieldsMap().get("secret");
                    assertThat(secret).isNotNull();
                    assertThat(secret.getMasked()).isTrue();
                    assertThat(secret.getValue().getStr()).isEqualTo(LHConstants.STRING_MASK);
                })
                .start();
    }

    @LHWorkflow("mutation-validation-valid-mutations")
    public Workflow buildValidMutationsWf() {
        return Workflow.newWorkflow(
                "mutation-validation-valid-mutations",
                wf -> {
                    WfRunVariable myInt = wf.declareInt("my-int").required();
                    WfRunVariable myDouble = wf.declareDouble("my-double").required();
                    WfRunVariable myArr = wf.declareArray("my-arr", Long.class).withDefault(new Long[] {1L, 2L, 3L});
                    WfRunVariable myJsonArr = wf.declareJsonArr("my-json-arr").required();
                    WfRunVariable myJsonObj = wf.declareJsonObj("my-json-obj").required();

                    // INT += DOUBLE is coerced back to INT: (5 + 2.5) -> 7, then 7 * 2 -> 14
                    wf.mutate(myInt, VariableMutationType.ADD, 2.5);
                    wf.mutate(myInt, VariableMutationType.MULTIPLY, 2);
                    wf.mutate(myDouble, VariableMutationType.MULTIPLY, 4);
                    wf.mutate(myArr, VariableMutationType.EXTEND, 4L);
                    wf.mutate(myJsonArr, VariableMutationType.EXTEND, new ArrayList<>(List.of("b")));
                    myJsonObj.get("name").assign("new");
                    wf.mutate(myJsonObj.jsonPath("$.count"), VariableMutationType.ADD, 2);
                    wf.mutate(myJsonObj.jsonPath("$.added"), VariableMutationType.ASSIGN, true);
                },
                new LHConfig());
    }

    @LHWorkflow("mutation-validation-null-non-nullable-struct-field")
    public Workflow buildNullNonNullableStructFieldWf() {
        return Workflow.newWorkflow(
                "mutation-validation-null-non-nullable-struct-field",
                wf -> {
                    WfRunVariable profile =
                            wf.declareStruct("profile", MutationProfile.class).required();
                    profile.get("name").assign(null);
                },
                new LHConfig());
    }

    @LHWorkflow("mutation-validation-rollback-on-validation-failure")
    public Workflow buildRollbackOnValidationFailureWf() {
        return Workflow.newWorkflow(
                "mutation-validation-rollback-on-validation-failure",
                wf -> {
                    WfRunVariable profile =
                            wf.declareStruct("profile", MutationProfile.class).required();
                    WfRunVariable counter = wf.declareInt("counter").withDefault(0);
                    counter.assign(1);
                    profile.get("name").assign(null);
                },
                new LHConfig());
    }

    @LHWorkflow("mutation-validation-masked-struct-field-path-mutation")
    public Workflow buildMaskedStructFieldPathMutationWf() {
        return Workflow.newWorkflow(
                "mutation-validation-masked-struct-field-path-mutation",
                wf -> {
                    WfRunVariable profile =
                            wf.declareStruct("profile", MutationProfile.class).required();
                    profile.get("secret").assign("hunter2");
                },
                new LHConfig());
    }

    private static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return LHLibUtil.deserializeFromjson(json, clazz);
        } catch (LHJsonProcessingException exn) {
            throw new RuntimeException(exn);
        }
    }
}

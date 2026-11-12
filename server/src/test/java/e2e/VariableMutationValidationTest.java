package e2e;

import static org.assertj.core.api.Assertions.assertThat;

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

    @LHWorkflow("mutation-validation-primitives")
    private Workflow primitivesWf;

    @LHWorkflow("mutation-validation-structs")
    private Workflow structsWf;

    @Test
    void shouldKeepApplyingValidMutations() {
        verifier.prepareRun(
                        primitivesWf,
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
                .thenVerifyVariable(0, "my-json-arr", val -> assertThat(parseJson(val.getJsonArr(), List.class))
                        .containsExactly("a", "b"))
                .thenVerifyVariable(0, "my-json-obj", val -> assertThat(parseJson(val.getJsonObj(), Map.class))
                        .containsEntry("name", "new")
                        .containsEntry("count", 3L)
                        .containsEntry("added", true))
                .start();
    }

    @Test
    void shouldFailWhenAssigningNullToNonNullableStructField() {
        verifier.prepareRun(
                        structsWf,
                        Arg.of("action", "null-name"),
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
    void shouldAllowTemporarilyInvalidValueRestoredOnSameEdge() {
        verifier.prepareRun(
                        structsWf,
                        Arg.of("action", "restore-name"),
                        Arg.of("profile", new MutationProfile("Obi-Wan", "hello there")))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "profile", val -> assertThat(
                                val.getStruct().getStruct().getFieldsMap().get("name"))
                        .extracting(field -> field.getValue().getStr())
                        .isEqualTo("Ben"))
                .start();
    }

    @Test
    void shouldMaskStructFieldCreatedByPathMutation() {
        VariableValue profile = LHLibUtil.objToVarVal(new MutationProfile("Obi-Wan", null));
        VariableValue profileWithoutSecret = profile.toBuilder()
                .setStruct(profile.getStruct().toBuilder()
                        .setStruct(profile.getStruct().getStruct().toBuilder().removeFields("secret")))
                .build();

        verifier.prepareRun(structsWf, Arg.of("action", "set-secret"), Arg.of("profile", profileWithoutSecret))
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

    @LHWorkflow("mutation-validation-primitives")
    public Workflow buildPrimitivesWf() {
        return Workflow.newWorkflow(
                "mutation-validation-primitives",
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

    @LHWorkflow("mutation-validation-structs")
    public Workflow buildStructsWf() {
        return Workflow.newWorkflow(
                "mutation-validation-structs",
                wf -> {
                    WfRunVariable action = wf.declareStr("action").required();
                    WfRunVariable profile =
                            wf.declareStruct("profile", MutationProfile.class).required();

                    wf.doIf(action.isEqualTo("null-name"), then -> profile.get("name")
                            .assign(null));
                    wf.doIf(action.isEqualTo("set-secret"), then -> profile.get("secret")
                            .assign("hunter2"));
                    wf.doIf(action.isEqualTo("restore-name"), then -> {
                        profile.get("name").assign(null);
                        profile.get("name").assign("Ben");
                    });
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

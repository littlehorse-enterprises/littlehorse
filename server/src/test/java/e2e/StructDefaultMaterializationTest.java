package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * Verifies that when a struct is provided at ingress with a field omitted, and that field has an
 * explicit default value in its StructDef, the default is materialized into the persisted WfRun
 * variable (rather than being silently left absent).
 */
@LHTest
public class StructDefaultMaterializationTest {
    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @Test
    void shouldMaterializeDefaultForNullableFieldAbsentAtIngress() {
        String structDefName = "materialize-nullable-default";
        registerStructWithDefaultedName(structDefName, true);

        Workflow wf = buildExtractNameWorkflow("materialize-nullable-wf", structDefName);

        verifier.prepareRun(wf, Arg.of("obj", emptyStruct(structDefName)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "obj", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    assertThat(fields).containsKey("name");
                    assertThat(fields.get("name").getValue().getStr()).isEqualTo("default-name");
                })
                .thenVerifyVariable(0, "extracted-name", variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("default-name"))
                .start();
    }

    @Test
    void shouldMaterializeDefaultForNonNullableFieldAbsentAtIngress() {
        String structDefName = "materialize-non-nullable-default";
        registerStructWithDefaultedName(structDefName, false);

        Workflow wf = buildExtractNameWorkflow("materialize-non-nullable-wf", structDefName);

        verifier.prepareRun(wf, Arg.of("obj", emptyStruct(structDefName)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "obj", variableValue -> {
                    var fields = variableValue.getStruct().getStruct().getFieldsMap();
                    assertThat(fields).containsKey("name");
                    assertThat(fields.get("name").getValue().getStr()).isEqualTo("default-name");
                })
                .thenVerifyVariable(0, "extracted-name", variableValue -> assertThat(variableValue.getStr())
                        .isEqualTo("default-name"))
                .start();
    }

    private void registerStructWithDefaultedName(String structDefName, boolean nullable) {
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(structDefName)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "name",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .setIsNullable(nullable)
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setStr("default-name"))
                                        .build()))
                .build());
        waitForStructDef(structDefName);
    }

    private Workflow buildExtractNameWorkflow(String wfName, String structDefName) {
        return Workflow.newWorkflow(wfName, w -> {
            WfRunVariable obj = w.declareStruct("obj", structDefName).required();
            WfRunVariable extractedName = w.declareStr("extracted-name");
            w.mutate(extractedName, VariableMutationType.ASSIGN, obj.get("name"));
        });
    }

    private VariableValue emptyStruct(String structDefName) {
        return VariableValue.newBuilder()
                .setStruct(Struct.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName(structDefName))
                        .setStruct(InlineStruct.newBuilder()))
                .build();
    }

    private void waitForStructDef(String name) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .ignoreExceptionsMatching(LHTestExceptionUtil::isNotFoundException)
                .until(() -> {
                    client.getStructDef(StructDefId.newBuilder().setName(name).build());
                    return true;
                });
    }
}

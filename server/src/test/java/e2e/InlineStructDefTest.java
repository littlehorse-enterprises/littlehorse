package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import e2e.Struct.Location;
import e2e.Struct.Product;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs({Product.class})
@WithWorkers("inlineStructWorker")
public class InlineStructDefTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("inline-struct-product-wf")
    private Workflow productWf;

    @Test
    void wfSpecVariableDeclaredAsInlineStructSurvivesRoundTrip() {
        String wfName = "inline-struct-rt-" + UUID.randomUUID();
        WfSpec stored = client.putWfSpec(Workflow.newWorkflow(wfName, wf -> {
                    wf.declareInlineStruct("location", Location.class);
                })
                .compileWorkflow());

        WfSpecId id = stored.getId();
        Awaitility.await()
                .atMost(Duration.ofMillis(500))
                .ignoreExceptionsMatching(LHTestExceptionUtil::isNotFoundException)
                .until(() -> {
                    client.getWfSpec(id);
                    return true;
                });

        WfSpec got = client.getWfSpec(id);
        VariableDef locationDef =
                got.getThreadSpecsMap().get(got.getEntrypointThreadName()).getVariableDefsList().stream()
                        .filter(v -> v.getVarDef().getName().equals("location"))
                        .findFirst()
                        .orElseThrow()
                        .getVarDef();

        assertThat(locationDef.getTypeDef().getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(locationDef.getTypeDef().getInlineStructDef().containsFields("city"))
                .isTrue();
        assertThat(locationDef.getTypeDef().getInlineStructDef().containsFields("zipCode"))
                .isTrue();
    }

    @Test
    void nestedInlineStructFieldWorkflowCompletesAndPreservesValue() {
        verifier.prepareRun(productWf, Arg.of("product-name", "Sprocket"), Arg.of("width", 8), Arg.of("height", 3))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "product", varVal -> {
                    var fields = varVal.getStruct().getStruct().getFieldsMap();
                    assertThat(fields.get("name").getValue().getStr()).isEqualTo("Sprocket");

                    // dimensions is stored as an inline struct — no named StructDefId
                    var dimsStruct = fields.get("dimensions").getValue().getStruct();
                    assertThat(dimsStruct.getStructDefId().getName()).isEmpty();

                    var dimFields = dimsStruct.getStruct().getFieldsMap();
                    assertThat(dimFields.get("width").getValue().getInt()).isEqualTo(8);
                    assertThat(dimFields.get("height").getValue().getInt()).isEqualTo(3);
                })
                .start();
    }

    @LHWorkflow("inline-struct-product-wf")
    public Workflow getProductWf() {
        return new WorkflowImpl("inline-struct-product-wf", wf -> {
            WfRunVariable nameIn = wf.declareStr("product-name").required();
            WfRunVariable widthIn = wf.declareInt("width").required();
            WfRunVariable heightIn = wf.declareInt("height").required();
            WfRunVariable product = wf.declareStruct("product", Product.class);

            product.assign(wf.buildStruct("inline-test-product")
                    .put("name", nameIn)
                    .put(
                            "dimensions",
                            wf.buildInlineStruct().put("width", widthIn).put("height", heightIn)));

            wf.execute("echo-product-name", product.get("name"));
        });
    }

    @LHTaskMethod("echo-product-name")
    public String echoProductName(String name) {
        return name;
    }
}

package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class InlineStructDefTest {

    private LittleHorseBlockingStub client;

    @Test
    void wfSpecVariableDeclaredAsInlineStructSurvivesRoundTrip() {
        String wfName = "inline-struct-rt-" + UUID.randomUUID();
        PutWfSpecRequest compiled = Workflow.newWorkflow(wfName, wf -> wf.declareJsonObj("location"))
                .compileWorkflow();
        InlineStructDef locationType = InlineStructDef.newBuilder()
                .putFields("city", primitiveField(VariableType.STR))
                .putFields("zipCode", primitiveField(VariableType.INT))
                .build();
        var threadSpec = compiled.getThreadSpecsOrThrow(compiled.getEntrypointThreadName()).toBuilder();
        threadSpec
                .getVariableDefsBuilder(0)
                .getVarDefBuilder()
                .clearType()
                .setTypeDef(TypeDefinition.newBuilder().setInlineStructDef(locationType));
        WfSpec stored = client.putWfSpec(compiled.toBuilder()
                .putThreadSpecs(compiled.getEntrypointThreadName(), threadSpec.build())
                .build());

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

    private StructFieldDef primitiveField(VariableType type) {
        return StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(type))
                .build();
    }
}

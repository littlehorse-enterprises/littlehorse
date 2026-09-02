package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class WfSpecStructEvolutionTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldAllowReRegisteringWfSpecAfterFrozenStructDefEvolves() {
        String structDefName = UUID.randomUUID().toString();
        String wfSpecName = "struct-evo-" + UUID.randomUUID();

        putStructDefV0(structDefName);
        waitForStructDef(structDefName, 0);

        // Register a WfSpec exposing the struct as a PUBLIC_VAR; this pins (and freezes) it at v0.
        Workflow original = new WorkflowImpl(wfSpecName, thread -> {
            thread.declareStruct("in", structDefName).asPublic();
        });
        WfSpec originalSpec = client.putWfSpec(original.compileWorkflow());
        assertThat(pinnedStructVersion(originalSpec)).isEqualTo(0);

        // Evolve the StructDef with a superset-compatible change -> v1.
        putStructDefV1(structDefName);
        waitForStructDef(structDefName, 1);

        // Re-registering the same WfSpec should now succeed and re-pin to v1.
        Workflow evolved = new WorkflowImpl(wfSpecName, thread -> {
            thread.declareStruct("in", structDefName).asPublic();
        });
        WfSpec evolvedSpec = client.putWfSpec(evolved.compileWorkflow());
        assertThat(pinnedStructVersion(evolvedSpec)).isEqualTo(1);
    }

    @Test
    void shouldStillRejectChangingFrozenVariableFundamentalType() {
        String structDefName = UUID.randomUUID().toString();
        String wfSpecName = "struct-evo-reject-" + UUID.randomUUID();

        putStructDefV0(structDefName);
        waitForStructDef(structDefName, 0);

        Workflow original = new WorkflowImpl(wfSpecName, thread -> {
            thread.declareStruct("in", structDefName).asPublic();
        });
        client.putWfSpec(original.compileWorkflow());

        // Changing the frozen public variable from a Struct to a primitive must still be rejected.
        Workflow incompatible = new WorkflowImpl(wfSpecName, thread -> {
            thread.addVariable("in", VariableType.STR).asPublic();
        });

        assertThatThrownBy(() -> client.putWfSpec(incompatible.compileWorkflow()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("as it was formerly declared a PUBLIC_VAR");
    }

    private void putStructDefV0(String name) {
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(name)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "x",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                        .build()))
                .build());
    }

    private void putStructDefV1(String name) {
        client.putStructDef(PutStructDefRequest.newBuilder()
                .setName(name)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "x",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                        .build())
                        .putFields(
                                "y",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                        .setDefaultValue(
                                                VariableValue.newBuilder().setInt(0))
                                        .build()))
                .setAllowedUpdates(StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES)
                .build());
    }

    private int pinnedStructVersion(WfSpec spec) {
        return spec.getThreadSpecsMap()
                .get("entrypoint")
                .getVariableDefs(0)
                .getVarDef()
                .getTypeDef()
                .getStructDefId()
                .getVersion();
    }

    private void waitForStructDef(String name, Integer version) {
        StructDefId.Builder id = StructDefId.newBuilder().setName(name);
        if (version != null) id.setVersion(version);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .ignoreExceptionsMatching(LHTestExceptionUtil::isNotFoundException)
                .until(() -> {
                    client.getStructDef(id.build());
                    return true;
                });
    }
}

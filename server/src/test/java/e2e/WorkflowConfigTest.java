package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowConfigTest {
    private LittleHorseBlockingStub client;

    @Test
    void usesConfiguredAdapterWhenWorkflowCompilesBeforeRegistration() {
        UUID defaultValue = UUID.randomUUID();
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new UUIDAdapter()).build();
        Workflow workflow = Workflow.newWorkflow(
                "configured-adapter-" + UUID.randomUUID(),
                thread -> {
                    thread.addVariable("id", defaultValue);
                },
                config);

        // Dependency inspection can compile the workflow before it is registered.
        assertThat(workflow.getRequiredTaskDefNames()).isEmpty();
        WfSpec registered = client.putWfSpec(workflow.compileWorkflow());
        VariableDef id = registered
                .getThreadSpecsOrThrow("entrypoint")
                .getVariableDefs(0)
                .getVarDef();

        assertThat(id.getTypeDef().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(id.getDefaultValue().getStr()).isEqualTo(defaultValue.toString());
    }

    private static class UUIDAdapter implements LHStringAdapter<UUID> {
        @Override
        public String toString(UUID src) {
            return src.toString();
        }

        @Override
        public UUID fromString(String src) {
            return UUID.fromString(src);
        }

        @Override
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }
    }
}

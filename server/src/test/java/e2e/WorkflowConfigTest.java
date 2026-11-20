package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHIntegerAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowConfigTest {
    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    public static class Customer {
        private String name = "Ada";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void discoversArrayAndMapTypesBeforeRegisteringAndRunningWorkflow() {
        Workflow workflow = Workflow.newWorkflow("discovered-containers-" + UUID.randomUUID(), thread -> {
            thread.declareArray("customers", Customer[].class).withDefault(new Customer[][] {{new Customer()}});
            thread.declareMap("by-name", String.class, Customer[].class)
                    .withDefault(Map.of("team", new Customer[] {new Customer()}));
        });

        var discovered = workflow.getReferencedJavaTypes();
        assertThat(discovered).contains(Customer[][].class, Customer[].class, Customer.class, Map.class, String.class);
        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "customers", value -> assertThat(value.getArray()
                                .getItems(0)
                                .getArray()
                                .getItems(0)
                                .getStruct()
                                .getStruct()
                                .getFieldsOrThrow("name")
                                .getValue()
                                .getStr())
                        .isEqualTo("Ada"))
                .thenVerifyVariable(
                        0, "by-name", value -> assertThat(value.getMap().getEntriesCount())
                                .isEqualTo(1))
                .start();
        assertThat(workflow.getReferencedJavaTypes()).isEqualTo(discovered);
    }

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

        assertThat(id.getTypeDef().getPrimitiveType()).isEqualTo(VariableType.INT);
        assertThat(id.getDefaultValue().getInt()).isEqualTo(defaultValue.hashCode());
    }

    private static class UUIDAdapter extends LHIntegerAdapter<UUID> {
        @Override
        public Integer toInteger(UUID src) {
            return src.hashCode();
        }

        @Override
        public UUID fromInteger(Integer src) {
            return new UUID(0, src);
        }

        @Override
        public Class<UUID> getTypeClass() {
            return UUID.class;
        }
    }
}

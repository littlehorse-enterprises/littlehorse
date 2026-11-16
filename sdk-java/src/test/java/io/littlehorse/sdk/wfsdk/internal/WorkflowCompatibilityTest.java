package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHIntegerAdapter;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class WorkflowCompatibilityTest {
    @Test
    void deprecatedFactoriesAndConstructorsStillAcceptConfigAtCompilation() {
        LHConfig config =
                LHConfig.newBuilder().addTypeAdapter(new PriorityAdapter()).build();
        ThreadFunc body = thread -> thread.addVariable("priority", Priority.HIGH);
        for (Workflow workflow : List.of(
                Workflow.newWorkflow("legacy-factory", body),
                Workflow.newWorkflow("legacy-placeholder-factory", body, Map.of()),
                new WorkflowImpl("legacy-constructor", body),
                new WorkflowImpl("legacy-placeholder-constructor", body, Map.of()))) {
            VariableDef variable = workflow.compileWorkflow(config)
                    .getThreadSpecsOrThrow("entrypoint")
                    .getVariableDefs(0)
                    .getVarDef();
            assertThat(variable.getTypeDef().getPrimitiveType()).isEqualTo(VariableType.INT);
            assertThat(variable.getDefaultValue().getInt()).isEqualTo(1);
        }
    }

    @Test
    void deprecatedPlaceholderOverloadsStillResolvePlaceholders() {
        ThreadFunc body = thread -> thread.declareStruct("address", "${company}-address");
        Map<String, String> placeholders = Map.of("company", "acme");
        for (Workflow workflow : List.of(
                Workflow.newWorkflow("legacy-factory", body, placeholders),
                new WorkflowImpl("legacy-constructor", body, placeholders))) {
            VariableDef variable = workflow.compileWorkflow()
                    .getThreadSpecsOrThrow("entrypoint")
                    .getVariableDefs(0)
                    .getVarDef();
            assertThat(variable.getTypeDef().getStructDefId().getName()).isEqualTo("acme-address");
        }
    }

    private enum Priority {
        LOW,
        HIGH
    }

    private static class PriorityAdapter extends LHIntegerAdapter<Priority> {
        @Override
        public Integer toInteger(Priority src) {
            return src.ordinal();
        }

        @Override
        public Priority fromInteger(Integer src) {
            return Priority.values()[src];
        }

        @Override
        public Class<Priority> getTypeClass() {
            return Priority.class;
        }
    }
}

package io.littlehorse.sdk.worker.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.adapter.LHTypeAdapterRegistry;
import org.junit.jupiter.api.Test;

public class VariableMappingTest {

    @Test
    void shouldAssignInlineStructInput() throws Exception {
        TaskDef taskDef = TaskDef.newBuilder()
                .addInputVars(VariableDef.newBuilder()
                        .setName("customer")
                        .setTypeDef(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder().setName("customer"))))
                .build();

        VariableMapping mapping = new VariableMapping(
                taskDef, 0, InlineStruct.class, "customer", "customer", LHTypeAdapterRegistry.empty());

        InlineStruct inlineStruct = InlineStruct.newBuilder()
                .putFields(
                        "id",
                        StructField.newBuilder()
                                .setValue(VariableValue.newBuilder().setStr("abc-123"))
                                .build())
                .build();

        ScheduledTask scheduledTask = ScheduledTask.newBuilder()
                .addVariables(VarNameAndVal.newBuilder()
                        .setVarName("customer")
                        .setValue(VariableValue.newBuilder()
                                .setStruct(Struct.newBuilder()
                                        .setStructDefId(StructDefId.newBuilder().setName("customer"))
                                        .setStruct(inlineStruct))))
                .build();

        Object assigned = mapping.assign(scheduledTask, null);
        assertThat(assigned).isEqualTo(inlineStruct);
    }

    @Test
    void shouldFailInlineStructWhenStructDefNameMismatches() {
        TaskDef taskDef = TaskDef.newBuilder()
                .addInputVars(VariableDef.newBuilder()
                        .setName("customer")
                        .setTypeDef(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder().setName("customer"))))
                .build();

        assertThatThrownBy(() -> new VariableMapping(
                        taskDef,
                        0,
                        InlineStruct.class,
                        "customer",
                        "different-customer",
                        LHTypeAdapterRegistry.empty()))
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("different-customer");
    }
}

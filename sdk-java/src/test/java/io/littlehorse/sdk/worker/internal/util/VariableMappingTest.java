package io.littlehorse.sdk.worker.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.Array;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.testutils.TestReflection;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskParameter;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class VariableMappingTest {

    class Dummy {
        @LHTaskMethod("inline-param")
        public void inlineParam(@LHType(name = "customer", structDefName = "customer") InlineStruct customer) {}

        @LHTaskMethod("inline-param-2")
        public void inlineParam2(
                @LHType(name = "customer", structDefName = "different-customer") InlineStruct customer) {}

        @LHTaskMethod("native-array-param")
        public void nativeArrayParam(@LHType(name = "numbers", isLHArray = true) Long[] numbers) {}

        @LHTaskMethod("native-array-param-string")
        public void nativeArrayParamString(@LHType(name = "numbers", isLHArray = true) String[] numbers) {}
    }

    @Test
    void shouldAssignInlineStructInput() throws Exception {
        TaskDef taskDef = TaskDef.newBuilder()
                .addInputVars(VariableDef.newBuilder()
                        .setName("customer")
                        .setTypeDef(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder().setName("customer"))))
                .build();

        Method method = TestReflection.getTaskMethodByName(Dummy.class, "inline-param");
        Parameter param = TestReflection.getParameter(method, 0);

        LHTaskParameter lhParam = new LHTaskParameter(param, LHTypeAdapterRegistry.empty(), Map.of());
        VariableMapping mapping = new VariableMapping(taskDef.getInputVars(0), lhParam, LHTypeAdapterRegistry.empty());

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

        Object assigned = mapping.assign(scheduledTask);
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

        Method method = TestReflection.getTaskMethodByName(Dummy.class, "inline-param-2");
        Parameter param = TestReflection.getParameter(method, 0);

        LHTaskParameter lhParam = new LHTaskParameter(param, LHTypeAdapterRegistry.empty(), Map.of());

        assertThatThrownBy(() -> new VariableMapping(taskDef.getInputVars(0), lhParam, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("different-customer");
    }

    @Test
    void shouldAssignNativeArrayInput() throws Exception {
        TaskDef taskDef = TaskDef.newBuilder()
                .addInputVars(VariableDef.newBuilder()
                        .setName("numbers")
                        .setTypeDef(TypeDefinition.newBuilder()
                                .setInlineArrayDef(InlineArrayDef.newBuilder()
                                        .setArrayType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))))
                .build();

        Method method = TestReflection.getTaskMethodByName(Dummy.class, "native-array-param");
        Parameter param = TestReflection.getParameter(method, 0);

        LHTaskParameter lhParam = new LHTaskParameter(param, LHTypeAdapterRegistry.empty(), Map.of());
        VariableMapping mapping = new VariableMapping(taskDef.getInputVars(0), lhParam, LHTypeAdapterRegistry.empty());

        ScheduledTask scheduledTask = ScheduledTask.newBuilder()
                .addVariables(VarNameAndVal.newBuilder()
                        .setVarName("numbers")
                        .setValue(VariableValue.newBuilder()
                                .setArray(Array.newBuilder()
                                        .addItems(VariableValue.newBuilder().setInt(1L))
                                        .addItems(VariableValue.newBuilder().setInt(2L))
                                        .addItems(VariableValue.newBuilder().setInt(3L)))))
                .build();

        Object assigned = mapping.assign(scheduledTask);
        assertThat((Long[]) assigned).containsExactly(1L, 2L, 3L);
    }

    @Test
    void shouldFailInlineArrayWhenElementTypeMismatches() {
        TaskDef taskDef = TaskDef.newBuilder()
                .addInputVars(VariableDef.newBuilder()
                        .setName("numbers")
                        .setTypeDef(TypeDefinition.newBuilder()
                                .setInlineArrayDef(InlineArrayDef.newBuilder()
                                        .setArrayType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))))
                .build();

        Method method = TestReflection.getTaskMethodByName(Dummy.class, "native-array-param-string");
        Parameter param = TestReflection.getParameter(method, 0);

        LHTaskParameter lhParam = new LHTaskParameter(param, LHTypeAdapterRegistry.empty(), Map.of());

        assertThatThrownBy(() -> new VariableMapping(taskDef.getInputVars(0), lhParam, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("Array<INT>")
                .hasMessageContaining("Array<STR>");
    }
}

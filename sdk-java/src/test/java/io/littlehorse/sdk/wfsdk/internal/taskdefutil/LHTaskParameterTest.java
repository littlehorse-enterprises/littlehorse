package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.testutils.TestReflection;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LHTaskParameterTest {
    public class ParameterTestTasks {
        @LHTaskMethod("test-primitive")
        public void primitiveParamTask(@LHType(name = "param1") String param1) {}

        @LHTaskMethod("test-json-arr")
        public void jsonParamTask(@LHType(name = "param1") String[] param1) {}

        @LHTaskMethod("test-native-arr")
        public void nativeArrayParamTask(@LHType(name = "param1", isLHArray = true) String[] param1) {}

        @LHTaskMethod("test-type-adapted-method")
        public void typeAdaptedParamTask(@LHType(name = "param1") UUID param1) {}

        @LHTaskMethod("test-masked-param")
        public void maskedParamTask(@LHType(name = "param1", masked = true) int param1) {}

        @LHTaskMethod("test-inline-struct-param")
        public void inlineStructTask(@LHType(name = "param1", structDefName = "customer") InlineStruct param1) {}

        @LHTaskMethod("inline-struct-placeholder-task")
        public void inlineStructPlaceholderTask(
                @LHType(name = "param1", structDefName = "${inputStruct}") InlineStruct customer) {}

        @LHTaskMethod("inline-struct-invalid-task")
        public void inlineStructInvalidTask(InlineStruct customer) {}
        ;

        @LHTaskMethod("non-inline-structdef-invalid-task")
        public void nonInlineStructDefInvalidTask(@LHType(structDefName = "customer") String customer) {}

        @LHTaskMethod("non-array-lh-array-invalid-task")
        public void nonArrayLhArrayInvalidTask(@LHType(isLHArray = true) String customer) {}
    }

    @Test
    public void shouldHandlePrimitiveTaskParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-primitive");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.STR)
                        .build())
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    public void shouldHandleJsonArrayTaskParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-json-arr");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.JSON_ARR)
                        .build())
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    public void shouldHandleNativeArrayTaskParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-native-arr");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build()))
                        .build())
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    public void shouldHandleTypeAdaptedLHParameterType() {
        LHTypeAdapterRegistry typeAdapterRegistry =
                LHTypeAdapterRegistry.from(Map.of(UUID.class, new LHStringAdapter<UUID>() {
                    @Override
                    public Class<UUID> getTypeClass() {
                        return UUID.class;
                    }

                    @Override
                    public String toString(UUID src) {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'toString'");
                    }

                    @Override
                    public UUID fromString(String src) {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'fromString'");
                    }
                }));

        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-type-adapted-method");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, typeAdapterRegistry, Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.STR)
                        .build())
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    public void shouldHandleMaskedParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-masked-param");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        assertThat(taskParameter.getVariableDef().getTypeDef().getMasked()).isTrue();
    }

    @Test
    public void shouldInferInlineStructParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "test-inline-struct-param");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName("customer").setVersion(-1))
                        .build())
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    void shouldResolvePlaceholdersForInlineStructTypes() {
        Map<String, String> placeholderValues = Map.of("inputStruct", "customer-request");

        Method taskMethod =
                TestReflection.getTaskMethodByName(ParameterTestTasks.class, "inline-struct-placeholder-task");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter =
                new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), placeholderValues);

        TypeDefinition inputTypeDef = taskParameter.getVariableDef().getTypeDef();

        assertThat(inputTypeDef.getStructDefId().getName()).isEqualTo("customer-request");
    }

    @Test
    void shouldFailWhenInlineStructTypeIsMissingStructDefName() {
        Method taskMethod = TestReflection.getTaskMethodByName(ParameterTestTasks.class, "inline-struct-invalid-task");
        Parameter parameter = taskMethod.getParameters()[0];

        assertThatThrownBy(() -> {
                    new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("InlineStruct parameters must declare @LHType(structDefName = \"...\")");
    }

    @Test
    void shouldFailWhenStructDefNameIsUsedOnNonInlineStructParameter() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ParameterTestTasks.class, "non-inline-structdef-invalid-task");
        Parameter parameter = taskMethod.getParameters()[0];

        assertThatThrownBy(() -> {
                    new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("@LHType(structDefName = ...) can only be used on InlineStruct");
    }

    @Test
    void shouldFailWhenIsLHArrayUsedOnNonArrayParameter() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ParameterTestTasks.class, "non-array-lh-array-invalid-task");
        Parameter parameter = taskMethod.getParameters()[0];

        assertThatThrownBy(() -> {
                    new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("@LHType(isLHArray = true) can only be used on array parameters");
    }
}

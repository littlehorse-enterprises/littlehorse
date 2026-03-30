package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.testutils.TestReflection;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.Library;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LHTaskParameterTest {
    public class TestClass {
        @LHTaskMethod("test-primitive")
        public void testPrimitiveParam(@LHType(name = "param1") String param1) {}

        @LHTaskMethod("test-json-arr")
        public void testJsonArrParam(@LHType(name = "param1") String[] param1) {}

        @LHTaskMethod("test-lh-array")
        public void testLHArrayParam(@LHType(name = "param1", isLHArray = true) String[] param1) {}

        @LHTaskMethod("test-struct-array")
        public void testStructArrayParam(@LHType(name = "param1", isLHArray = true) Library[] param1) {}

        @LHTaskMethod("test-type-adapted-method")
        public void testTypeAdaptedMethod(@LHType(name = "param1") UUID param1) {}

        @LHTaskMethod("test-masked-param")
        public void testMaskedParam(@LHType(name = "param1", masked = true) int param1) {}
    }

    @Test
    public void shouldHandlePrimitiveTaskParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-primitive");
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
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-json-arr");
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
    public void shouldHandleLHArrayAnnotationOnPrimitiveArrayType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-lh-array");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build())
                                .build()))
                .build();

        assertThat(actualVariableDef).isEqualTo(expectedVariableDef);
    }

    @Test
    public void shouldHandleStructArrayType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-struct-array");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        VariableDef actualVariableDef = taskParameter.getVariableDef();
        VariableDef expectedVariableDef = VariableDef.newBuilder()
                .setName("param1")
                .setTypeDef(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setStructDefId(StructDefId.newBuilder()
                                                .setName("library")
                                                .build())
                                        .build())
                                .build()))
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

        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-type-adapted-method");
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
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-masked-param");
        Parameter parameter = taskMethod.getParameters()[0];
        LHTaskParameter taskParameter = new LHTaskParameter(parameter, LHTypeAdapterRegistry.empty(), Map.of());

        assertThat(taskParameter.getVariableDef().getTypeDef().getMasked()).isTrue();
    }
}

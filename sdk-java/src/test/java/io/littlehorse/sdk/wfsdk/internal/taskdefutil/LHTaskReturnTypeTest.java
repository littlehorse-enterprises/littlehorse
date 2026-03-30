package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.testutils.TestReflection;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.Library;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LHTaskReturnTypeTest {
    public class TestClass {
        @LHTaskMethod("test-void")
        public void testVoidReturnType() {}

        @LHTaskMethod("test-primitive")
        public String testPrimitiveReturnType() {
            return "hello world";
        }

        @LHTaskMethod("test-json-arr")
        public String[] testJsonArrReturnType() {
            return new String[] {"hello", "world"};
        }

        @LHTaskMethod(value = "test-lh-array")
        @LHType(isLHArray = true)
        public String[] testLHArrayReturnType() {
            return new String[] {"hello", "world"};
        }

        @LHTaskMethod("test-struct-array")
        @LHType(isLHArray = true)
        public Library[] testStructArrayReturnType() {
            return new Library[] {new Library()};
        }

        @LHTaskMethod("test-type-adapted-method")
        public UUID testTypeAdaptedReturnType() {
            return UUID.randomUUID();
        }

        @LHTaskMethod("test-masked-param")
        @LHType(masked = true)
        public int testMaskedReturnType() {
            return 42;
        }
    }

    @Test
    public void shouldHandleVoidTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-void");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder().build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandlePrimitiveTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-primitive");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.STR)
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleJsonArrayTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-json-arr");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.JSON_ARR)
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleLHArrayTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-lh-array");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setInlineArrayDef(io.littlehorse.sdk.common.proto.InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleStructArrayTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-struct-array");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setStructDefId(StructDefId.newBuilder()
                                                .setName("library")
                                                .build()))))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleTypeAdaptedTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-type-adapted-method");
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

        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, typeAdapterRegistry, Map.of());
        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.STR)
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleMaskedTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(TestClass.class, "test-masked-param");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();

        assertThat(actualReturnType.getReturnType().getMasked()).isTrue();
    }
}

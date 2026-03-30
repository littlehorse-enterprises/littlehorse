package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.testutils.TestReflection;
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

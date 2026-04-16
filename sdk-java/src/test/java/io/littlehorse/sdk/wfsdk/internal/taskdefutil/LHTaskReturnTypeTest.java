package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
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
    public class ReturnTypeTestTasks {
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

        @LHTaskMethod("test-native-arr")
        @LHType(isLHArray = true)
        public String[] testNativeArrReturnType() {
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

        @LHTaskMethod("test-inline-struct-param")
        @LHType(name = "param1", structDefName = "customer")
        public InlineStruct inlineStructTask() {
            return null;
        }

        @LHTaskMethod("inline-struct-placeholder-task")
        @LHType(structDefName = "${outputStruct}")
        public InlineStruct inlineStructPlaceholderTask() {
            return null;
        }

        @LHTaskMethod("inline-struct-invalid-task")
        public InlineStruct inlineStructInvalidTask() {
            return null;
        }

        @LHTaskMethod("non-inline-structdef-invalid-task")
        @LHType(structDefName = "customer")
        public String nonInlineStructDefInvalidTask() {
            return "hello world";
        }

        @LHTaskMethod("non-array-lh-array-invalid-task")
        @LHType(isLHArray = true)
        public String nonArrayLhArrayInvalidTask() {
            return "hello world";
        }
    }

    @Test
    public void shouldHandleVoidTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-void");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder().build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandlePrimitiveTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-primitive");
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
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-json-arr");
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
    public void shouldHandleNativeArrayTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-native-arr");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build()))
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    public void shouldHandleTypeAdaptedTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-type-adapted-method");
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
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-masked-param");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();

        assertThat(actualReturnType.getReturnType().getMasked()).isTrue();
    }

    @Test
    public void shouldInferInlineStructParameter() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-inline-struct-param");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setStructDefId(
                                StructDefId.newBuilder().setName("customer").setVersion(-1))
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldResolvePlaceholdersForInlineStructTypes() {
        Map<String, String> placeholderValues = Map.of("outputStruct", "customer");

        Method taskMethod =
                TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "inline-struct-placeholder-task");
        LHTaskReturnType taskReturnType =
                new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), placeholderValues);

        TypeDefinition returnType = taskReturnType.getReturnType().getReturnType();

        assertThat(returnType.getStructDefId().getName()).isEqualTo("customer");
    }

    @Test
    void shouldFailWhenInlineStructTypeIsMissingStructDefName() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "inline-struct-invalid-task");

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("Methods that return InlineStruct must declare @LHType(structDefName = \"...\")");
    }

    @Test
    void shouldFailWhenStructDefNameIsUsedOnNonInlineStructReturnType() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "non-inline-structdef-invalid-task");

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("@LHType(structDefName = ...) can only be used on InlineStruct");
    }

    @Test
    void shouldFailWhenIsLHArrayUsedOnNonArrayReturnType() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "non-array-lh-array-invalid-task");

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("@LHType(isLHArray = true) can only be used on array return types");
    }
}

package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHJsonObjAdapter;
import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineMapDef;
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

    static class UnannotatedArrayElement {}

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

        @LHTaskMethod("invalid-native-arr-pojo")
        @LHType(isLHArray = true)
        public UnannotatedArrayElement[] invalidNativeArrayPojoReturnType() {
            return null;
        }

        @LHTaskMethod("invalid-native-arr-adapter-json-obj")
        @LHType(isLHArray = true)
        public UUID[] invalidNativeArrayAdapterJsonObjReturnType() {
            return null;
        }

        @LHTaskMethod("test-native-map")
        @LHType(isLHMap = true)
        public Map<String, Long> testNativeMapReturnType() {
            return Map.of("a", 1L);
        }

        @LHTaskMethod("non-map-lh-map-invalid-task")
        @LHType(isLHMap = true)
        public String nonMapLhMapInvalidTask() {
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

    @Test
    void shouldFailWhenNativeArrayReturnElementResolvesToJsonObj() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "invalid-native-arr-pojo");

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden JSON type: JSON_OBJ");
    }

    @Test
    void shouldFailWhenNativeArrayReturnElementAdapterResolvesToJsonObj() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "invalid-native-arr-adapter-json-obj");

        LHTypeAdapterRegistry typeAdapterRegistry =
                LHTypeAdapterRegistry.from(Map.of(UUID.class, new LHJsonObjAdapter<UUID>() {
                    @Override
                    public String toJsonObj(UUID src) {
                        return "{}";
                    }

                    @Override
                    public UUID fromJsonObj(String src) {
                        return UUID.randomUUID();
                    }

                    @Override
                    public Class<UUID> getTypeClass() {
                        return UUID.class;
                    }
                }));

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, typeAdapterRegistry, Map.of());
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden JSON type: JSON_OBJ");
    }

    @Test
    public void shouldHandleNativeMapTaskReturnType() {
        Method taskMethod = TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "test-native-map");
        LHTaskReturnType taskReturnType = new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());

        ReturnType actualReturnType = taskReturnType.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setInlineMapDef(InlineMapDef.newBuilder()
                                .setKeyType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build())
                                .setValueType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.INT)
                                        .build()))
                        .build())
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldFailWhenIsLHMapUsedOnNonMapReturnType() {
        Method taskMethod =
                TestReflection.getTaskMethodByName(ReturnTypeTestTasks.class, "non-map-lh-map-invalid-task");

        assertThatThrownBy(() -> {
                    new LHTaskReturnType(taskMethod, LHTypeAdapterRegistry.empty(), Map.of());
                })
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("@LHType(isLHMap = true) can only be used on Map return types");
    }
}

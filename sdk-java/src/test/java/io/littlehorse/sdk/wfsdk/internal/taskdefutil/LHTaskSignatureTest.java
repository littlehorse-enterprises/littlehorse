package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.TaskSchemaMismatchError;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.testutils.TestReflection;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.WorkerContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHTaskSignatureTest {

    static class MyWorker {
        @LHTaskMethod("primitive-task")
        public String primitiveTask(int myVar) {
            return "hello";
        }

        @LHTaskMethod("worker-context-task")
        public void workerContextTask(WorkerContext workerContext) {}

        @LHTaskMethod("struct-task")
        public Garage structTask(Car car) {
            return new Garage();
        }

        @LHTaskMethod("masked-struct-task")
        public @LHType(masked = true) Car maskedStructTask(@LHType(masked = true) Car car) {
            return null;
        }

        @LHTaskMethod("person-task")
        public void personTask(Person person) {}

        @LHTaskMethod(value = "description-task", description = "description-test")
        public void descriptionTask() {}

        @LHTaskMethod("adapter-task")
        public UUID adapterTask(UUID in) {
            return in;
        }

        @LHTaskMethod("adapter-struct-task")
        public UuidHolder adapterStructTask(UuidHolder in) {
            return in;
        }

        @LHTaskMethod("inline-struct-task")
        @LHType(structDefName = "customer")
        public InlineStruct inlineStructTask(@LHType(structDefName = "customer") InlineStruct customer) {
            return customer;
        }

        @LHTaskMethod("inline-struct-placeholder-task-${model}")
        @LHType(structDefName = "${outputStruct}")
        public InlineStruct inlineStructPlaceholderTask(
                @LHType(structDefName = "${inputStruct}") InlineStruct customer) {
            return customer;
        }

        @LHTaskMethod("inline-struct-invalid-task")
        public InlineStruct inlineStructInvalidTask(InlineStruct customer) {
            return customer;
        }
    }

    @LHStructDef("car")
    @Getter
    static class Car {
        String model;
        int year;
        boolean isElectric;
        Person owner;
        Garage garage;
    }

    @LHStructDef("person")
    @Getter
    static class Person {
        String name;
        int age = 10;
    }

    @LHStructDef("garage")
    @Getter
    static class Garage {
        String address;
        int size;
        Person owner;
    }

    @LHStructDef("uuid-holder")
    @Getter
    static class UuidHolder {
        UUID id;

        public UUID getId() {
            return id;
        }
    }

    private static LHTaskSignature signatureFor(
            String taskName,
            LHTypeAdapterRegistry typeAdapterRegistry,
            Map<String, String> placeholders,
            Class<?>... paramTypes) {
        Method method = TestReflection.getTaskMethodByName(MyWorker.class, taskName);
        return new LHTaskSignature(method, typeAdapterRegistry, placeholders);
    }

    private static LHTaskSignature signatureFor(String taskName, Class<?>... paramTypes) {
        return signatureFor(taskName, LHTypeAdapterRegistry.empty(), Map.of(), paramTypes);
    }

    @Test
    void shouldGetDescriptionFromAnnotation() {
        LHTaskSignature taskSignature = signatureFor("description-task");

        assertThat(taskSignature.getTaskDefDescription()).contains("description-test");
    }

    @Test
    void shouldInferPrimitiveParameterType() {
        LHTaskSignature taskSignature = signatureFor("primitive-task", int.class);

        TypeDefinition actualTypeDefinition =
                taskSignature.getVariableDefs().get(0).getVariableDef().getTypeDef();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    void shouldInferPrimitiveReturnType() {
        LHTaskSignature taskSignature = signatureFor("primitive-task", int.class);

        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldIgnoreWorkerContextInTaskDefParameter() {
        LHTaskSignature taskSignature = signatureFor("worker-context-task", WorkerContext.class);

        assertThat(taskSignature.getVariableDefs()).isEmpty();
        assertThat(taskSignature.hasWorkerContext()).isTrue();
    }

    @Test
    void shouldInferVoidReturnType() {
        LHTaskSignature taskSignature = signatureFor("worker-context-task", WorkerContext.class);
        ReturnType actualReturnType = taskSignature.getReturnType();

        assertThat(actualReturnType).isEqualTo(ReturnType.newBuilder().build());
    }

    @Test
    void shouldInferStructReturnType() {
        LHTaskSignature taskSignature = signatureFor("struct-task", Car.class);
        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName("garage")))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldIgnoreMaskedReturnAnnotationAfterRefactor() {
        LHTaskSignature taskSignature = signatureFor("masked-struct-task", Car.class);

        assertThat(taskSignature.getReturnType().getReturnType().getMasked()).isFalse();
    }

    @Test
    void shouldInferParameterTypeWithMasked() {
        LHTaskSignature taskSignature = signatureFor("masked-struct-task", Car.class);

        boolean actualMaskedValue = taskSignature
                .getVariableDefs()
                .get(0)
                .getVariableDef()
                .getTypeDef()
                .getMasked();

        assertThat(actualMaskedValue).isTrue();
    }

    @Test
    void shouldReturnNoStructDefDependenciesAfterRefactor() {
        LHTaskSignature taskSignature = signatureFor("struct-task", Car.class);

        assertThat(taskSignature.getStructDefDependencies()).isEmpty();
    }

    @Test
    void shouldUseTypeAdapterForParameterAndReturnType() {
        LHStringAdapter<UUID> uuidAdapter = new LHStringAdapter<UUID>() {
            @Override
            public String toString(UUID src) {
                return src.toString();
            }

            @Override
            public UUID fromString(String src) {
                return UUID.fromString(src);
            }

            @Override
            public Class<UUID> getTypeClass() {
                return UUID.class;
            }
        };

        LHTaskSignature taskSignature = signatureFor(
                "adapter-task", LHTypeAdapterRegistry.from(Map.of(UUID.class, uuidAdapter)), Map.of(), UUID.class);

        TypeDefinition inputTypeDef =
                taskSignature.getVariableDefs().get(0).getVariableDef().getTypeDef();
        TypeDefinition returnTypeDef = taskSignature.getReturnType().getReturnType();

        assertThat(inputTypeDef.getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(returnTypeDef.getPrimitiveType()).isEqualTo(VariableType.STR);
    }

    @Test
    void shouldUseTypeAdapterForStructDefFieldTypes() {
        LHStringAdapter<UUID> uuidAdapter = new LHStringAdapter<UUID>() {
            @Override
            public String toString(UUID src) {
                return src.toString();
            }

            @Override
            public UUID fromString(String src) {
                return UUID.fromString(src);
            }

            @Override
            public Class<UUID> getTypeClass() {
                return UUID.class;
            }
        };

        StructDef structDef = new LHStructDefType(
                        UuidHolder.class, LHTypeAdapterRegistry.from(Map.of(UUID.class, uuidAdapter)))
                .toStructDef();

        assertThat(structDef.getStructDef().getFieldsCount()).isEqualTo(1);
        assertThat(structDef
                        .getStructDef()
                        .getFieldsMap()
                        .values()
                        .iterator()
                        .next()
                        .getFieldType()
                        .getPrimitiveType())
                .isEqualTo(VariableType.STR);
    }

    @Test
    void shouldInferInlineStructParameterAndReturnTypeFromAnnotation() {
        LHTaskSignature taskSignature = signatureFor("inline-struct-task", InlineStruct.class);

        TypeDefinition inputTypeDef =
                taskSignature.getVariableDefs().get(0).getVariableDef().getTypeDef();
        TypeDefinition returnTypeDef = taskSignature.getReturnType().getReturnType();

        assertThat(inputTypeDef.getStructDefId().getName()).isEqualTo("customer");
        assertThat(returnTypeDef.getStructDefId().getName()).isEqualTo("customer");
    }

    @Test
    void shouldResolvePlaceholdersForInlineStructTypes() {
        LHTaskSignature taskSignature = signatureFor(
                "inline-struct-placeholder-task-${model}",
                LHTypeAdapterRegistry.empty(),
                Map.of("model", "acme", "inputStruct", "customer-request", "outputStruct", "customer"),
                InlineStruct.class);

        TypeDefinition inputTypeDef =
                taskSignature.getVariableDefs().get(0).getVariableDef().getTypeDef();
        TypeDefinition returnTypeDef = taskSignature.getReturnType().getReturnType();

        assertThat(taskSignature.getTaskDefName()).isEqualTo("inline-struct-placeholder-task-acme");
        assertThat(inputTypeDef.getStructDefId().getName()).isEqualTo("customer-request");
        assertThat(returnTypeDef.getStructDefId().getName()).isEqualTo("customer");
    }

    @Test
    void shouldFailWhenInlineStructTypeIsMissingStructDefName() {
        Method method = TestReflection.getTaskMethodByName(MyWorker.class, "inline-struct-invalid-task");

        assertThatThrownBy(() -> new LHTaskSignature(method, LHTypeAdapterRegistry.empty(), Map.of()))
                .isInstanceOf(TaskSchemaMismatchError.class)
                .hasMessageContaining("InlineStruct parameters must declare @LHType(structDefName = \\\"...\\\")");
    }
}

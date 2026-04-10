package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.StructDef;
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

        @LHTaskMethod("worker-context-task")
        public void workerContextTask(WorkerContext workerContext) {}

        @LHTaskMethod("masked-struct-task")
        public @LHType(masked = true) Car maskedStructTask(@LHType(masked = true) Car car) {
            return null;
        }

        @LHTaskMethod("person-task")
        public void personTask(Person person) {}

        @LHTaskMethod(value = "description-task", description = "description-test")
        public void descriptionTask() {}

        @LHTaskMethod("adapter-struct-task")
        public UuidHolder adapterStructTask(UuidHolder in) {
            return in;
        }

        @LHTaskMethod(value = "blank-desc-task", description = "")
        public void blankDescTask() {};
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
    void shouldIgnoreWorkerContextInTaskDefParameter() {
        LHTaskSignature taskSignature = signatureFor("worker-context-task", WorkerContext.class);

        assertThat(taskSignature.getVariableDefs()).isEmpty();
        assertThat(taskSignature.hasWorkerContext()).isTrue();
    }

    @Test
    void shouldReturnNoStructDefDependenciesAfterRefactor() {
        LHTaskSignature taskSignature = signatureFor("struct-task", Car.class);

        assertThat(taskSignature.getStructDefDependencies()).isEmpty();
    }

    @Test
    void shouldIgnoreEmptyTaskDefDescription() {
        LHTaskSignature taskSignature = signatureFor("blank-desc-task");

        assertThat(taskSignature.toPutTaskDefRequest().hasDescription()).isFalse();
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
}

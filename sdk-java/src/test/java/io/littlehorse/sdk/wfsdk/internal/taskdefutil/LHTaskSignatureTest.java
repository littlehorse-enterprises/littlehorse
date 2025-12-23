package io.littlehorse.sdk.wfsdk.internal.taskdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.sdk.worker.WorkerContext;
import java.util.List;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHTaskSignatureTest {
    class MyWorker {
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
    }

    @LHStructDef("car")
    @Getter
    class Car {
        String model;
        int year;
        boolean isElectric;
        Person owner;
        Garage garage;
    }

    @LHStructDef("person")
    @Getter
    class Person {
        String name;
        int age = 10;
    }

    @LHStructDef("garage")
    @Getter
    class Garage {
        String address;
        int size;
        Person owner;
    }

    @Test
    void shouldGetDescriptionFromAnnotation() {
        LHTaskSignature taskSignature = new LHTaskSignature("description-task", new MyWorker(), "description-task");
        String actualDescription = taskSignature.getTaskDefDescription();
        String expectedDescription = "description-test";

        assertThat(actualDescription).isEqualTo(expectedDescription);
    }

    @Test
    void shouldInferPrimitiveParameterType() {
        LHTaskSignature taskSignature = new LHTaskSignature("primitive-task", new MyWorker(), "primitive-task");

        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();
        VariableDef firstParamVariableDef = actualVariableDefs.get(0);
        TypeDefinition actualTypeDefinition = firstParamVariableDef.getTypeDef();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    void shouldInferPrimitiveReturnType() {
        LHTaskSignature taskSignature = new LHTaskSignature("primitive-task", new MyWorker(), "primitive-task");

        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldIgnoreWorkerContextInTaskDefParameter() {
        LHTaskSignature taskSignature =
                new LHTaskSignature("worker-context-task", new MyWorker(), "worker-context-task");
        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();

        assertThat(actualVariableDefs.size()).isEqualTo(0);
    }

    @Test
    void shouldInferVoidReturnType() {
        LHTaskSignature taskSignature =
                new LHTaskSignature("worker-context-task", new MyWorker(), "worker-context-task");
        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder().build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldInferStructReturnType() {
        LHTaskSignature taskSignature = new LHTaskSignature("struct-task", new MyWorker(), "struct-task");
        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName("garage")))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldInferReturnTypeWithMasked() {
        LHTaskSignature taskSignature = new LHTaskSignature("masked-struct-task", new MyWorker(), "masked-struct-task");
        ReturnType actualReturnType = taskSignature.getReturnType();
        TypeDefinition actualTypeDefinition = actualReturnType.getReturnType();
        boolean actualReturnTypeMaskedValue = actualTypeDefinition.getMasked();
        boolean expectedReturnTypeMaskedValue = true;

        assertThat(actualReturnTypeMaskedValue).isEqualTo(expectedReturnTypeMaskedValue);
    }

    @Test
    void shouldInferParameterTypeWithMasked() {
        LHTaskSignature taskSignature = new LHTaskSignature("masked-struct-task", new MyWorker(), "masked-struct-task");

        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();
        VariableDef firstParamVariableDef = actualVariableDefs.get(0);
        TypeDefinition actualTypeDefinition = firstParamVariableDef.getTypeDef();

        boolean actualMaskedValue = actualTypeDefinition.getMasked();
        boolean expectedMaskedValue = true;

        assertThat(actualMaskedValue).isEqualTo(expectedMaskedValue);
    }

    @Test
    void shouldInferStructDefParameterTypeWithMasked() {
        LHTaskSignature taskSignature = new LHTaskSignature("masked-struct-task", new MyWorker(), "masked-struct-task");

        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();
        VariableDef firstParamVariableDef = actualVariableDefs.get(0);
        TypeDefinition actualTypeDefinition = firstParamVariableDef.getTypeDef();

        boolean actualMaskedValue = actualTypeDefinition.getMasked();
        boolean expectedMaskedValue = true;

        assertThat(actualMaskedValue).isEqualTo(expectedMaskedValue);
    }

    @Test
    void shouldReturnSortedListOfParamAndReturnTypeStructDefDependencies() {
        LHTaskSignature taskSignature = new LHTaskSignature("struct-task", new MyWorker(), "struct-task");
        List<LHStructDefType> actualClassList = taskSignature.getStructDefDependencies();
        List<LHStructDefType> expectedClassList = List.of(
                new LHStructDefType(Person.class), new LHStructDefType(Garage.class), new LHStructDefType(Car.class));

        assertThat(actualClassList).isEqualTo(expectedClassList);
    }
}

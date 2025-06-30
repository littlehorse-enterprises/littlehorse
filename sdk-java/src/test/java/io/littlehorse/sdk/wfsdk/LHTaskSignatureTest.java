package io.littlehorse.sdk.wfsdk;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LHTaskSignatureTest {
    class MyWorker {
        @LHTaskMethod("greet")
        public String greet(int myVar) {
            return "hello";
        }

        @LHTaskMethod("complete-order")
        public void completeOrder(WorkerContext workerContext) {}

        @LHTaskMethod("get-car-owner")
        public Garage getCarGarage(Car car) {
            return new Garage();
        }
    }

    @LHStructDef(name = "car")
    class Car {
        String model;
        int year;
        boolean isElectric;
        Person owner;
        Garage garage;
    }

    @LHStructDef(name = "person")
    class Person {
        String name;
        int age;
    }

    @LHStructDef(name = "garage")
    class Garage {
        String address;
        int size;
        Person owner;
    }

    @Test
    void shouldInferPrimitiveTaskDefReturnType() {
        LHTaskSignature taskSignature = new LHTaskSignature("greet", new MyWorker(), "greet");

        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldInferVoidReturnType() {
        LHTaskSignature taskSignature = new LHTaskSignature("complete-order", new MyWorker(), "complete-order");
        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder().build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }


    @Test
    void shouldInferStructReturnType() {
        LHTaskSignature taskSignature = new LHTaskSignature("get-car-owner", new MyWorker(), "get-car-owner");
        ReturnType actualReturnType = taskSignature.getReturnType();
        ReturnType expectedReturnType = ReturnType.newBuilder()
                .setReturnType(TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName("garage")))
                .build();

        assertThat(actualReturnType).isEqualTo(expectedReturnType);
    }

    @Test
    void shouldInferPrimitiveParameterType() {
        LHTaskSignature taskSignature = new LHTaskSignature("greet", new MyWorker(), "greet");

        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();
        VariableDef firstParamVariableDef = actualVariableDefs.getFirst();
        TypeDefinition actualTypeDefinition = firstParamVariableDef.getTypeDef();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    void shouldInferStructDefParameterType() {
        LHTaskSignature taskSignature = new LHTaskSignature("get-car-owner", new MyWorker(), "get-car-owner");

        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();
        VariableDef firstParamVariableDef = actualVariableDefs.getFirst();
        TypeDefinition actualTypeDefinition = firstParamVariableDef.getTypeDef();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName("car")).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    void shouldIgnoreWorkerContextInTaskDefParameter() {
        LHTaskSignature taskSignature = new LHTaskSignature("complete-order", new MyWorker(), "complete-order");
        List<VariableDef> actualVariableDefs = taskSignature.getVariableDefs();

        assertThat(actualVariableDefs.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnSortedListOfParamAndReturnTypeStructDefDependencies() {
        LHTaskSignature taskSignature = new LHTaskSignature("get-car-owner", new MyWorker(), "get-car-owner");
        List<Class<?>> actualClassList = new ArrayList<>(taskSignature.getStructDefDependencies());
        List<Class<?>> expectedClassList = List.of(Person.class, Garage.class, Car.class);

        assertThat(actualClassList).isEqualTo(expectedClassList);
    }
}

package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.StructDefUtil;
import io.littlehorse.sdk.worker.LHStructDef;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class StructDefUtilTest {

    @Nested
    class NoCycleTest {
        @Test
        public void shouldCompleteWithoutExceptionWhenNoCircularDependencies() {
            StructDefUtil.getStructDefDependencies(Car.class);
        }

        @Test
        public void shouldReturnTopologicallySortedListOfDependencies() {
            List<Class<?>> classList = StructDefUtil.getStructDefDependencies(Car.class);
            List<Class<?>> expectedClassList = List.of(CarID.class, Garage.class, Person.class, Car.class);

            assertThat(expectedClassList).isEqualTo(classList);
        }

        @LHStructDef(name = "car")
        class Car {
            public Garage garage;
            public CarID id;
            public Person owner;
            public Person passenger;
        }

        @LHStructDef(name = "person")
        class Person {
            public Garage garage;
        }

        @LHStructDef(name = "garage")
        class Garage {
            public CarID[] carIDs;
        }

        @LHStructDef(name = "carId")
        class CarID {
            public String uuid;
        }
    }

    @Nested
    class CycleTest {
        @Test
        public void shouldThrowExceptionWhenCircularDependencies() {
            assertThatThrownBy(() -> {
                        StructDefUtil.getStructDefDependencies(Car.class);
                    })
                    .isInstanceOf(StructDefCircularDependencyException.class);
        }

        @LHStructDef(name = "car")
        class Car {
            public Person owner;
            public Person passenger;
        }

        @LHStructDef(name = "person")
        class Person {
            public Garage garage;
        }

        @LHStructDef(name = "garage")
        class Garage {
            public Car[] cars;
        }
    }
}

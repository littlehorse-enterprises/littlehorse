package io.littlehorse.sdk.wfsdk.internal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.littlehorse.sdk.wfsdk.internal.structdefutil.CircularDependencyException;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.StructDefUtil;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StructDefUtilTest {

  @Nested
  class NoCycleTest {
    @Test
    public void shouldCompleteWithoutExceptionWhenNoCircularDependencies() {
        StructDefUtil.dfsFindCycle(Car.class);
    }

    public class Car {
      public CarID id;
      public Person owner;
      public Person passenger;
    }

    public class Person {
      public Garage garage;
    }

    public class Garage {
      public CarID[] carIDs;
    }

    public class CarID {
      public String uuid;
    }
  }

  @Nested
  class CycleTest {
    @Test
    public void shouldThrowExceptionWhenCircularDependencies() {
      assertThatThrownBy(() -> {
        StructDefUtil.dfsFindCycle(Car.class);
      }).isInstanceOf(CircularDependencyException.class);
    }

    public class Car {
      public Person owner;
      public Person passenger;
    }

    public class Person {
      public Garage garage;
    }

    public class Garage {
      public Car[] cars;
    }
  }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef(name="car")
@Setter
@Getter
public class Car {
  private String brand;
  private String model;

  public Car() {}

  public Car(String brand, String model) {
    this.brand = brand;
    this.model = model;
  }
}

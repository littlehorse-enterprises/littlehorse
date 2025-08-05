package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "car")
public class Car {
  public Person person;

  public Person getPerson() {
    return new Person();
  }
}

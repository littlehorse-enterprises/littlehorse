package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef(name = "person")
public class Person {
    private String name;
    private Car car;

    public Person() {}

    public Person(String name, Car car) {
        this.name = name;
        this.car = car;
    }
}

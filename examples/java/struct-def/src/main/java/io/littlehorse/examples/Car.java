package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "car")
public class Car {
    public String name;
    public String model;
    public Person owner;

    public Car() {}
    ;

    public Car(String name, String model, Person owner) {
        this.name = name;
        this.model = model;
        this.owner = owner;
    }

    public String getName() {
        return this.name;
    }

    public String getModel() {
        return this.model;
    }

    public Person getOwner() {
        return this.owner;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Owned by %s", name, model, owner);
    }
}

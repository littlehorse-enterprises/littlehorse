package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "car")
public class Car {
    public String name;

    public String getName() {
        return this.name;
    }
}

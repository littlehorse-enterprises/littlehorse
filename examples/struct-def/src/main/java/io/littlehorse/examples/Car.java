package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "car")
public class Car {
    public String brand;
    public String model;
    public Integer price;
    public Integer year;
    public Boolean isElectric;
    public String[] features;
}

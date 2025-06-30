package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

@LHStructDef(name = "car")
public class Car {
    public Garage garage;
    public Person owner;
    public String brand;
    public String model;
    public Integer price;
    public Integer year;
    public Boolean isElectric;

    @LHStructField(masked = true)
    public String[] features;

}

package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("struct-car")
public class CarWithExtraField {
    public String brand;
    public String model;
    public int mileage;
    public String color;

    public CarWithExtraField() {}

    public CarWithExtraField(String brand, String model, int mileage, String color) {
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
        this.color = color;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public int getMileage() {
        return this.mileage;
    }

    public String getColor() {
        return this.color;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public void setMileage(final int mileage) {
        this.mileage = mileage;
    }

    public void setColor(final String color) {
        this.color = color;
    }
}

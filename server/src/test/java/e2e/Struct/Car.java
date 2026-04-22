package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("struct-car")
public class Car {
    public String brand;
    public String model;
    public Integer mileage;

    public Car() {}

    public Car(String brand, String model, Integer mileage) {
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Mileage: %d", brand, model, mileage);
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public Integer getMileage() {
        return this.mileage;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public void setMileage(final Integer mileage) {
        this.mileage = mileage;
    }
}

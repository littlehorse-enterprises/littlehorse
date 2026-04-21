package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef("struct-car")
public class CarWithMissingField {
    public String brand;
    public String model;

    public CarWithMissingField() {}

    public CarWithMissingField(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getModel() {
        return this.model;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public void setModel(final String model) {
        this.model = model;
    }
}

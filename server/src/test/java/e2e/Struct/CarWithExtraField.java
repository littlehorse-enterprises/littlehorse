package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-car")
@Getter
@Setter
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
}

package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-car")
@Getter
@Setter
public class Car {
    public String brand;
    public String model;
    public int mileage;

    public Car() {}

    public Car(String brand, String model, int mileage) {
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Mileage: %d", brand, model, mileage);
    }
}

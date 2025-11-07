package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-car")
@Getter
@Setter
public class CarWithMissingField {
    public String brand;
    public String model;

    public CarWithMissingField() {}

    public CarWithMissingField(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }
}

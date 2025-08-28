package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef(name = "car")
@Getter
@Setter
public class Car {
    public String firstName;
    public String lastName;
    public int mileage;

    public Car() {}

    public Car(String firstName, String lastName, int mileage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mileage = mileage;
    }
}

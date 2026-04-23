package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-person-with-address")
@Getter
@Setter
public class PersonWithAddress {
    public String name;
    public Address address;

    public PersonWithAddress() {}

    public PersonWithAddress(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", name, address);
    }
}

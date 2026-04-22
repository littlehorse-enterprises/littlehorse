package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("struct-address")
@Getter
@Setter
public class Address {
    public String street;
    public String state;
    public int zip;

    public Address() {}

    public Address(String street, String state, int zip) {
        this.street = street;
        this.state = state;
        this.zip = zip;
    }

    @Override
    public String toString() {
        return String.format("%s, %s %d", street, state, zip);
    }
}

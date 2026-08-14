package e2e.Struct;

import lombok.Getter;
import lombok.Setter;

/** Intentionally unannotated — used to test that {@code declareInlineStruct} works without {@code @LHStructDef}. */
@Getter
@Setter
public class Location {
    public String city;
    public int zipCode;

    public Location() {}

    public Location(String city, int zipCode) {
        this.city = city;
        this.zipCode = zipCode;
    }
}

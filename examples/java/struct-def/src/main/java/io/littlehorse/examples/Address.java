package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef("address")
public class Address {
    private Integer houseNumber;
    private String street;
    private String city;
    private String planet;
    private Integer zipCode;

    public Address() {}

    public Address(Integer houseNumber, String street, String city, String planet, Integer zipCode) {
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.planet = planet;
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return "%s %s, %s, %s %d".formatted(houseNumber, street, city, planet, zipCode);
    }
}

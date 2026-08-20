package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef("address")
public class Address {
    @LHStructField(description = "The street number of the building.")
    private Integer houseNumber;

    @LHStructField(description = "The name of the street.")
    private String street;

    @LHStructField(description = "The city or municipality.")
    private String city;

    @LHStructField(description = "The planet where the address is located.")
    private String planet;

    @LHStructField(description = "The postal code for the location.")
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

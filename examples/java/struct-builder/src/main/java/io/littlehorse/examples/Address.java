package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef("address")
public class Address {
    private String street;
    private String city;
    private String state;
    private Integer zip;

    public Address() {}

    public Address(String street, String city, String state, Integer zip) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "%s, %s, %s %d".formatted(street, city, state, zip);
    }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructField;

public class DeliveryAddress {
    @LHStructField(description = "The street address for the delivery destination.")
    private String street;

    @LHStructField(description = "The city for the delivery destination.")
    private String city;

    @LHStructField(description = "The postal code for the delivery destination.")
    private String postalCode;

    public DeliveryAddress() {}

    public DeliveryAddress(String street, String city, String postalCode) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}

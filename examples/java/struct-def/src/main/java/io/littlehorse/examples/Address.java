package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef(name = "address")
public class Address {
  private int houseNumber;
  private String street;
  private String city;
  private String planet;
  private int zipCode;

  public Address() {}

  public Address(int houseNumber, String street, String city, String planet, int zipCode) {
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

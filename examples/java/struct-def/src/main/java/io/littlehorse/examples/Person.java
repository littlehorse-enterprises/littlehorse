package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef(name = "person")
public class Person {
    private String firstName;
    private String lastName;
    private Address homeAddress;

    public Person() {}

    public Person(String firstName, String lastName, Address homeAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.homeAddress = homeAddress;
    }

    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}

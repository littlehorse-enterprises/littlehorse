package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef("person")
public class Person {
    private String name;
    private String email;
    private Address address;

    public Person() {}

    public Person(String name, String email, Address address) {
        this.name = name;
        this.email = email;
        this.address = address;
    }

    @Override
    public String toString() {
        return "%s <%s> at %s".formatted(name, email, address);
    }
}

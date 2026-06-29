package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@LHStructDef("person")
@Getter
@Setter
public class Person {
    public String name;
    public String[] friends;
    public PhoneNumbers phoneNumbers;

    public Person() {}

    public Person(String name, String[] friends, PhoneNumbers phoneNumbers) {
        this.name = name;
        this.friends = friends;
        this.phoneNumbers = phoneNumbers;
    }
}

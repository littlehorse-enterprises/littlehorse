package e2e.Struct;

import io.littlehorse.sdk.worker.LHStructDef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@LHStructDef(name = "person")
@Getter
@Setter
public class Person {
    public String name;
    public List<String> friends;
    public Map<String, String> phoneNumbers;

    public Person() {}
    ;

    public Person(String name, List<String> friends, Map<String, String> phoneNumbers) {
        this.name = name;
        this.friends = new ArrayList<>(friends);
        this.phoneNumbers = new HashMap<>(phoneNumbers);
    }
}

package e2e.Struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonPojo {
    public String name;
    public List<String> friends;
    public Map<String, String> phoneNumbers;

    public PersonPojo() {}
    ;

    public PersonPojo(String name, List<String> friends, Map<String, String> phoneNumbers) {
        this.name = name;
        this.friends = new ArrayList<>(friends);
        this.phoneNumbers = new HashMap<>(phoneNumbers);
    }
}

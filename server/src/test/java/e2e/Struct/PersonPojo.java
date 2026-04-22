package e2e.Struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonPojo {
    public String name;
    public List<String> friends;
    public Map<String, String> phoneNumbers;

    public PersonPojo() {}

    public PersonPojo(String name, List<String> friends, Map<String, String> phoneNumbers) {
        this.name = name;
        this.friends = new ArrayList<>(friends);
        this.phoneNumbers = new HashMap<>(phoneNumbers);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getFriends() {
        return this.friends;
    }

    public Map<String, String> getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFriends(final List<String> friends) {
        this.friends = friends;
    }

    public void setPhoneNumbers(final Map<String, String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    /**
     * Simulates looking up an address by name. In a real application this would
     * call a database or external service and return a JSON object.
     */
    @LHTaskMethod("fetch-address")
    public Address fetchAddress(String name) {
        log.debug("Looking up address for {}", name);
        return new Address("124 Sand Dune Lane", "Anchorhead", "Tattooine", 97412);
    }

    @LHTaskMethod("save-person")
    public String savePerson(Person person) {
        log.debug("Saving person record: {}", person);
        return "Saved %s".formatted(person);
    }
}

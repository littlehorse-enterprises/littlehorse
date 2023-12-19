package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForExternalEventWorker {

    private static final Logger log = LoggerFactory.getLogger(
        WaitForExternalEventWorker.class
    );

    @LHTaskMethod("ask-for-person-details")
    public String askForName() {
        log.debug("Executing ask-for-name");
        return "Hi what's your name?";
    }

    @LHTaskMethod("greet")
    public String greet(String name) {
        log.debug("Executing greet");
        return "Hello there, " + name;
    }
}

class Person {
    public String name;
}

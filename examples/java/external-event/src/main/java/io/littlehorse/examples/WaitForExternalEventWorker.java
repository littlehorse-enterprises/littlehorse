package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForExternalEventWorker {

    private static final Logger log = LoggerFactory.getLogger(WaitForExternalEventWorker.class);

    @LHTaskMethod("ask-for-name")
    public String askForName() {
        log.debug("Executing ask-for-name");
        return "Hi what's your name?";
    }

    @LHTaskMethod("greet-external-event")
    public String greet(String name) {
        log.debug("Executing greet-external-event");
        return "Hello there, " + name;
    }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitForOneOfWorker {

    private static final Logger log = LoggerFactory.getLogger(WaitForOneOfWorker.class);

    @LHTaskMethod("child-completed")
    public String childCompleted() {
        log.info("One of the child threads has completed!");
        return "Child thread execution completed successfully";
    }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("get-owner")
    public Person getOwner(Car car) {
        return car.getOwner();
    }

    @LHTaskMethod("notify-owner")
    public String notifyOwner(Person person) {
        return person.toString() + " has been notified!";
    }
}

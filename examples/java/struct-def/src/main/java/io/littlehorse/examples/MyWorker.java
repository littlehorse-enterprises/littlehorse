package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("greet")
    public String greet(String name) {
        log.debug("Executing greet");
        return "hello " + name;
    }

    @LHTaskMethod("describe-car")
    public String describeCar(Car car) {
        log.debug("Executing describe-car. {}", car);
        return "You drive a " + car.getBrand() + " " + car.getModel();
    }
}

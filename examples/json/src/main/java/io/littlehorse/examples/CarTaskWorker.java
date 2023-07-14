package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(CarTaskWorker.class);

    @LHTaskMethod("greet")
    public String greet(String name) {
        log.debug("Executing greet");
        return "hello " + name;
    }

    @LHTaskMethod("describe-car")
    public String describeCar(Car car) {
        log.debug("Executing describe-car. {}", car);
        return "You drive a " + car.brand + " " + car.model;
    }
}

class Car {

    public String brand;
    public String model;

    @Override
    public String toString() {
        return "Car{" + "brand='" + brand + '\'' + ", model='" + model + '\'' + '}';
    }
}

class Person {

    public String name;
    public Car car;
}

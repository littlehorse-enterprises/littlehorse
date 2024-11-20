package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    @LHTaskMethod("charge-credit-card")
    public String greeting(String user, int price) {
        System.out.println("Price: " + price);
        return "hello there, " + user;
    }

}

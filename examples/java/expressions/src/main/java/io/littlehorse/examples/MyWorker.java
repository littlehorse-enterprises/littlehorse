package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("place-order")
    public String placeOrder(Double total) {
        log.debug("Executing place order with total: {}", total);
        return "total paid: " + total;
    }
}

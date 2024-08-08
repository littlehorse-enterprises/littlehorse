package io.littlehorse.examples;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("greet")
    @LHType(VariableType.MASK)
    public String greeting(@LHType(VariableType.MASK) String name) {
        log.debug("Executing task greet");
        return "hello there, " + name;
    }

}

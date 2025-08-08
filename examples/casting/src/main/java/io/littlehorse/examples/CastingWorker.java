package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CastingWorker {

    private static final Logger log = LoggerFactory.getLogger(CastingWorker.class);

    @LHTaskMethod("string-method")
    public String stringMethod(String value, WorkerContext context) {
        context.log("Executing string-method with value:"+ value);
        return "String: " + value;
    }
    @LHTaskMethod("int-method")
    public int intMethod(int value, WorkerContext context ) {
        int result = value * 2;
        context.log("Executing int-method with value: " + value + ", and doubling its value to: " + result);
        return result;
    }
    @LHTaskMethod("double-method")
    public double doubleMethod(double value, WorkerContext context) {
        double result = value * 0.9;
        context.log("Executing double-method with value: " + value + ", and reducing its value to: " + result);
        return result;
    }
    @LHTaskMethod("bool-method")
    public boolean boolMethod(boolean value, WorkerContext context) {
        boolean result = !value;
        context.log("Executing bool-method with value: " + value + ", and toggling its value to: " + result);
        return result;
    }
}

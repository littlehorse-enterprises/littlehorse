package io.littlehorse.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class CheckpointTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(CheckpointTaskWorker.class);

    @LHTaskMethod("greet")
    public String greeting(String name, WorkerContext context) { // WorkerContext enables checkpointing
        int attemptNumber = context.getAttemptNumber();
        System.out.println("Hello from task worker on attempt " + attemptNumber + " before the checkpoint");

        String result = context.executeAndCheckpoint(
                (checkpointContext) -> {
                    checkpointContext.log("test log");
                    System.out.println(
                            "Hello from task worker on attempt " + attemptNumber + " in the first checkpoint");
                    return "hello " + name + " from first checkpoint";
                },
                String.class);

        System.out.println("Hello from after the first checkpoint");

        if (attemptNumber == 0) {
            throw new RuntimeException("Throwing a failure in the second checkpoint to show how the checkpoint works");
        }

        result += context.executeAndCheckpoint(
                (_) -> {
                    System.out.println("Hi from inside the second checkpoint");
                    return " and the second checkpoint";
                },
                String.class);

        System.out.println("Hi from after the checkpoints on attemptNumber " + attemptNumber);

        return result + " and after the second checkpoint";
    }
}

package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    @LHTaskMethod(value = "get-uuid", description = "Generates and returns a random UUID.")
    public UUID getUUID() {
        UUID uuid = UUID.randomUUID();
        log.debug("Generated UUID {}", uuid);
        return uuid;
    }

    @LHTaskMethod(value = "echo-uuid", description = "Receives a UUID and writes it to task log output.")
    public void echoUUID(UUID uuid, WorkerContext context) {
        log.debug("Received UUID {}", uuid);
        context.log("Received UUID via adapter: " + uuid);
    }
}

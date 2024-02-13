package io.littlehorse.canary.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shutdown {
    private Shutdown() {}

    public static void addShutdownHook(final String message, final AutoCloseable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("Error in ShutdownHook '{}'", message, e);
            }
            log.trace("Shutdown process for '{}' was completed", message);
        }));
    }
}

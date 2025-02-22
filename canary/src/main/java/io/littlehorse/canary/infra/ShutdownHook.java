package io.littlehorse.canary.infra;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownHook {
    private ShutdownHook() {}

    public static void add(final String message, final AutoCloseable closeable) {
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

package io.littlehorse.canary.util;

import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shutdown {
    private Shutdown() {}

    public static void addShutdownHook(final AutoCloseable closeable) {
        addShutdownHook(null, closeable);
    }

    public static void addShutdownHook(final String name, final AutoCloseable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final String nameToPresent =
                    name != null ? name : closeable.getClass().getSimpleName();
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("Error in ShutdownHook '{}'", nameToPresent, e);
            }
            log.trace("{} shutdown process completed", nameToPresent);
        }));
    }

    public static void block() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        addShutdownHook("Main", latch::countDown);
        latch.await();
    }
}

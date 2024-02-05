package io.littlehorse.canary.util;

import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shutdown {
    private Shutdown() {}

    public static void addShutdownHook(final AutoCloseable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("Error in ShutdownHook '{}'", closeable.getClass().getName(), e);
            }
            log.trace("{} shutdown process completed", closeable.getClass().getName());
        }));
    }

    public static void block() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        addShutdownHook(latch::countDown);
        latch.await();
    }
}

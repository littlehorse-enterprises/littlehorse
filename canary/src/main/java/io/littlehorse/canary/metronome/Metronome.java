package io.littlehorse.canary.metronome;

import io.littlehorse.canary.kafka.MetricsEmitter;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome implements Closeable, Runnable {

    private final ScheduledExecutorService executor;
    private final MetricsEmitter emitter;

    public Metronome(final MetricsEmitter emitter) {
        this.emitter = emitter;
        executor = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        executor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        executor.shutdown();
        log.trace("Closed");
    }

    @Override
    public void run() {
        log.trace("Executing metronome");
    }
}

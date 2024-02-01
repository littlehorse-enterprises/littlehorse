package io.littlehorse.canary.metronome;

import io.littlehorse.canary.kafka.MetricsEmitter;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome implements Closeable, Runnable {

    private final MetricsEmitter emitter;
    private final ScheduledExecutorService executor;

    public Metronome(final MetricsEmitter emitter, final long frequency) {
        this.emitter = emitter;

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this, 0, frequency, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        log.trace("Executing metronome");
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}

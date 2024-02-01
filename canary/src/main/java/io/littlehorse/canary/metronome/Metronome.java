package io.littlehorse.canary.metronome;

import io.littlehorse.canary.kafka.MetricsEmitter;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome implements Closeable {

    private final MetricsEmitter emitter;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final int runs;

    public Metronome(final MetricsEmitter emitter, final long frequency, final int threads, final int runs) {
        this.emitter = emitter;

        this.runs = runs;

        mainExecutor = Executors.newScheduledThreadPool(1);
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency, TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
    }

    private static void executeRuns() {
        log.trace("Executing runs");
    }

    private void scheduledRun() {
        log.trace("Executing metronome");
        for (int i = 0; i < runs; i++) {
            requestsExecutor.submit(Metronome::executeRuns);
        }
    }

    @Override
    public void close() {
        mainExecutor.shutdown();
        requestsExecutor.shutdown();
    }
}

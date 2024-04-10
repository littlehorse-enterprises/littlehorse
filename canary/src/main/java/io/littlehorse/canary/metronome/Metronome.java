package io.littlehorse.canary.metronome;

import io.littlehorse.canary.kafka.BeatEmitter;
import io.littlehorse.canary.proto.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.util.LHClient;
import io.littlehorse.canary.util.ShutdownHook;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome {

    private final BeatEmitter emitter;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final LHClient lhClient;
    private final int runs;

    public Metronome(
            final BeatEmitter emitter,
            final LHClient lhClient,
            final Duration frequency,
            final int threads,
            final int runs) {
        this.emitter = emitter;
        this.lhClient = lhClient;
        this.runs = runs;

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Metronome: Main Executor Thread", () -> closeExecutor(mainExecutor));
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency.toMillis(), TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
        ShutdownHook.add("Metronome: Request Executor Thread", () -> closeExecutor(requestsExecutor));

        log.info("Metronome Started");
    }

    private void closeExecutor(final ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void executeRun() {
        final Instant start = Instant.now();
        final String wfId = UUID.randomUUID().toString().replace("-", "");

        log.debug("Executing run {}", wfId);

        try {
            lhClient.runCanaryWf(wfId, start);
            emmit(wfId, start, BeatStatus.OK);
        } catch (Exception e) {
            emmit(wfId, start, BeatStatus.ERROR);
        }
    }

    private void emmit(final String wfId, final Instant start, final BeatStatus status) {
        emitter.future(wfId, BeatType.WF_RUN_REQUEST, status, Duration.between(start, Instant.now()));
    }

    private void scheduledRun() {
        log.trace("Executing metronome");
        for (int i = 0; i < runs; i++) {
            requestsExecutor.submit(this::executeRun);
        }
    }
}

package io.littlehorse.canary.metronome;

import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
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
public class MetronomeRunWfExecutor {

    private final BeatProducer producer;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final LHClient lhClient;
    private final int runs;
    private final LocalRepository repository;

    public MetronomeRunWfExecutor(
            final BeatProducer producer,
            final LHClient lhClient,
            final Duration frequency,
            final int threads,
            final int runs,
            final LocalRepository repository) {
        this.producer = producer;
        this.lhClient = lhClient;
        this.runs = runs;
        this.repository = repository;

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Metronome: RunWf Main Executor Thread", () -> closeExecutor(mainExecutor));
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency.toMillis(), TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
        ShutdownHook.add("Metronome: RunWf Request Executor Thread", () -> closeExecutor(requestsExecutor));

        log.info("RunWf Metronome Started");
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
        } catch (Exception e) {
            sendMetricBeat(wfId, start, BeatStatus.ERROR.name());
            throw e;
        }

        sendMetricBeat(wfId, start, BeatStatus.OK.name());
        repository.save(wfId, 0);
    }

    private void sendMetricBeat(final String wfId, final Instant start, final String status) {
        producer.sendFuture(wfId, BeatType.WF_RUN_REQUEST, status, Duration.between(start, Instant.now()));
    }

    private void scheduledRun() {
        log.trace("Executing run wf metronome");
        for (int i = 0; i < runs; i++) {
            requestsExecutor.submit(this::executeRun);
        }
    }
}

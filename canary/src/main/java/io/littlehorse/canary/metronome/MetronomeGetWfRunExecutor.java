package io.littlehorse.canary.metronome;

import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.proto.Attempt;
import io.littlehorse.canary.proto.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.util.LHClient;
import io.littlehorse.canary.util.ShutdownHook;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WfRun;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeGetWfRunExecutor {
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final BeatProducer producer;
    private final LHClient lhClient;
    private final LocalRepository repository;

    public MetronomeGetWfRunExecutor(
            final BeatProducer producer,
            final LHClient lhClient,
            final Duration frequency,
            final int threads,
            final LocalRepository repository) {
        this.producer = producer;
        this.lhClient = lhClient;
        this.repository = repository;

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Metronome: GetWfRun  Main Executor Thread", () -> closeExecutor(mainExecutor));
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency.toMillis(), TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
        ShutdownHook.add("Metronome: GetWfRun  Request Executor Thread", () -> closeExecutor(requestsExecutor));

        log.info("GetWfRun Metronome Started");
    }

    private void closeExecutor(final ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void scheduledRun() {
        log.trace("Executing GetWfRun metronome");
        final Instant searchCriteria = Instant.now().minus(Duration.ofMinutes(1));
        final Map<String, Attempt> attempts = repository.getAttemptsBefore(searchCriteria);
        attempts.forEach(this::executeRun);
    }

    private void executeRun(final String id, final Attempt attempt) {
        log.trace("GetWfRun Attempt {} {}", id, attempt.getLastAttempt());
        final Instant start = Instant.now();
        try {
            final WfRun run = lhClient.getCanaryWfRun(id);
            log.debug("GetWfRun {} {}", id, run.getStatus());
            producer.sendFuture(
                    id, BeatType.GET_WF_RUN_REQUEST, run.getStatus().name(), Duration.between(start, Instant.now()));

            if (run.getStatus().equals(LHStatus.COMPLETED)) {
                repository.delete(id);
            }
            // TODO MISSING RETRY LOGIC
            // TODO ADD DELETE AFTER IT GET EXHAUSTED
        } catch (Exception e) {
            producer.sendFuture(
                    id, BeatType.GET_WF_RUN_REQUEST, BeatStatus.ERROR.name(), Duration.between(start, Instant.now()));
        }
    }
}

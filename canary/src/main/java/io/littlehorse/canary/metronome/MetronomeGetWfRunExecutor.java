package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
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
    private final long retries;

    public MetronomeGetWfRunExecutor(
            final BeatProducer producer,
            final LHClient lhClient,
            final Duration frequency,
            final int threads,
            final int retries,
            final LocalRepository repository) {
        this.producer = producer;
        this.lhClient = lhClient;
        this.repository = repository;
        this.retries = retries;

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
        if (attempt.getAttempt() >= retries) {
            repository.delete(id);
            producer.sendFuture(id, BeatType.GET_WF_RUN_EXHAUSTED_RETRIES);
            return;
        }

        updateAttempt(id, attempt);

        final Instant start = Instant.now();
        final WfRun currentStatus = getCurrentStatus(id, start);

        if (currentStatus == null) {
            return;
        }

        producer.sendFuture(
                id,
                BeatType.GET_WF_RUN_REQUEST,
                currentStatus.getStatus().name(),
                Duration.between(start, Instant.now()));

        log.debug("GetWfRun {} {}", id, currentStatus.getStatus());
        if (currentStatus.getStatus().equals(LHStatus.COMPLETED)) {
            repository.delete(id);
        }
    }

    private void updateAttempt(final String id, final Attempt attempt) {
        repository.save(
                id,
                Attempt.newBuilder()
                        .setStart(attempt.getStart())
                        .setLastAttempt(Timestamps.now())
                        .setAttempt(attempt.getAttempt() + 1)
                        .build());
    }

    private WfRun getCurrentStatus(final String id, final Instant start) {
        try {
            return lhClient.getCanaryWfRun(id);
        } catch (Exception e) {
            log.error("Error executing getWfRun {}", e.getMessage(), e);
            producer.sendFuture(
                    id, BeatType.GET_WF_RUN_REQUEST, BeatStatus.ERROR.name(), Duration.between(start, Instant.now()));
            return null;
        }
    }
}

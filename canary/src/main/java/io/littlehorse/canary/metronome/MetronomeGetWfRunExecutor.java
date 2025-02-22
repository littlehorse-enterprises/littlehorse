package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.grpc.StatusRuntimeException;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.littlehorse.LHClient;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.metronome.model.Beat;
import io.littlehorse.canary.metronome.model.BeatStatus;
import io.littlehorse.canary.proto.Attempt;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.sdk.common.proto.LHStatus;
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
    public static final String EXHAUSTED_RETRIES = "EXHAUSTED_RETRIES";
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
        // exist if it gets exhausted
        if (attempt.getAttempt() >= retries) {
            exhaustedRetries(id);
            return;
        }

        // update retry number
        updateAttempt(id, attempt);

        final Instant start = Instant.now();
        final LHStatus status = getCurrentStatus(id);
        final Instant end = Instant.now();

        if (status == null) {
            return;
        }

        log.debug("GetWfRun {} {}", id, status);

        // check if wf run was successful
        if (status.equals(LHStatus.COMPLETED)) {
            sendSuccessfulWfRun(id, status, Duration.between(start, end));
            return;
        }

        // this status is not expected, send error
        log.error("GetWfRun returns error {} {}", id, status);
        sendErrorWfRun(id, status);
    }

    private void sendErrorWfRun(final String id, final LHStatus status) {
        final BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.ERROR)
                .source(BeatStatus.Source.WORKFLOW)
                .reason(status.name())
                .build();

        final Beat beat = Beat.builder(BeatType.GET_WF_RUN_REQUEST)
                .id(id)
                .status(beatStatus)
                .build();

        producer.send(beat);
    }

    private void sendSuccessfulWfRun(final String id, final LHStatus currentStatus, final Duration latency) {
        final BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.OK)
                .source(BeatStatus.Source.WORKFLOW)
                .reason(currentStatus.name())
                .build();

        final Beat beat = Beat.builder(BeatType.GET_WF_RUN_REQUEST)
                .id(id)
                .latency(latency)
                .status(beatStatus)
                .build();

        producer.send(beat);
        repository.delete(id);
    }

    private void exhaustedRetries(final String id) {
        repository.delete(id);

        final BeatStatus beatStatus = BeatStatus.builder(BeatStatus.Code.ERROR)
                .reason(EXHAUSTED_RETRIES)
                .build();

        final Beat beat = Beat.builder(BeatType.GET_WF_RUN_REQUEST)
                .id(id)
                .status(beatStatus)
                .build();

        producer.send(beat);
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

    private LHStatus getCurrentStatus(final String id) {
        try {
            return lhClient.getCanaryWfRun(id).getStatus();
        } catch (Exception e) {
            log.error("Error executing getWfRun {}", e.getMessage(), e);

            final BeatStatus.BeatStatusBuilder statusBuilder = BeatStatus.builder(BeatStatus.Code.ERROR)
                    .reason(e.getClass().getSimpleName());

            if (e instanceof StatusRuntimeException statusException) {
                statusBuilder
                        .source(BeatStatus.Source.GRPC)
                        .reason(statusException.getStatus().getCode().name());
            }

            final Beat beat = Beat.builder(BeatType.GET_WF_RUN_REQUEST)
                    .id(id)
                    .status(statusBuilder.build())
                    .build();

            producer.send(beat);
            return null;
        }
    }
}

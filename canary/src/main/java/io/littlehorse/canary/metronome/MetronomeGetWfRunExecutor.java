package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.grpc.StatusRuntimeException;
import io.littlehorse.canary.infra.HealthStatusBinder;
import io.littlehorse.canary.infra.HealthStatusRegistry;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeGetWfRunExecutor implements HealthStatusBinder {
    public static final String EXHAUSTED_RETRIES = "EXHAUSTED_RETRIES";
    private final ScheduledExecutorService mainExecutor;
    private final BeatProducer producer;
    private final LHClient lhClient;
    private final Duration frequency;
    private final LocalRepository repository;
    private final long retries;
    private ScheduledFuture<?> scheduledFuture;

    public MetronomeGetWfRunExecutor(
            final BeatProducer producer,
            final LHClient lhClient,
            final Duration frequency,
            final int retries,
            final LocalRepository repository) {
        this.producer = producer;
        this.lhClient = lhClient;
        this.frequency = frequency;
        this.repository = repository;
        this.retries = retries;

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Metronome: GetWfRun  Main Executor Thread", () -> closeExecutor(mainExecutor));
    }

    public void start() {
        scheduledFuture = mainExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        scheduledRun();
                    } catch (Exception e) {
                        log.error("Error when executing workflow run", e);
                    }
                },
                0,
                frequency.toMillis(),
                TimeUnit.MILLISECONDS);
        log.info("GetWfRun Metronome Started");
    }

    private void closeExecutor(final ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void scheduledRun() {
        log.debug("Executing GetWfRun metronome");
        final Instant searchCriteria = Instant.now().minus(Duration.ofMinutes(1));
        final Map<String, Attempt> attempts = repository.getAttemptsBefore(searchCriteria);
        attempts.forEach((id, attempt) -> {
            try {
                executeRun(id, attempt);
            } catch (Exception e) {
                sendError(id, e);
            }
        });
    }

    private void executeRun(final String id, final Attempt attempt) {
        log.debug("GetWfRun {}", id);

        // exit if it gets exhausted
        if (attempt.getAttempt() >= retries) {
            sendExhaustedRetries(id);
            return;
        }

        // update attempt number
        updateAttempt(id, attempt);

        // get status
        final Instant start = Instant.now();
        final LHStatus status = lhClient.getCanaryWfRun(id).getStatus();
        final Duration latency = Duration.between(start, Instant.now());

        // send beat and exit
        sendBeat(id, status, latency);

        if (status.equals(LHStatus.COMPLETED)) {
            repository.delete(id);
            return;
        }

        // log in case of error
        log.error("GetWfRun returns workflow error {} {}", id, status);
    }

    private void sendBeat(final String id, final LHStatus status, final Duration latency) {
        final BeatStatus beatStatus = BeatStatus.builder(
                        status.equals(LHStatus.COMPLETED) ? BeatStatus.Code.OK : BeatStatus.Code.ERROR)
                .source(BeatStatus.Source.WORKFLOW)
                .reason(status.name())
                .build();

        final Beat beat = Beat.builder(BeatType.GET_WF_RUN_REQUEST)
                .id(id)
                .latency(latency)
                .status(beatStatus)
                .build();

        producer.send(beat);
    }

    private void sendExhaustedRetries(final String id) {
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
        final Attempt newAttempt = Attempt.newBuilder()
                .setStart(attempt.getStart())
                .setLastAttempt(Timestamps.now())
                .setAttempt(attempt.getAttempt() + 1)
                .build();
        log.debug("GetWfRun {} Retry {}", id, newAttempt.getAttempt());
        repository.save(id, newAttempt);
    }

    private void sendError(final String id, final Exception e) {
        log.error("Error executing getWfRun {}", e.getMessage(), e);

        final BeatStatus.BeatStatusBuilder statusBuilder =
                BeatStatus.builder(BeatStatus.Code.ERROR).reason(e.getClass().getSimpleName());

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
    }

    @Override
    public void bindTo(final HealthStatusRegistry registry) {
        registry.addStatus("metronome-get-wf-run-executor", this::isRunning);
    }

    private boolean isRunning() {
        return scheduledFuture != null && !scheduledFuture.isDone();
    }
}

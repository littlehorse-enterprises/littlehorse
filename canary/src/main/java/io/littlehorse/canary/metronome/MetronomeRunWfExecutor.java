package io.littlehorse.canary.metronome;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.littlehorse.LHClient;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.metronome.model.Beat;
import io.littlehorse.canary.metronome.model.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.sdk.common.proto.WfRun;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeRunWfExecutor {

    private final BeatProducer producer;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final LHClient lhClient;
    private final Duration frequency;
    private final int runs;
    private final LocalRepository repository;
    private final int sampleRate;
    private final boolean sampleDataEnabled;
    private final int sampleSize;

    public MetronomeRunWfExecutor(
            final BeatProducer producer,
            final LHClient lhClient,
            final Duration frequency,
            final int threads,
            final int runs,
            final int sampleRate,
            final LocalRepository repository) {
        this.producer = producer;
        this.lhClient = lhClient;
        this.frequency = frequency;
        this.runs = runs;
        this.repository = repository;
        this.sampleRate = sampleRate;
        this.sampleDataEnabled = sampleRate > 0;
        this.sampleSize = (int) (runs * (sampleRate / 100.0));

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        ShutdownHook.add("Metronome: RunWf Main Executor Thread", () -> closeExecutor(mainExecutor));

        requestsExecutor = Executors.newFixedThreadPool(threads);
        ShutdownHook.add("Metronome: RunWf Request Executor Thread", () -> closeExecutor(requestsExecutor));
    }

    public void start() {
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency.toMillis(), TimeUnit.MILLISECONDS);
        log.info("RunWf Metronome Started");
    }

    private void closeExecutor(final ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void executeRun(final boolean isSampleIteration) {
        final Instant start = Instant.now();
        final String wfId = UUID.randomUUID().toString().replace("-", "");

        log.debug("Executing run {}", wfId);

        final ListenableFuture<WfRun> future = lhClient.runCanaryWf(wfId, start, isSampleIteration);
        Futures.addCallback(future, new MetronomeCallback(wfId, start, isSampleIteration), requestsExecutor);
    }

    private void scheduledRun() {
        log.trace("Executing run wf metronome");
        final HashSet<Integer> sample = createSampleRuns();
        for (int i = 0; i < runs; i++) {
            final boolean isSampleIteration = sample.contains(i);
            this.executeRun(isSampleIteration);
        }
    }

    private HashSet<Integer> createSampleRuns() {
        if (!sampleDataEnabled) {
            return new HashSet<>();
        }
        final List<Integer> range =
                new ArrayList<>(IntStream.range(0, runs).boxed().toList());
        Collections.shuffle(range);
        final List<Integer> sample = range.subList(0, sampleSize);
        return new HashSet<>(sample);
    }

    private class MetronomeCallback implements FutureCallback<WfRun> {
        private final boolean isSampleIteration;
        private final String wfRunId;
        private final Instant startedAt;

        private MetronomeCallback(final String wfRunId, final Instant startedAt, final boolean isSampleIteration) {
            this.isSampleIteration = isSampleIteration;
            this.wfRunId = wfRunId;
            this.startedAt = startedAt;
        }

        @Override
        public void onSuccess(final WfRun result) {
            if (!isSampleIteration) return;

            final Beat beat = Beat.builder(BeatType.WF_RUN_REQUEST)
                    .id(wfRunId)
                    .latency(Duration.between(startedAt, Instant.now()))
                    .status(BeatStatus.builder(BeatStatus.Code.OK).build())
                    .build();
            producer.send(beat);
            repository.save(wfRunId, 0);
        }

        @Override
        public void onFailure(final Throwable t) {
            log.error("Error executing runWf {}", t.getMessage(), t);

            final BeatStatus.BeatStatusBuilder statusBuilder = BeatStatus.builder(BeatStatus.Code.ERROR)
                    .reason(t.getClass().getSimpleName());

            if (t instanceof StatusRuntimeException statusException) {
                statusBuilder
                        .source(BeatStatus.Source.GRPC)
                        .reason(statusException.getStatus().getCode().name());
            }

            if (t instanceof StatusException statusException) {
                statusBuilder
                        .source(BeatStatus.Source.GRPC)
                        .reason(statusException.getStatus().getCode().name());
            }

            final Beat beat = Beat.builder(BeatType.WF_RUN_REQUEST)
                    .id(wfRunId)
                    .latency(Duration.between(startedAt, Instant.now()))
                    .status(statusBuilder.build())
                    .build();

            producer.send(beat);
        }
    }
}

package io.littlehorse.canary.metronome;

import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.proto.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.util.LHClient;
import io.littlehorse.canary.util.ShutdownHook;
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
        this.runs = runs;
        this.repository = repository;
        this.sampleRate = sampleRate;
        this.sampleDataEnabled = sampleRate > 0;
        this.sampleSize = (int) (runs * (sampleRate / 100.0));

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

    private void executeRun(boolean isSampleIteration) {
        final Instant start = Instant.now();
        final String wfId = UUID.randomUUID().toString().replace("-", "");

        log.debug("Executing run {}", wfId);

        try {
            lhClient.runCanaryWf(wfId, start, isSampleIteration);
        } catch (Exception e) {
            sendMetricBeat(wfId, start, BeatStatus.ERROR.name());
            return;
        }
        if (isSampleIteration) {
            sendMetricBeat(wfId, start, BeatStatus.OK.name());
            repository.save(wfId, 0);
        }
    }

    private void sendMetricBeat(final String wfId, final Instant start, final String status) {
        producer.sendFuture(wfId, BeatType.WF_RUN_REQUEST, status, Duration.between(start, Instant.now()));
    }

    private void scheduledRun() {
        log.trace("Executing run wf metronome");
        HashSet<Integer> sample = createSampleRuns();
        for (int i = 0; i < runs; i++) {
            final boolean isSampleIteration = sample.contains(i);
            requestsExecutor.submit(() -> this.executeRun(isSampleIteration));
        }
    }

    private HashSet<Integer> createSampleRuns() {
        if (!sampleDataEnabled) {
            return new HashSet<>();
        }
        final List<Integer> range =
                new ArrayList<>(IntStream.range(0, runs).boxed().toList());
        Collections.shuffle(range);
        List<Integer> sample = range.subList(0, sampleSize);
        return new HashSet<>(sample);
    }
}

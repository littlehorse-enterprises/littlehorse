package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.Aggregator;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.BeatEmitter;
import io.littlehorse.canary.kafka.TopicCreator;
import io.littlehorse.canary.metronome.Metronome;
import io.littlehorse.canary.metronome.MetronomeWorker;
import io.littlehorse.canary.metronome.MetronomeWorkflow;
import io.littlehorse.canary.prometheus.PrometheusExporter;
import io.littlehorse.canary.prometheus.PrometheusServerExporter;
import io.littlehorse.canary.util.LHClient;
import io.littlehorse.canary.util.ShutdownHook;
import io.littlehorse.sdk.common.config.LHConfig;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;

@Slf4j
public class Main {

    public static void main(final String[] args) throws InterruptedException {
        try {
            initialize(args);
        } catch (Exception e) {
            log.error("Error starting application", e);
            System.exit(-1);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        ShutdownHook.add("Main Thread", latch::countDown);
        latch.await();
    }

    private static void initialize(final String[] args) throws IOException {
        // dependencies
        final CanaryConfig canaryConfig = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();
        final LHConfig lhConfig =
                new LHConfig(canaryConfig.toLittleHorseConfig().toMap());
        final LHClient lhClient = new LHClient(lhConfig);
        final BeatEmitter emitter = new BeatEmitter(
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                lhClient.getServerVersion(),
                canaryConfig.getTopicName(),
                canaryConfig.toKafkaProducerConfig().toMap());
        final PrometheusExporter prometheusExporter = new PrometheusExporter(canaryConfig.getCommonTags());

        // create topics
        if (canaryConfig.isTopicCreationEnabled()) {
            new TopicCreator(
                    canaryConfig.toKafkaAdminConfig().toMap(),
                    new NewTopic(
                            canaryConfig.getTopicName(),
                            canaryConfig.getTopicPartitions(),
                            canaryConfig.getTopicReplicas()),
                    canaryConfig.getTopicCreationTimeout());
        }

        // start worker
        if (canaryConfig.isMetronomeWorkerEnabled()) {
            new MetronomeWorker(emitter, lhConfig);
        }

        // register wf
        if (canaryConfig.isWorkflowCreationEnabled()) {
            new MetronomeWorkflow(lhClient);
        }

        // start metronome client
        if (canaryConfig.isMetronomeEnabled()) {
            new Metronome(
                    emitter,
                    lhClient,
                    canaryConfig.getMetronomeFrequency(),
                    canaryConfig.getMetronomeThreads(),
                    canaryConfig.getMetronomeRuns());
        }

        // start the aggregator
        if (canaryConfig.isAggregatorEnabled()) {
            new PrometheusServerExporter(
                    canaryConfig.getMetricsPort(), canaryConfig.getMetricsPath(), prometheusExporter);
            final Aggregator aggregator = new Aggregator(
                    canaryConfig.toKafkaStreamsConfig().toMap(),
                    canaryConfig.getTopicName(),
                    canaryConfig.getAggregatorStoreRetention());
            prometheusExporter.addMeasurable(aggregator);
        }
    }
}

package io.littlehorse.canary;

import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.BeatEmitter;
import io.littlehorse.canary.kafka.KafkaTopicBootstrap;
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
        final CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();
        final LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());
        final LHClient lhClient = new LHClient(lhConfig);
        final BeatEmitter emitter = new BeatEmitter(
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                lhClient.getServerVersion(),
                config.getTopicName(),
                config.toKafkaProducerConfig().toMap());
        final PrometheusExporter prometheusExporter = new PrometheusExporter(config.getCommonTags());

        // create topics
        if (config.isTopicCreationEnabled()) {
            new KafkaTopicBootstrap(
                    config.toKafkaAdminConfig().toMap(),
                    new NewTopic(config.getTopicName(), config.getTopicPartitions(), config.getTopicReplicas()),
                    config.getTopicCreationTimeoutMs());
        }

        // start worker
        if (config.isMetronomeWorkerEnabled()) {
            new MetronomeWorker(emitter, lhConfig);
        }

        // register wf
        if (config.isWorkflowCreationEnabled()) {
            new MetronomeWorkflow(lhClient);
        }

        // start metronome client
        if (config.isMetronomeEnabled()) {
            new Metronome(
                    emitter,
                    lhClient,
                    config.getMetronomeFrequencyMs(),
                    config.getMetronomeThreads(),
                    config.getMetronomeRuns());
        }

        if (config.isAggregatorEnabled()) {
            final PrometheusServerExporter prometheusServerExporter =
                    new PrometheusServerExporter(config.getMetricsPort(), config.getMetricsPath(), prometheusExporter);

            //            final AggregatorBootstrap aggregatorBootstrap = new AggregatorBootstrap(config);
            //            prometheusExporterBootstrap.addMeasurable(aggregatorBootstrap);
        }
    }
}

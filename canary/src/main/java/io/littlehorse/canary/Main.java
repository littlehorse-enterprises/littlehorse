package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.Aggregator;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.TopicCreator;
import io.littlehorse.canary.metronome.MetronomeGetWfRunExecutor;
import io.littlehorse.canary.metronome.MetronomeRunWfExecutor;
import io.littlehorse.canary.metronome.MetronomeWorker;
import io.littlehorse.canary.metronome.MetronomeWorkflow;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.prometheus.PrometheusExporter;
import io.littlehorse.canary.prometheus.PrometheusServerExporter;
import io.littlehorse.canary.util.LHClient;
import io.littlehorse.canary.util.ShutdownHook;
import io.littlehorse.sdk.common.config.LHConfig;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
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

        final PrometheusExporter prometheusExporter = new PrometheusExporter(canaryConfig.getCommonTags());
        // create topics
        if (canaryConfig.isTopicCreationEnabled()) {
            final NewTopic topic = new NewTopic(
                    canaryConfig.getTopicName(), canaryConfig.getTopicPartitions(), canaryConfig.getTopicReplicas());

            new TopicCreator(
                    canaryConfig.toKafkaConfig().toMap(), canaryConfig.getTopicCreationTimeout(), List.of(topic));
        }
        final boolean metronomeOrWorkerEnabled =
                canaryConfig.isMetronomeEnabled() || canaryConfig.isMetronomeWorkerEnabled();

        if (metronomeOrWorkerEnabled) {
            final LHConfig lhConfig =
                    new LHConfig(canaryConfig.toLittleHorseConfig().toMap());
            final LHClient lhClient = new LHClient(
                    lhConfig,
                    canaryConfig.getWorkflowName(),
                    canaryConfig.getWorkflowVersion(),
                    canaryConfig.getWorkflowRevision());
            prometheusExporter.addMeasurable(lhClient);
            final BeatProducer producer = new BeatProducer(
                    lhConfig.getApiBootstrapHost(),
                    lhConfig.getApiBootstrapPort(),
                    lhClient.getServerVersion(),
                    canaryConfig.getTopicName(),
                    canaryConfig.toKafkaConfig().toMap(),
                    canaryConfig.getMetronomeBeatExtraTags());

            // start worker
            if (canaryConfig.isMetronomeWorkerEnabled()) {
                new MetronomeWorker(producer, lhConfig);
            }

            // start metronome client
            if (canaryConfig.isMetronomeEnabled()) {

                // register wf
                if (canaryConfig.isWorkflowCreationEnabled()) {
                    new MetronomeWorkflow(lhClient, canaryConfig.getWorkflowName());
                }

                final LocalRepository repository = new LocalRepository(canaryConfig.getMetronomeDataPath());

                new MetronomeRunWfExecutor(
                        producer,
                        lhClient,
                        canaryConfig.getMetronomeRunFrequency(),
                        canaryConfig.getMetronomeRunThreads(),
                        canaryConfig.getMetronomeRunRequests(),
                        canaryConfig.getMetronomeSampleRate(),
                        repository);

                new MetronomeGetWfRunExecutor(
                        producer,
                        lhClient,
                        canaryConfig.getMetronomeGetFrequency(),
                        canaryConfig.getMetronomeGetThreads(),
                        canaryConfig.getMetronomeGetRetries(),
                        repository);
            }
        }

        // start the aggregator
        if (canaryConfig.isAggregatorEnabled()) {
            new PrometheusServerExporter(
                    canaryConfig.getMetricsPort(), canaryConfig.getMetricsPath(), prometheusExporter);
            final Aggregator aggregator = new Aggregator(
                    canaryConfig.toKafkaConfig().toMap(),
                    canaryConfig.getTopicName(),
                    canaryConfig.getAggregatorStoreRetention(),
                    canaryConfig.getAggregatorExportFrequency());
            prometheusExporter.addMeasurable(aggregator);
        }
    }
}

package io.littlehorse.canary;

import io.javalin.http.HandlerType;
import io.littlehorse.canary.aggregator.Aggregator;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.infra.WebServer;
import io.littlehorse.canary.kafka.TopicCreator;
import io.littlehorse.canary.littlehorse.LHClient;
import io.littlehorse.canary.metronome.MetronomeGetWfRunExecutor;
import io.littlehorse.canary.metronome.MetronomeRunWfExecutor;
import io.littlehorse.canary.metronome.MetronomeWorker;
import io.littlehorse.canary.metronome.MetronomeWorkflow;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
import io.littlehorse.canary.prometheus.PrometheusExporter;
import io.littlehorse.sdk.common.config.LHConfig;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;

@Slf4j
public class Main {

    public static void main(final String[] args) throws InterruptedException {
        try {
            final CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();
            final PrometheusExporter exporter = new PrometheusExporter(config.getCommonTags());

            maybeCreateTopics(config);
            maybeStartMetronome(config);
            maybeStartAggregator(config, exporter);
            startWebServer(config, exporter);
        } catch (Exception e) {
            log.error("Error starting application", e);
            System.exit(-1);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        ShutdownHook.add("Main Thread", latch::countDown);
        latch.await();
    }

    private static void maybeStartMetronome(final CanaryConfig config) {
        if (!config.isMetronomeEnabled() && !config.isMetronomeWorkerEnabled()) return;

        final LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());

        final LHClient lhClient = new LHClient(
                lhConfig, config.getWorkflowName(), config.getWorkflowVersion(), config.getWorkflowRevision());

        final BeatProducer producer = new BeatProducer(
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                lhClient.getServerVersion(),
                config.getTopicName(),
                config.toKafkaProducerConfig().toMap(),
                config.getMetronomeBeatExtraTags());

        maybeStartMetronomeWorker(config, producer, lhConfig);
        maybeRegisterWorkflow(config, lhClient);
        maybeStartMetronomeExecutors(config, producer, lhClient);
    }

    private static void maybeStartMetronomeExecutors(
            final CanaryConfig config, final BeatProducer producer, final LHClient lhClient) {
        if (!config.isMetronomeEnabled()) return;

        final LocalRepository repository = new LocalRepository(config.getMetronomeDataPath());

        final MetronomeRunWfExecutor runWfExecutor = new MetronomeRunWfExecutor(
                producer,
                lhClient,
                config.getMetronomeRunFrequency(),
                config.getMetronomeRunThreads(),
                config.getMetronomeRunRequests(),
                config.getMetronomeSamplePercentage(),
                repository);
        runWfExecutor.start();

        final MetronomeGetWfRunExecutor getWfRunExecutor = new MetronomeGetWfRunExecutor(
                producer, lhClient, config.getMetronomeGetFrequency(), config.getMetronomeGetRetries(), repository);
        getWfRunExecutor.start();
    }

    private static void maybeRegisterWorkflow(final CanaryConfig config, final LHClient lhClient) {
        if (!config.isWorkflowCreationEnabled()) return;

        final MetronomeWorkflow workflow =
                new MetronomeWorkflow(lhClient, config.getWorkflowName(), config.getWorkflowRetention());
        workflow.register();
    }

    private static void maybeStartMetronomeWorker(
            final CanaryConfig config, final BeatProducer producer, final LHConfig lhConfig) {
        if (!config.isMetronomeWorkerEnabled()) return;

        final MetronomeWorker worker = new MetronomeWorker(producer, lhConfig);
        worker.start();
    }

    private static void startWebServer(final CanaryConfig config, final PrometheusExporter prometheusExporter) {
        final WebServer webServer = new WebServer(config.getMetricsPort());
        webServer.addHandler(HandlerType.GET, config.getMetricsPath(), prometheusExporter);
        webServer.start();
    }

    private static void maybeStartAggregator(final CanaryConfig config, final PrometheusExporter prometheusExporter) {
        if (!config.isAggregatorEnabled()) return;

        final Aggregator aggregator = new Aggregator(
                config.toKafkaStreamsConfig().toMap(),
                config.getTopicName(),
                config.getAggregatorStoreRetention(),
                config.getAggregatorExportFrequency());

        prometheusExporter.addMeasurable(aggregator);
        aggregator.start();
    }

    private static void maybeCreateTopics(final CanaryConfig config) {
        if (!config.isTopicCreationEnabled()) return;

        final NewTopic topic =
                new NewTopic(config.getTopicName(), config.getTopicPartitions(), config.getTopicReplicas());

        final TopicCreator topicCreator =
                new TopicCreator(config.toKafkaAdminConfig().toMap(), config.getTopicCreationTimeout());

        topicCreator.create(List.of(topic));
    }
}

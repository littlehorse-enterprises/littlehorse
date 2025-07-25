package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.Aggregator;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.infra.HealthExporter;
import io.littlehorse.canary.infra.PrometheusExporter;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.infra.WebServer;
import io.littlehorse.canary.infra.WebServiceBinder;
import io.littlehorse.canary.kafka.TopicCreator;
import io.littlehorse.canary.littlehorse.LHClient;
import io.littlehorse.canary.metronome.MetronomeGetWfRunExecutor;
import io.littlehorse.canary.metronome.MetronomeRunWfExecutor;
import io.littlehorse.canary.metronome.MetronomeWorker;
import io.littlehorse.canary.metronome.MetronomeWorkflow;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.internal.LocalRepository;
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
            initialize(config);
        } catch (Exception e) {
            log.error("Error starting application", e);
            System.exit(-1);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        ShutdownHook.add("Main Thread", latch::countDown);
        latch.await();
    }

    private static void initialize(final CanaryConfig config) {
        final PrometheusExporter prometheusExporter =
                new PrometheusExporter(config.getMetricsPath(), config.getCommonTags());
        final HealthExporter healthExporter = new HealthExporter(config.getHealthPath());

        maybeCreateTopics(config);
        maybeStartMetronome(config, healthExporter);
        maybeStartAggregator(config, prometheusExporter, healthExporter);
        startWebServer(config, prometheusExporter, healthExporter);
    }

    private static void maybeStartMetronome(final CanaryConfig config, final HealthExporter healthExporter) {
        if (!config.isMetronomeEnabled() && !config.isMetronomeWorkerEnabled()) return;

        final LHConfig lhConfig = new LHConfig(config.toLittleHorseConfig().toMap());

        final LHClient lhClient = new LHClient(
                lhConfig, config.getWorkflowName(), config.getWorkflowVersion(), config.getWorkflowRevision());

        final BeatProducer producer = new BeatProducer(
                lhConfig.getApiBootstrapHost(),
                lhConfig.getApiBootstrapPort(),
                config.getTopicName(),
                config.toKafkaConfig().toMap(),
                config.getMetronomeBeatExtraTags());

        maybeStartMetronomeWorker(config, producer, lhConfig, healthExporter);
        maybeRegisterWorkflow(config, lhClient);
        maybeStartMetronomeExecutors(config, producer, lhClient, healthExporter);
    }

    private static void maybeStartMetronomeExecutors(
            final CanaryConfig config,
            final BeatProducer producer,
            final LHClient lhClient,
            final HealthExporter healthExporter) {
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
        healthExporter.addStatus(runWfExecutor);
        runWfExecutor.start();

        final MetronomeGetWfRunExecutor getWfRunExecutor = new MetronomeGetWfRunExecutor(
                producer, lhClient, config.getMetronomeGetFrequency(), config.getMetronomeGetRetries(), repository);
        healthExporter.addStatus(getWfRunExecutor);
        getWfRunExecutor.start();
    }

    private static void maybeRegisterWorkflow(final CanaryConfig config, final LHClient lhClient) {
        if (!config.isWorkflowCreationEnabled()) return;

        final MetronomeWorkflow workflow =
                new MetronomeWorkflow(lhClient, config.getWorkflowName(), config.getWorkflowRetention());
        workflow.register();
    }

    private static void maybeStartMetronomeWorker(
            final CanaryConfig config,
            final BeatProducer producer,
            final LHConfig lhConfig,
            final HealthExporter healthExporter) {
        if (!config.isMetronomeWorkerEnabled()) return;

        final MetronomeWorker worker = new MetronomeWorker(producer, lhConfig);
        healthExporter.addStatus(worker);
        worker.start();
    }

    private static void startWebServer(final CanaryConfig config, final WebServiceBinder... services) {
        final WebServer webServer = new WebServer(config.getMetricsPort());
        webServer.addServices(services);
        webServer.start();
    }

    private static void maybeStartAggregator(
            final CanaryConfig config,
            final PrometheusExporter prometheusExporter,
            final HealthExporter healthExporter) {
        if (!config.isAggregatorEnabled()) return;

        final Aggregator aggregator = new Aggregator(
                config.toKafkaConfig().toMap(),
                config.getTopicName(),
                config.getAggregatorStoreRetention(),
                config.getAggregatorExportFrequency());

        prometheusExporter.addMeasurable(aggregator);
        healthExporter.addStatus(aggregator);
        aggregator.start();
    }

    private static void maybeCreateTopics(final CanaryConfig config) {
        if (!config.isTopicCreationEnabled()) return;

        final NewTopic topic =
                new NewTopic(config.getTopicName(), config.getTopicPartitions(), config.getTopicReplicas());

        final TopicCreator topicCreator =
                new TopicCreator(config.toKafkaConfig().toMap(), config.getTopicCreationTimeout());

        topicCreator.create(List.of(topic));
    }
}

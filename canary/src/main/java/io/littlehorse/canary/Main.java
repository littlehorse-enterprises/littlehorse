package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.AggregatorBootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.KafkaTopicBootstrap;
import io.littlehorse.canary.metronome.MetronomeBootstrap;
import io.littlehorse.canary.metronome.MetronomeWorkerBootstrap;
import io.littlehorse.canary.prometheus.PrometheusExporterBootstrap;
import io.littlehorse.canary.util.Shutdown;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(final String[] args) throws IOException, InterruptedException {
        final CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();

        log.debug("Canary configurations: {}", config);
        log.debug("Canary active metrics: {}", config.getEnabledMetrics());
        log.debug("KafkaAdmin configurations: {}", config.toKafkaAdminConfig());
        log.debug("KafkaProducer configurations: {}", config.toKafkaProducerConfig());
        log.debug("KafkaStreams configurations: {}", config.toKafkaStreamsConfig());
        log.debug("LittleHorse configurations: {}", config.toLittleHorseConfig());

        try {
            initializeBootstraps(config);
        } catch (Exception e) {
            log.error("Error starting application", e);
            System.exit(-1);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        Shutdown.addShutdownHook("Main Thread", latch::countDown);
        latch.await();
    }

    private static void initializeBootstraps(final CanaryConfig config) {
        final PrometheusExporterBootstrap prometheusExporterBootstrap = new PrometheusExporterBootstrap(config);

        final KafkaTopicBootstrap kafkaTopicBootstrap = new KafkaTopicBootstrap(config);
        prometheusExporterBootstrap.addMeasurable(kafkaTopicBootstrap);

        if (config.isMetronomeEnabled()) {
            final MetronomeBootstrap metronomeBootstrap = new MetronomeBootstrap(config);
            prometheusExporterBootstrap.addMeasurable(metronomeBootstrap);
        }

        if (config.isMetronomeWorkerEnabled()) {
            final MetronomeWorkerBootstrap metronomeWorkerBootstrap = new MetronomeWorkerBootstrap(config);
            prometheusExporterBootstrap.addMeasurable(metronomeWorkerBootstrap);
        }

        if (config.isAggregatorEnabled()) {
            final AggregatorBootstrap aggregatorBootstrap = new AggregatorBootstrap(config);
            prometheusExporterBootstrap.addMeasurable(aggregatorBootstrap);
        }
    }
}

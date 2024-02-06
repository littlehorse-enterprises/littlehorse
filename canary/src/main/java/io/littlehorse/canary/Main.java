package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.AggregatorBootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.KafkaTopicBootstrap;
import io.littlehorse.canary.metronome.MetronomeBootstrap;
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

        log.info("Canary configurations: {}", config);
        log.info("KafkaAdmin configurations: {}", config.toKafkaAdminConfig());
        log.info("KafkaProducer configurations: {}", config.toKafkaProducerConfig());
        log.info("KafkaStreams configurations: {}", config.toKafkaStreamsConfig());
        log.info("LittleHorse configurations: {}", config.toLittleHorseConfig());

        try {
            initializeBootstraps(config);
        } catch (Exception e) {
            log.error("Error starting application", e);
            System.exit(-1);
        }

        log.info("Canary started");

        final CountDownLatch latch = new CountDownLatch(1);
        Shutdown.addShutdownHook("Main Thread", latch::countDown);
        latch.await();
    }

    private static void initializeBootstraps(final CanaryConfig config) {
        final PrometheusExporterBootstrap prometheusExporterBootstrap = new PrometheusExporterBootstrap(
                config.getMetricsPort(), config.getMetricsPath(), config.isMetricsFilterEnabled(), config.getId());

        final KafkaTopicBootstrap kafkaTopicBootstrap = new KafkaTopicBootstrap(
                config.getTopicName(),
                config.getTopicPartitions(),
                config.getTopicReplicas(),
                config.toKafkaAdminConfig().toMap());
        prometheusExporterBootstrap.addMesurable(kafkaTopicBootstrap);

        if (config.isMetronomeEnabled()) {
            final MetronomeBootstrap metronomeBootstrap = new MetronomeBootstrap(
                    config.getTopicName(),
                    config.toKafkaProducerConfig().toMap(),
                    config.toLittleHorseConfig().toMap(),
                    config.getMetronomeFrequency(),
                    config.getMetronomeThreads(),
                    config.getMetronomeRuns());
            prometheusExporterBootstrap.addMesurable(metronomeBootstrap);
        }

        if (config.isAggregatorEnabled()) {
            final AggregatorBootstrap aggregatorBootstrap = new AggregatorBootstrap(
                    config.getTopicName(), config.toKafkaStreamsConfig().toMap());
            prometheusExporterBootstrap.addMesurable(aggregatorBootstrap);
        }
    }
}

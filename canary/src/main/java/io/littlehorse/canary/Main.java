package io.littlehorse.canary;

import io.littlehorse.canary.aggregator.AggregatorBootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.KafkaTopicBootstrap;
import io.littlehorse.canary.metronome.MetronomeBootstrap;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();

        log.debug("Canary configurations: {}", config);
        log.debug("KafkaAdmin configurations: {}", config.toKafkaAdminConfig());
        log.debug("KafkaProducer configurations: {}", config.toKafkaProducerConfig());
        log.debug("KafkaStreams configurations: {}", config.toKafkaStreamsConfig());
        log.debug("LittleHorse configurations: {}", config.toLittleHorseConfig());

        int latchSize = 1 + (config.isMetronomeEnabled() ? 1 : 0) + (config.isAggregatorEnabled() ? 1 : 0);
        CountDownLatch latch = new CountDownLatch(latchSize);

        try {
            initiateKafkaTopicBootstrap(config, latch);

            if (config.isMetronomeEnabled()) {
                initiateMetronomeBootstrap(config, latch);
            }

            if (config.isAggregatorEnabled()) {
                initiateAggregatorBootstrap(config, latch);
            }
        } catch (Exception e) {
            log.error("Error initiating application, shutting down", e);
            System.exit(-1);
        }

        log.info("Started");
        latch.await();
        log.info("Stopped");
    }

    private static void initiateAggregatorBootstrap(CanaryConfig config, CountDownLatch latch) {
        AggregatorBootstrap aggregatorBootstrap = new AggregatorBootstrap(
                config.getTopicName(), config.toKafkaStreamsConfig().toMap());
        addShutdownHook(aggregatorBootstrap, latch);
    }

    private static void initiateMetronomeBootstrap(CanaryConfig config, CountDownLatch latch) {
        MetronomeBootstrap metronomeBootstrap = new MetronomeBootstrap(
                config.getTopicName(),
                config.toKafkaProducerConfig().toMap(),
                config.toLittleHorseConfig().toMap());
        addShutdownHook(metronomeBootstrap, latch);
    }

    private static void initiateKafkaTopicBootstrap(CanaryConfig config, CountDownLatch latch) {
        KafkaTopicBootstrap kafkaTopicBootstrap = new KafkaTopicBootstrap(
                config.getTopicName(),
                config.getTopicPartitions(),
                config.getTopicReplicas(),
                config.toKafkaAdminConfig().toMap());
        addShutdownHook(kafkaTopicBootstrap, latch);
    }

    private static void addShutdownHook(Bootstrap bootstrap, CountDownLatch latch) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.trace("{} shutdown process started", bootstrap.getClass().getSimpleName());
            try {
                bootstrap.shutdown();
            } catch (Exception e) {
                log.error("Error in ShutdownHook '{}'", bootstrap.getClass().getSimpleName(), e);
            } finally {
                latch.countDown();
            }
        }));
    }
}

package io.littlehorse.canary;

import com.google.common.collect.Lists;
import io.littlehorse.canary.aggregator.AggregatorBootstrap;
import io.littlehorse.canary.app.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.KafkaBootstrap;
import io.littlehorse.canary.metronome.WorkerBootstrap;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
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

        List<Bootstrap> bootstraps = List.of(new KafkaBootstrap(), new WorkerBootstrap(), new AggregatorBootstrap());

        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.trace("Shutdown process started");
            for (Bootstrap bootstrap : Lists.reverse(bootstraps)) {
                bootstrap.shutdown();
            }
            log.trace("Shutdown process completed");
            latch.countDown();
        }));

        for (Bootstrap bootstrap : bootstraps) {
            bootstrap.initialize(config);
        }

        log.info("Started");
        latch.await();
        log.info("Stopped");
    }
}

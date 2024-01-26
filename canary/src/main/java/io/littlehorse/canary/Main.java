package io.littlehorse.canary;

import io.littlehorse.canary.app.BoostrapInitializationException;
import io.littlehorse.canary.app.Bootstrap;
import io.littlehorse.canary.config.CanaryConfig;
import io.littlehorse.canary.config.ConfigLoader;
import io.littlehorse.canary.kafka.KafkaBootstrap;
import io.littlehorse.canary.metronome.WorkerBootstrap;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) throws IOException, BoostrapInitializationException, InterruptedException {
        CanaryConfig config = args.length > 0 ? ConfigLoader.load(Paths.get(args[0])) : ConfigLoader.load();

        log.info("Canary configurations: {}", config);
        log.info("KafkaAdmin configurations: {}", config.toKafkaAdminConfig());
        log.info("LittleHorse configurations: {}", config.toLittleHorseConfig());

        List<Bootstrap> bootstraps = List.of(new KafkaBootstrap(), new WorkerBootstrap());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown process started");
            for (Bootstrap bootstrap : bootstraps) {
                bootstrap.shutdown();
            }
        }));

        for (Bootstrap bootstrap : bootstraps) {
            bootstrap.initialize(config);
        }
    }
}

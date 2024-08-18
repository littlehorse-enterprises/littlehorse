package io.littlehorse;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.LHServer;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;

@Slf4j
public class App {

    public static void doIdempotentSetup(LHServerConfig config) throws InterruptedException, ExecutionException {
        log.info("Creating topics!!");

        boolean createdATopic = false;
        for (NewTopic topic : config.getAllTopics()) {
            createdATopic = config.createKafkaTopic(topic) || createdATopic;
        }

        if (createdATopic) {
            log.info("Sleeping 5 seconds to allow topic creation to propagate");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.warn("InterruptedException was ignored");
            }
            log.info("Done creating topics");
        } else {
            log.info("Looks like all the topics already existed!");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        LHServerConfig config;

        if (args.length > 0) {
            log.info("Loading configuration from file '{}'", args[0]);
            config = new LHServerConfig(args[0]);
        } else {
            log.info("Loading configuration from env variables");
            config = new LHServerConfig();
        }

        log.info("Settings:\n{}", config);
        if (config.shouldCreateTopics()) {
            doIdempotentSetup(config);
        }

        CountDownLatch latch = new CountDownLatch(1);
        LHServer server = new LHServer(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server");
            server.close();

            // The Config manages the KafkaAdminClient, so we need to clean it up
            config.cleanup();

            latch.countDown();
        }));

        log.info("Starting the LHServer now!");
        server.start();
        latch.await();
        log.info("Done waiting for countdown latch, exiting now.");
    }
}

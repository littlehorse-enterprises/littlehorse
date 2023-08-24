package io.littlehorse.driver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class TestDriverStandalone extends TestDriver {

    private static final Logger log = LoggerFactory.getLogger(TestDriverStandalone.class);

    private KafkaContainer kafka;
    private KafkaStreamsServerImpl server;

    public TestDriverStandalone(Set<Class<?>> tests, int threads) {
        super(tests, threads);
    }

    @Override
    public void setup() throws Exception {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
        log.info("Starting kafka");
        kafka.start();
        startServer();
        workerConfig = new LHWorkerConfig();

        try {
            client = workerConfig.getBlockingStub();
        } catch (IOException exn) {
            throw new RuntimeException(exn);
        }
    }

    private void startServer() throws Exception {
        Properties serverProperties = new Properties();
        serverProperties.put(LHConfig.KAFKA_BOOTSTRAP_KEY, kafka.getBootstrapServers());
        serverProperties.put(LHConfig.KAFKA_STATE_DIR_KEY, "/tmp/" + UUID.randomUUID());
        serverProperties.put(LHConfig.CLUSTER_PARTITIONS_KEY, "3");

        LHConfig serverConfig = new LHConfig(serverProperties);

        for (NewTopic topic : serverConfig.getAllTopics()) {
            serverConfig.createKafkaTopic(topic);
        }

        // wait until topics are created
        TimeUnit.SECONDS.sleep(3);

        // run the server in another thread
        server = new KafkaStreamsServerImpl(serverConfig);

        new Thread(() -> {
                    try {
                        log.info("Starting server");
                        server.start();
                    } catch (IOException exn) {
                        throw new RuntimeException(exn);
                    }
                })
                .start();

        // wait until the server is up
        TimeUnit.SECONDS.sleep(5);
    }
}

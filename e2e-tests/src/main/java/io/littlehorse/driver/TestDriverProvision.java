package io.littlehorse.driver;

import io.littlehorse.common.LHConfig;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class TestDriverProvision extends TestDriver {

    private static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );
    CountDownLatch latch = new CountDownLatch(1);

    public TestDriverProvision(Set<Class<?>> tests, int threads) {
        super(tests, threads);
    }

    @Override
    public void arrange() throws Exception {
        kafka.start();
        startServer();
        workerConfig = new LHWorkerConfig();
        client = new LHClient(workerConfig);
    }

    private void startServer() throws Exception {
        Properties serverProperties = new Properties();
        serverProperties.put(
            LHConfig.KAFKA_BOOTSTRAP_KEY,
            kafka.getBootstrapServers()
        );
        serverProperties.put(
            LHConfig.KAFKA_STATE_DIR_KEY,
            "/tmp/" + UUID.randomUUID()
        );
        serverProperties.put(LHConfig.CLUSTER_PARTITIONS_KEY, 6);

        LHConfig serverConfig = new LHConfig(serverProperties);

        for (NewTopic topic : serverConfig.getAllTopics()) {
            serverConfig.createKafkaTopic(topic);
        }

        // wait until topics are created
        TimeUnit.SECONDS.sleep(5);

        // run the server in another thread
        KafkaStreamsServerImpl server = new KafkaStreamsServerImpl(serverConfig);

        new Thread(() -> {
            try {
                server.start();
            } catch (IOException exn) {
                throw new RuntimeException(exn);
            }
        })
            .start();

        // wait until the server is up
        TimeUnit.SECONDS.sleep(5);
    }

    @Override
    public void teardown() throws Exception {
        latch.countDown();
        TimeUnit.SECONDS.sleep(1);
        kafka.stop();
    }
}

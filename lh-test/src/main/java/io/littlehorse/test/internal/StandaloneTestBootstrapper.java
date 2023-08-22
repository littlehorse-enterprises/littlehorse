package io.littlehorse.test.internal;

import io.littlehorse.common.LHConfig;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StandaloneTestBootstrapper implements TestBootstrapper {

    private static final String LH_CONFIG_FILE = ".config/littlehorse.config";

    private LHWorkerConfig workerConfig;
    private LHClient client;

    private KafkaContainer kafka;
    private KafkaStreamsServerImpl server;

    public StandaloneTestBootstrapper() {
        try {
            setup();
        } catch (Exception e) {
            throw new LHTestInitializationException(e);
        }
    }

    public void setup() throws Exception {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
        kafka.start();
        startServer();
        workerConfig = new LHWorkerConfig();
        client = new LHClient(workerConfig);
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
                        server.start();
                    } catch (IOException exn) {
                        throw new RuntimeException(exn);
                    }
                })
                .start();

        // wait until the server is up
        TimeUnit.SECONDS.sleep(10);
    }

    @Override
    public LHWorkerConfig getWorkerConfig() {
        return workerConfig;
    }

    @Override
    public LHClient getLhClient() {
        return client;
    }
}

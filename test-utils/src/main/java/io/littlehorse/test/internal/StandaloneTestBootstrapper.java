package io.littlehorse.test.internal;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StandaloneTestBootstrapper implements TestBootstrapper {

    private LHConfig workerConfig;
    private LittleHorseBlockingStub client;

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
        workerConfig = new LHConfig(testClientProperties());
        client = workerConfig.getBlockingStub();
        startServer();
    }

    private void startServer() throws Exception {
        Properties serverProperties = new Properties();
        serverProperties.put(LHServerConfig.KAFKA_BOOTSTRAP_KEY, kafka.getBootstrapServers());
        serverProperties.put(LHServerConfig.KAFKA_STATE_DIR_KEY, "/tmp/" + UUID.randomUUID());
        serverProperties.put(LHServerConfig.CLUSTER_PARTITIONS_KEY, "3");

        LHServerConfig serverConfig = new LHServerConfig(serverProperties);

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
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .ignoreException(StatusRuntimeException.class)
                .until(() -> {
                    client.whoami(Empty.getDefaultInstance());
                    return true;
                });
    }

    @Override
    public LHConfig getWorkerConfig() {
        return workerConfig;
    }

    @Override
    public LittleHorseBlockingStub getLhClient() {
        return client;
    }

    private Properties testClientProperties() {
        Properties configs = new Properties();
        String tenantId = System.getenv().getOrDefault("LHC_TENANT_ID", "test-tenant");
        configs.put(LHConfig.TENANT_ID_KEY, tenantId);
        return configs;
    }
}

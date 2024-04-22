package io.littlehorse.test.internal;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
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
        client = workerConfig
                .getBlockingStub()
                .withCallCredentials(new MockCallCredentials(new TenantIdModel(workerConfig.getTenantId())));
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
        String tenantId = System.getenv().getOrDefault(LHConfig.TENANT_ID_KEY, "test-tenant");
        configs.put(LHConfig.TENANT_ID_KEY, tenantId);
        return configs;
    }
}

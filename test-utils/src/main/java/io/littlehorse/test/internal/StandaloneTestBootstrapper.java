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
    private KafkaStreamsServerImpl server1;
    private KafkaStreamsServerImpl server2;

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
        startServers();
    }

    private void startServers() throws Exception {
        LHServerConfig server1Config = new LHServerConfig(getServer1Config());
        LHServerConfig server2Config = new LHServerConfig(getServer2Config());

        for (NewTopic topic : server1Config.getAllTopics()) {
            server1Config.createKafkaTopic(topic);
        }

        TimeUnit.SECONDS.sleep(3);

        // run the server in another thread
        server1 = new KafkaStreamsServerImpl(server1Config);
        server2 = new KafkaStreamsServerImpl(server2Config);

        new Thread(() -> {
                    try {
                        server1.start();
                    } catch (IOException exn) {
                        throw new RuntimeException(exn);
                    }
                })
                .start();
        new Thread(() -> {
                    try {
                        server2.start();
                    } catch (IOException exn) {
                        throw new RuntimeException(exn);
                    }
                })
                .start();
    }

    private Properties getServer1Config() {
        Properties result = getBaseServerConfig();
        result.put(LHServerConfig.INTERNAL_ADVERTISED_PORT_KEY, "2011");
        result.put(LHServerConfig.INTERNAL_BIND_PORT_KEY, "2011");
        result.put(LHServerConfig.ADVERTISED_LISTENERS_KEY, "PLAIN://localhost:2023");
        result.put(LHServerConfig.LISTENERS_KEY, "PLAIN:2023");
        return result;
    }

    private Properties getServer2Config() {
        Properties result = getBaseServerConfig();
        result.put(LHServerConfig.INTERNAL_ADVERTISED_PORT_KEY, "2012");
        result.put(LHServerConfig.INTERNAL_BIND_PORT_KEY, "2012");
        result.put(LHServerConfig.ADVERTISED_LISTENERS_KEY, "PLAIN://localhost:2024");
        result.put(LHServerConfig.LISTENERS_KEY, "PLAIN:2024");
        return result;
    }

    private Properties getBaseServerConfig() {
        Properties serverProperties = new Properties();
        serverProperties.put(LHServerConfig.KAFKA_BOOTSTRAP_KEY, kafka.getBootstrapServers());
        serverProperties.put(LHServerConfig.KAFKA_STATE_DIR_KEY, "/tmp/" + UUID.randomUUID());
        serverProperties.put(LHServerConfig.CLUSTER_PARTITIONS_KEY, "12");
        serverProperties.put(LHServerConfig.CORE_STREAM_THREADS_KEY, "2");
        serverProperties.put(LHServerConfig.LHS_CLUSTER_ID_KEY, "e2e-test-cluster");
        serverProperties.put(LHServerConfig.SHOULD_CREATE_TOPICS_KEY, "false");
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY, 1024L * 1024L * 50);
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY, 1024L * 1024L * 50);
        return serverProperties;
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

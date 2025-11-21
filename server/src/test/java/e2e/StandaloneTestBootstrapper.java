package e2e;

import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.server.LHServer;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.TestBootstrapper;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StandaloneTestBootstrapper implements TestBootstrapper {

    private LHConfig workerConfig;

    private KafkaContainer kafka;
    private LHServer server1;
    private LHServer server2;

    public StandaloneTestBootstrapper() {
        try {
            setup();
        } catch (Exception e) {
            throw new LHTestInitializationException(e);
        }
    }

    public void setup() throws Exception {
        kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.0"));
        kafka.withCreateContainerCmdModifier(cmd -> {
            cmd.getHostConfig().withMemory(1024L * 1024L * 1024L * 1); // 1 GB for Kafka
        });
        kafka.start();
        workerConfig = new LHConfig(testClientProperties());
        startServers();

        // Enable the output topic by default.
        Awaitility.await().ignoreException(StatusRuntimeException.class).until(() -> {
            LittleHorseBlockingStub client = workerConfig.getBlockingStub();
            client.putTenant(PutTenantRequest.newBuilder()
                    .setId(workerConfig.getTenantId().getId())
                    .setOutputTopicConfig(OutputTopicConfig.newBuilder()
                            .setDefaultRecordingLevel(OutputTopicRecordingLevel.ALL_ENTITY_EVENTS)
                            .build())
                    .build());
            return true;
        });
    }

    private void startServers() throws Exception {
        LHServerConfig server1Config = new LHServerConfig(getServer1Config());
        LHServerConfig server2Config = new LHServerConfig(getServer2Config());

        for (NewTopic topic : server1Config.getAllTopics()) {
            server1Config.createKafkaTopic(topic);
        }

        // Create output topic because it's gonna be enabled
        Pair<NewTopic, NewTopic> outputTopics = server1Config.getOutputTopicsFor(
                new TenantModel(workerConfig.getTenantId().getId()));
        server1Config.createKafkaTopic(outputTopics.getLeft());
        server1Config.createKafkaTopic(outputTopics.getRight());

        TimeUnit.SECONDS.sleep(4);

        // run the server in another thread
        server1 = new LHServer(server1Config);
        server2 = new LHServer(server2Config);

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
        serverProperties.put(LHServerConfig.CORE_MEMTABLE_SIZE_BYTES_KEY, String.valueOf(1024L * 1024L * 8));
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY, String.valueOf(1024L * 1024L * 100));
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY, String.valueOf(1024L * 1024L * 100));
        serverProperties.put(LHServerConfig.X_ENABLE_STRUCT_DEFS_KEY, "true");
        serverProperties.put(LHServerConfig.X_MAX_DELETES_PER_COMMAND_KEY, "10"); // To test wfrun deletion iteration
        return serverProperties;
    }

    @Override
    public LHConfig getWorkerConfig() {
        return workerConfig;
    }

    private Properties testClientProperties() {
        Properties configs = new Properties();
        String tenantId = System.getenv().getOrDefault(LHConfig.TENANT_ID_KEY, LHConstants.DEFAULT_TENANT);
        configs.put(LHConfig.TENANT_ID_KEY, tenantId);
        return configs;
    }
}

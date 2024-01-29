package io.littlehorse.test.internal;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
    private static final long MEGABYTE = 1024L * 1024L;

    private KafkaContainer kafka;
    private List<KafkaStreamsServerImpl> servers;

    public StandaloneTestBootstrapper() {
        this.servers = new ArrayList<>();
        try {
            setup();
        } catch (Exception e) {
            throw new LHTestInitializationException(e);
        }
    }

    public void setup() throws Exception {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

        kafka.withKraft()
                .withEnv("KAFKA_NUM_NETWORK_THREADS", "2")
                .withEnv("KAFKA_NUM_BACKGROUND_THREADS", "2")
                .withEnv("KAFKA_NUM_IO_THREADS", "2")
                .start();
        workerConfig = new LHConfig();
        client = workerConfig.getBlockingStub();
        startServer();
    }

    private void startServer() throws Exception {
        Properties props1 = getServerProps(2023, 2011, 1822);
        Properties props2 = getServerProps(2024, 2012, 1833);

        LHServerConfig server1Config = new LHServerConfig(props1);
        LHServerConfig server2Config = new LHServerConfig(props2);

        for (NewTopic topic : server1Config.getAllTopics()) {
            server1Config.createKafkaTopic(topic);
        }

        // wait until topics are created
        TimeUnit.SECONDS.sleep(3);

        // run the server in another thread
        servers.add(new KafkaStreamsServerImpl(server1Config));
        servers.add(new KafkaStreamsServerImpl(server2Config));

        for (KafkaStreamsServerImpl server : servers) {
            new Thread(() -> {
                        try {
                            server.start();
                        } catch (IOException exn) {
                            throw new RuntimeException(exn);
                        }
                    })
                    .start();
        }

        // wait until at least the first server is up. TODO: Wait for second too.
        Awaitility.await().atMost(Duration.ofSeconds(20)).ignoreException(StatusRuntimeException.class).until(() -> {
            String randomTaskDefName = UUID.randomUUID().toString();
            try {
                client.putTaskDef(PutTaskDefRequest.newBuilder().setName(randomTaskDefName).build());
                client.whoami(Empty.getDefaultInstance());
            } catch(Exception exn) {
                exn.printStackTrace();
                throw exn;
            }
            return true;
        });
    }

    private Properties getServerProps(int externalPort, int internalPort, int metricsPort) {
        Properties serverProperties = new Properties();
        serverProperties.put(LHServerConfig.KAFKA_BOOTSTRAP_KEY, kafka.getBootstrapServers());
        serverProperties.put(LHServerConfig.KAFKA_STATE_DIR_KEY, "/tmp/" + UUID.randomUUID());
        serverProperties.put(LHServerConfig.CLUSTER_PARTITIONS_KEY, "3");
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_BLOCK_CACHE_BYTES_KEY, String.valueOf(MEGABYTE * 32));
        serverProperties.put(LHServerConfig.ROCKSDB_TOTAL_MEMTABLE_BYTES_KEY, String.valueOf(MEGABYTE * 32));
        serverProperties.put(LHServerConfig.TIMER_STATESTORE_CACHE_BYTES_KEY, String.valueOf(MEGABYTE * 32));
        serverProperties.put(LHServerConfig.CORE_STATESTORE_CACHE_BYTES_KEY, String.valueOf(MEGABYTE * 32));

        serverProperties.put(LHServerConfig.INTERNAL_BIND_PORT_KEY, String.valueOf(internalPort));
        serverProperties.put(LHServerConfig.INTERNAL_ADVERTISED_PORT_KEY, String.valueOf(internalPort));
        serverProperties.put(LHServerConfig.INTERNAL_ADVERTISED_HOST_KEY, "localhost");
        serverProperties.put(LHServerConfig.LISTENERS_KEY, "PLAIN:%d".formatted(externalPort));
        serverProperties.put(LHServerConfig.ADVERTISED_LISTENERS_KEY, "PLAIN://localhost:%d".formatted(externalPort));
        serverProperties.put(LHServerConfig.HEALTH_SERVICE_PORT_KEY, metricsPort);

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
}

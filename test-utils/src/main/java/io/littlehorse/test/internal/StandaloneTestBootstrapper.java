package io.littlehorse.test.internal;

import com.google.protobuf.Empty;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.server.KafkaStreamsServerImpl;
import io.littlehorse.server.auth.ServerAuthorizer;
import io.littlehorse.test.exception.LHTestInitializationException;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.Awaitility;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class StandaloneTestBootstrapper implements TestBootstrapper {

    private LHConfig workerConfig;
    private LittleHorseBlockingStub client;
    private LittleHorseBlockingStub anonymousClient;

    private KafkaContainer kafka;
    private KafkaStreamsServerImpl server;

    public StandaloneTestBootstrapper() {
        this(null);
    }

    public StandaloneTestBootstrapper(PrincipalIdModel principalId) {
        try {
            setup(principalId);
        } catch (Exception e) {
            throw new LHTestInitializationException(e);
        }
    }

    public void setup(PrincipalIdModel principalId) throws Exception {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
        kafka.start();
        workerConfig = new LHConfig(testClientProperties());
        anonymousClient = workerConfig.getBlockingStub();
        if (principalId != null) {
            client = workerConfig
                    .getBlockingStub()
                    .withCallCredentials(
                            new MockCallCredentials(principalId, new TenantIdModel(workerConfig.getTenantId())));
        } else {
            client = anonymousClient;
        }
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
                .atMost(Duration.ofSeconds(30))
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

    @Override
    public LittleHorseBlockingStub getAnonymousClient() {
        return anonymousClient;
    }

    private Properties testClientProperties() {
        Properties configs = new Properties();
        String tenantId = System.getenv().getOrDefault(LHConfig.TENANT_ID_KEY, "test-tenant");
        configs.put(LHConfig.TENANT_ID_KEY, tenantId);
        return configs;
    }

    private static final class MockCallCredentials extends CallCredentials {

        private final PrincipalIdModel principalId;
        private final TenantIdModel tenantId;

        MockCallCredentials(final PrincipalIdModel principalId, final TenantIdModel tenantId) {
            this.principalId = principalId;
            this.tenantId = tenantId;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
            executor.execute(() -> {
                try {
                    Metadata headers = new Metadata();
                    headers.put(ServerAuthorizer.CLIENT_ID, principalId.getId());
                    headers.put(ServerAuthorizer.TENANT_ID, tenantId.getId());
                    metadataApplier.apply(headers);
                } catch (Exception e) {
                    metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                }
            });
        }
    }
}

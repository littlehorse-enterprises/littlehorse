package io.littlehorse.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.littlehorse.common.LHConfig;
import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import java.io.IOException;

public class LHServer {

    private LHConfig config;
    private KafkaStreamsBackend backend;
    private Server grpcServer;

    public LHServer(LHConfig config) {
        this.config = config;

        // Hypothetically we could implement different backends in the future...
        // perhaps a Pulsar/Cassandra/Yugabyte backend.
        backend = new KafkaStreamsBackend();

        HealthStatusManager grpcHealthCheckThingy = new HealthStatusManager();
        backend.init(this.config, grpcHealthCheckThingy);

        this.grpcServer =
            ServerBuilder
                .forPort(config.getApiBindPort())
                .addService(backend)
                .addService(grpcHealthCheckThingy.getHealthService())
                .build();
    }

    public void start() throws IOException {
        backend.start();
        grpcServer.start();
    }

    public void close() {
        grpcServer.shutdown();
        backend.close();
    }

    public static void doMain(LHConfig config) throws IOException {
        LHServer server = new LHServer(config);
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    server.close();
                    config.cleanup();
                })
            );
        server.start();
    }
}

package io.littlehorse.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.GetExternalEventDefPb;
import io.littlehorse.common.proto.GetExternalEventDefReplyPb;
import io.littlehorse.common.proto.GetTaskDefPb;
import io.littlehorse.common.proto.GetTaskDefReplyPb;
import io.littlehorse.common.proto.GetWfSpecPb;
import io.littlehorse.common.proto.GetWfSpecReplyPb;
import io.littlehorse.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import java.io.IOException;

public class LHServer extends LHPublicApiImplBase {

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
                .addService(this)
                .addService(grpcHealthCheckThingy.getHealthService())
                .build();
    }

    @Override
    public void getWfSpec(GetWfSpecPb req, StreamObserver<GetWfSpecReplyPb> ctx) {
        GetWfSpecReplyPb.Builder out = GetWfSpecReplyPb.newBuilder();

        try {
            WfSpec spec = backend.getWfSpec(
                req.getName(),
                req.hasVersion() ? req.getVersion() : null
            );
            if (spec == null) {
                out.setMessage("Couldn't find specified WfSpec.");
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setResult(spec.toProto());
                out.setCode(LHResponseCodePb.OK);
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Had an internal connection error: " + exn.getMessage());
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }

    @Override
    public void getTaskDef(GetTaskDefPb req, StreamObserver<GetTaskDefReplyPb> ctx) {
        GetTaskDefReplyPb.Builder out = GetTaskDefReplyPb.newBuilder();

        try {
            TaskDef spec = backend.getTaskDef(
                req.getName(),
                req.hasVersion() ? req.getVersion() : null
            );
            if (spec == null) {
                out.setMessage("Couldn't find specified TaskDef.");
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setResult(spec.toProto());
                out.setCode(LHResponseCodePb.OK);
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Had an internal connection error: " + exn.getMessage());
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }

    @Override
    public void getExternalEventDef(
        GetExternalEventDefPb req,
        StreamObserver<GetExternalEventDefReplyPb> ctx
    ) {
        GetExternalEventDefReplyPb.Builder out = GetExternalEventDefReplyPb.newBuilder();

        try {
            ExternalEventDef spec = backend.getExternalEventDef(
                req.getName(),
                req.hasVersion() ? req.getVersion() : null
            );
            if (spec == null) {
                out.setMessage("Couldn't find specified ExternalEventDef.");
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setResult(spec.toProto());
                out.setCode(LHResponseCodePb.OK);
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Had an internal connection error: " + exn.getMessage());
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
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

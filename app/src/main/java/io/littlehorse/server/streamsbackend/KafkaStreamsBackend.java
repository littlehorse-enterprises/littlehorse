package io.littlehorse.server.streamsbackend;

import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.proto.GetWfSpecPb;
import io.littlehorse.common.proto.GetWfSpecReplyPb;
import io.littlehorse.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.LHKafkaStoreInternalCommServer;
import java.io.IOException;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.Topology;

public class KafkaStreamsBackend extends LHPublicApiImplBase {

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;
    private KafkaStreams tagStreams;

    private LHKafkaStoreInternalCommServer backendInternalComms;

    public void init(LHConfig config, HealthStatusManager grpcHealthCheckThingy) {
        Topology coreTopo = ServerTopology.initCoreTopology(config);
        Topology timerTopo = ServerTopology.initTimerTopology(config);
        Topology taggingTopo = ServerTopology.initTaggingTopology(config);

        coreStreams = new KafkaStreams(coreTopo, config.getStreamsConfig("core"));
        timerStreams = new KafkaStreams(timerTopo, config.getStreamsConfig("timer"));
        tagStreams = new KafkaStreams(taggingTopo, config.getStreamsConfig("tag"));

        coreStreams.setStateListener(
            new LHBackendStateListener("core", grpcHealthCheckThingy)
        );
        timerStreams.setStateListener(
            new LHBackendStateListener("timer", grpcHealthCheckThingy)
        );
        tagStreams.setStateListener(
            new LHBackendStateListener("tag", grpcHealthCheckThingy)
        );

        backendInternalComms =
            new LHKafkaStoreInternalCommServer(config, coreStreams);
    }

    public void start() throws IOException {
        coreStreams.start();
        timerStreams.start();
        tagStreams.start();
        backendInternalComms.start();
    }

    public void close() {
        coreStreams.close();
        timerStreams.close();
        tagStreams.close();
        backendInternalComms.close();
    }

    @Override
    public void getWfSpec(GetWfSpecPb req, StreamObserver<GetWfSpecReplyPb> ctx) {
        GetWfSpecReplyPb.Builder out = GetWfSpecReplyPb.newBuilder();
        try {
            // TODO: first we need to fill out the commands so we know where
            // to go to look for the wfspecs.
            System.out.println("hi");
        } catch (Exception exn) {
            // TODO;
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }
}

class LHBackendStateListener implements StateListener {

    private String componentName;
    private HealthStatusManager grpcHealthCheckThingy;

    public LHBackendStateListener(
        String componentName,
        HealthStatusManager grpcHealthCheckThingy
    ) {
        this.componentName = componentName;
        this.grpcHealthCheckThingy = grpcHealthCheckThingy;
    }

    public void onChange(State newState, State oldState) {
        LHUtil.log("New state for", componentName, ":", newState);
        if (newState == State.RUNNING) {
            grpcHealthCheckThingy.setStatus(componentName, ServingStatus.SERVING);
        } else {
            grpcHealthCheckThingy.setStatus(componentName, ServingStatus.NOT_SERVING);
        }
    }
}

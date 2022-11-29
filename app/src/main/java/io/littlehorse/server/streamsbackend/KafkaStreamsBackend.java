package io.littlehorse.server.streamsbackend;

import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.LHKafkaStoreInternalCommServer;
import java.io.IOException;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.Topology;

public class KafkaStreamsBackend {

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;
    private KafkaStreams tagStreams;
    private LHConfig config;

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

        this.config = config;

        backendInternalComms =
            new LHKafkaStoreInternalCommServer(config, coreStreams);
    }

    public WfSpec getWfSpec(String name, Integer version) throws LHConnectionError {
        Bytes specBytes = null;
        String partitionKey = LHConstants.META_PARTITION_KEY;
        if (version == null) {
            specBytes =
                backendInternalComms.getLastFromPrefix(
                    WfSpec.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                backendInternalComms.getBytes(
                    WfSpec.getFullKey(name, version),
                    partitionKey
                );
        }

        if (specBytes == null) {
            return null;
        } else {
            try {
                return LHSerializable.fromBytes(
                    specBytes.get(),
                    WfSpec.class,
                    config
                );
            } catch (LHSerdeError exn) {
                throw new LHConnectionError(
                    exn,
                    "Unexpected failure to read response"
                );
            }
        }
    }

    public TaskDef getTaskDef(String name, Integer version) throws LHConnectionError {
        Bytes specBytes = null;
        String partitionKey = LHConstants.META_PARTITION_KEY;
        if (version == null) {
            specBytes =
                backendInternalComms.getLastFromPrefix(
                    TaskDef.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                backendInternalComms.getBytes(
                    TaskDef.getFullKey(name, version),
                    partitionKey
                );
        }

        if (specBytes == null) {
            return null;
        } else {
            try {
                return LHSerializable.fromBytes(
                    specBytes.get(),
                    TaskDef.class,
                    config
                );
            } catch (LHSerdeError exn) {
                throw new LHConnectionError(
                    exn,
                    "Unexpected failure to read response"
                );
            }
        }
    }

    public ExternalEventDef getExternalEventDef(String name, Integer version)
        throws LHConnectionError {
        Bytes specBytes = null;
        String partitionKey = LHConstants.META_PARTITION_KEY;
        if (version == null) {
            specBytes =
                backendInternalComms.getLastFromPrefix(
                    ExternalEventDef.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                backendInternalComms.getBytes(
                    ExternalEventDef.getFullKey(name, version),
                    partitionKey
                );
        }

        if (specBytes == null) {
            return null;
        } else {
            try {
                return LHSerializable.fromBytes(
                    specBytes.get(),
                    ExternalEventDef.class,
                    config
                );
            } catch (LHSerdeError exn) {
                throw new LHConnectionError(
                    exn,
                    "Unexpected failure to read response"
                );
            }
        }
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

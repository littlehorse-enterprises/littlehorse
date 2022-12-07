package io.littlehorse.server.streamsbackend;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.GetExternalEventPb;
import io.littlehorse.common.proto.GetExternalEventReplyPb;
import io.littlehorse.common.proto.GetNodeRunPb;
import io.littlehorse.common.proto.GetNodeRunReplyPb;
import io.littlehorse.common.proto.GetVariablePb;
import io.littlehorse.common.proto.GetVariableReplyPb;
import io.littlehorse.common.proto.GetWfRunPb;
import io.littlehorse.common.proto.GetWfRunReplyPb;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PaginatedTagQueryPb;
import io.littlehorse.common.proto.PaginatedTagQueryReplyPb;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.proto.SearchWfRunPb;
import io.littlehorse.common.proto.SearchWfRunReplyPb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.storeinternals.BackendInternalComms;
import io.littlehorse.server.streamsbackend.storeinternals.index.Tag;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;
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

    private BackendInternalComms internalComms;
    private LHProducer producer;

    public static final String DISCRETE_TAG_COUNT_PREFIX = "DiscreteTagCount/";
    public static final String DISCRETE_TAG_UPDATES_KEY = "DiscreteTagUpdates";

    public void init(LHConfig config, HealthStatusManager grpcHealthCheckThingy) {
        Topology coreTopo = ServerTopology.initCoreTopology(config);
        // Topology timerTopo = ServerTopology.initTimerTopology(config);
        // Topology taggingTopo = ServerTopology.initTaggingTopology(config);

        coreStreams = new KafkaStreams(coreTopo, config.getStreamsConfig("core"));
        // timerStreams = new KafkaStreams(timerTopo, config.getStreamsConfig("timer"));
        // tagStreams = new KafkaStreams(taggingTopo, config.getStreamsConfig("tag"));

        coreStreams.setStateListener(
            new LHBackendStateListener("core", grpcHealthCheckThingy)
        );
        // timerStreams.setStateListener(
        //     new LHBackendStateListener("timer", grpcHealthCheckThingy)
        // );
        // tagStreams.setStateListener(
        //     new LHBackendStateListener("tag", grpcHealthCheckThingy)
        // );

        this.config = config;
        this.producer = new LHProducer(config, false);

        internalComms = new BackendInternalComms(config, coreStreams);
    }

    public WfSpec getWfSpec(String name, Integer version) throws LHConnectionError {
        Bytes specBytes = null;
        String partitionKey = LHConstants.META_PARTITION_KEY;
        if (version == null) {
            specBytes =
                internalComms.getLastFromPrefix(
                    WfSpec.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                internalComms.getBytes(
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
                internalComms.getLastFromPrefix(
                    TaskDef.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                internalComms.getBytes(
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
                internalComms.getLastFromPrefix(
                    ExternalEventDef.getPrefixByName(name),
                    partitionKey
                );
        } else {
            specBytes =
                internalComms.getBytes(
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

    public <U extends MessageOrBuilder, T extends AbstractResponse<U>> T process(
        SubCommand<?> subCmd,
        Class<T> cls
    ) {
        if (!subCmd.hasResponse()) {
            throw new RuntimeException(
                "Not possible; expected only respondable commands."
            );
        }

        T out;
        try {
            out = cls.getDeclaredConstructor().newInstance();
        } catch (Exception exn) {
            // Not possible
            exn.printStackTrace();
            throw new RuntimeException(exn);
        }

        Command command = new Command();
        command.time = new Date();
        command.setSubCommand(subCmd);

        // TODO: allow client to set this on request to enable idempotent retries.
        command.commandId = LHUtil.generateGuid();

        // Now we need to record the command and wait for the processing.
        Future<RecordMetadata> rec = producer.send(
            command.getPartitionKey(), // partition key
            command, // payload
            config.getCoreCmdTopicName() // topic name
        );

        // Wait for the record to commit to kafka
        try {
            rec.get();
        } catch (Exception exn) {
            out.code = LHResponseCodePb.CONNECTION_ERROR;
            out.message = "May have failed recording event: " + exn.getMessage();
            return out;
        }

        // Now we make the call to wait for the processing on the correct node.
        try {
            Bytes raw = internalComms.waitForProcessing(command);
            if (raw == null) {
                return null;
            } else {
                try {
                    // This is if everything goes according to plan.
                    return LHSerializable.fromBytes(raw.get(), cls, config);
                } catch (LHSerdeError exn) {
                    out.code = LHResponseCodePb.CONNECTION_ERROR;
                    out.message = "Got an unreadable response: " + exn.getMessage();
                }
            }
        } catch (LHConnectionError exn) {
            out.code = LHResponseCodePb.CONNECTION_ERROR;
            out.message = "Request recorded, status pending: " + exn.getMessage();
        }
        return out;
    }

    public GetWfRunReplyPb getWfRun(GetWfRunPb req) {
        String partitionKey = req.getId();
        String storeKey = StoreUtils.getFullStoreKey(req.getId(), WfRun.class);

        GetWfRunReplyPb.Builder out = GetWfRunReplyPb.newBuilder();
        try {
            Bytes resp = internalComms.getBytes(storeKey, partitionKey);
            if (resp == null) {
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setCode(LHResponseCodePb.OK);
                out.setResult(
                    LHSerializable
                        .fromBytes(resp.get(), WfRun.class, config)
                        .toProto()
                );
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Failed connecting to backend: " + exn.getMessage());
        } catch (LHSerdeError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage(
                "Got an invalid response from backend: " + exn.getMessage()
            );
        }

        return out.build();
    }

    public GetNodeRunReplyPb getNodeRun(GetNodeRunPb req) {
        String partitionKey = req.getWfRunId();
        String fullStoreKey = StoreUtils.getFullStoreKey(
            NodeRun.getStoreKey(
                req.getWfRunId(),
                req.getThreadRunNumber(),
                req.getPosition()
            ),
            NodeRun.class
        );

        GetNodeRunReplyPb.Builder out = GetNodeRunReplyPb.newBuilder();
        try {
            Bytes resp = internalComms.getBytes(fullStoreKey, partitionKey);
            if (resp == null) {
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setCode(LHResponseCodePb.OK);
                out.setResult(
                    LHSerializable
                        .fromBytes(resp.get(), NodeRun.class, config)
                        .toProto()
                );
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Failed connecting to backend: " + exn.getMessage());
        } catch (LHSerdeError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage(
                "Got an invalid response from backend: " + exn.getMessage()
            );
        }

        return out.build();
    }

    public GetVariableReplyPb getVariable(GetVariablePb req) {
        String partitionKey = req.getWfRunId();
        String fullStoreKey = StoreUtils.getFullStoreKey(
            Variable.getStoreKey(
                req.getWfRunId(),
                req.getThreadRunNumber(),
                req.getVarName()
            ),
            Variable.class
        );

        GetVariableReplyPb.Builder out = GetVariableReplyPb.newBuilder();
        try {
            Bytes resp = internalComms.getBytes(fullStoreKey, partitionKey);
            if (resp == null) {
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setCode(LHResponseCodePb.OK);
                out.setResult(
                    LHSerializable
                        .fromBytes(resp.get(), Variable.class, config)
                        .toProto()
                );
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Failed connecting to backend: " + exn.getMessage());
        } catch (LHSerdeError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage(
                "Got an invalid response from backend: " + exn.getMessage()
            );
        }

        return out.build();
    }

    public GetExternalEventReplyPb getExternalEvent(GetExternalEventPb req) {
        String partitionKey = req.getWfRunId();
        String fullStoreKey = StoreUtils.getFullStoreKey(
            ExternalEvent.getStoreKey(
                req.getWfRunId(),
                req.getExternalEventDefName(),
                req.getGuid()
            ),
            Variable.class
        );

        GetExternalEventReplyPb.Builder out = GetExternalEventReplyPb.newBuilder();
        try {
            Bytes resp = internalComms.getBytes(fullStoreKey, partitionKey);
            if (resp == null) {
                out.setCode(LHResponseCodePb.NOT_FOUND_ERROR);
            } else {
                out.setCode(LHResponseCodePb.OK);
                out.setResult(
                    LHSerializable
                        .fromBytes(resp.get(), ExternalEvent.class, config)
                        .toProto()
                );
            }
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Failed connecting to backend: " + exn.getMessage());
        } catch (LHSerdeError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage(
                "Got an invalid response from backend: " + exn.getMessage()
            );
        }

        return out.build();
    }

    public SearchWfRunReplyPb searchWfRun(SearchWfRunPb req) {
        SearchWfRunReplyPb.Builder out = SearchWfRunReplyPb.newBuilder();

        BookmarkPb bookmark;
        if (req.hasBookmark()) {
            try {
                bookmark = BookmarkPb.parseFrom(req.getBookmark());
            } catch (InvalidProtocolBufferException exn) {
                out.setCode(LHResponseCodePb.BAD_REQUEST_ERROR);
                out.setMessage("Invalid bookmark provided: " + exn.getMessage());
                return out.build();
            }
        } else {
            bookmark = null;
        }

        // Now we need to fill out the actual result object ids. Basically, we need
        // to convert the incoming request to a PaginatedTagQuery.
        int limit = req.hasLimit() ? req.getLimit() : LHConstants.DEFAULT_LIMIT;

        PaginatedTagQueryPb internalQuery = PaginatedTagQueryPb
            .newBuilder()
            .setBookmark(bookmark)
            .setLimit(limit)
            .setFullTagAttributes(Tag.getTagPrefix(req))
            .setType(GETableClassEnumPb.WF_RUN)
            .build();

        try {
            PaginatedTagQueryReplyPb raw = internalComms.doPaginatedTagQuery(
                internalQuery
            );
            out.setCode(LHResponseCodePb.OK);
            if (raw.hasUpdatedBookmark()) {
                out.setBookmark(raw.getUpdatedBookmark().toByteString());
            }
            for (String wfRunId : raw.getObjectIdsList()) {
                out.addWfRunIds(wfRunId);
            }
        } catch (LHConnectionError exn) {
            out
                .setCode(LHResponseCodePb.CONNECTION_ERROR)
                .setMessage("Failed connecting to backend: " + exn.getMessage());
        }
        return out.build();
    }

    public PollTaskReplyPb pollTask(PollTaskPb req) {
        return null;
    }

    public void start() throws IOException {
        coreStreams.start();
        // timerStreams.start();
        // tagStreams.start();
        internalComms.start();
    }

    public void close() {
        coreStreams.close();
        timerStreams.close();
        tagStreams.close();
        internalComms.close();
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
        LHUtil.log(new Date(), "New state for", componentName + ":", newState);
        if (newState == State.RUNNING) {
            grpcHealthCheckThingy.setStatus(componentName, ServingStatus.SERVING);
        } else {
            grpcHealthCheckThingy.setStatus(componentName, ServingStatus.NOT_SERVING);
        }
    }
}

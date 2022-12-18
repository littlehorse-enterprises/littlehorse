package io.littlehorse.server;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommand.DeleteWfRun;
import io.littlehorse.common.model.command.subcommand.PutExternalEvent;
import io.littlehorse.common.model.command.subcommand.PutExternalEventDef;
import io.littlehorse.common.model.command.subcommand.PutTaskDef;
import io.littlehorse.common.model.command.subcommand.PutWfSpec;
import io.littlehorse.common.model.command.subcommand.ResumeWfRun;
import io.littlehorse.common.model.command.subcommand.RunWf;
import io.littlehorse.common.model.command.subcommand.StopWfRun;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.command.subcommandresponse.DeleteWfRunReply;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventDefReply;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventReply;
import io.littlehorse.common.model.command.subcommandresponse.PutTaskDefReply;
import io.littlehorse.common.model.command.subcommandresponse.PutWfSpecReply;
import io.littlehorse.common.model.command.subcommandresponse.ReportTaskReply;
import io.littlehorse.common.model.command.subcommandresponse.ResumeWfRunReply;
import io.littlehorse.common.model.command.subcommandresponse.RunWfReply;
import io.littlehorse.common.model.command.subcommandresponse.StopWfRunReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.DeleteWfRunPb;
import io.littlehorse.common.proto.DeleteWfRunReplyPb;
import io.littlehorse.common.proto.GetExternalEventDefPb;
import io.littlehorse.common.proto.GetExternalEventDefReplyPb;
import io.littlehorse.common.proto.GetExternalEventPb;
import io.littlehorse.common.proto.GetExternalEventReplyPb;
import io.littlehorse.common.proto.GetNodeRunPb;
import io.littlehorse.common.proto.GetNodeRunReplyPb;
import io.littlehorse.common.proto.GetTaskDefPb;
import io.littlehorse.common.proto.GetTaskDefReplyPb;
import io.littlehorse.common.proto.GetVariablePb;
import io.littlehorse.common.proto.GetVariableReplyPb;
import io.littlehorse.common.proto.GetWfRunPb;
import io.littlehorse.common.proto.GetWfRunReplyPb;
import io.littlehorse.common.proto.GetWfSpecPb;
import io.littlehorse.common.proto.GetWfSpecReplyPb;
import io.littlehorse.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.proto.PutExternalEventDefPb;
import io.littlehorse.common.proto.PutExternalEventDefReplyPb;
import io.littlehorse.common.proto.PutExternalEventPb;
import io.littlehorse.common.proto.PutExternalEventReplyPb;
import io.littlehorse.common.proto.PutTaskDefPb;
import io.littlehorse.common.proto.PutTaskDefReplyPb;
import io.littlehorse.common.proto.PutWfSpecPb;
import io.littlehorse.common.proto.PutWfSpecReplyPb;
import io.littlehorse.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.common.proto.ReportTaskReplyPb;
import io.littlehorse.common.proto.ResumeWfRunPb;
import io.littlehorse.common.proto.ResumeWfRunReplyPb;
import io.littlehorse.common.proto.RunWfPb;
import io.littlehorse.common.proto.RunWfReplyPb;
import io.littlehorse.common.proto.SearchWfRunPb;
import io.littlehorse.common.proto.SearchWfRunReplyPb;
import io.littlehorse.common.proto.StopWfRunPb;
import io.littlehorse.common.proto.StopWfRunReplyPb;
import io.littlehorse.common.proto.TaskResultEventPb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsbackend.BackendInternalComms;
import io.littlehorse.server.streamsbackend.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.utils.StoreUtils;
import io.littlehorse.server.streamsbackend.taskqueue.GodzillaTaskQueueManager;
import io.littlehorse.server.streamsbackend.taskqueue.TaskQueueStreamObserver;
import io.littlehorse.server.streamsbackend.util.LHAsyncWaiter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;

public class LHServer extends LHPublicApiImplBase {

    private LHConfig config;
    // private KafkaStreamsBackend backend;
    private Server grpcServer;
    private GodzillaTaskQueueManager godzilla;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    private BackendInternalComms internalComms;
    private LHProducer producer;

    private ConcurrentHashMap<String, LHAsyncWaiter<?>> asyncWaiters;

    public LHServer(LHConfig config) {
        this.config = config;

        HealthStatusManager grpcHealthCheckThingy = new HealthStatusManager();
        this.godzilla = new GodzillaTaskQueueManager(this);

        coreStreams =
            new KafkaStreams(
                ServerTopology.initCoreTopology(config, godzilla),
                config.getStreamsConfig("core")
            );
        timerStreams =
            new KafkaStreams(
                ServerTopology.initTimerTopology(config),
                config.getStreamsConfig("timer")
            );

        this.grpcServer =
            ServerBuilder
                .forPort(config.getApiBindPort())
                .addService(this)
                .addService(grpcHealthCheckThingy.getHealthService())
                .build();
        coreStreams.setStateListener(
            new LHBackendStateListener("core", grpcHealthCheckThingy)
        );
        timerStreams.setStateListener(
            new LHBackendStateListener("timer", grpcHealthCheckThingy)
        );
        internalComms = new BackendInternalComms(config, coreStreams);

        this.producer = new LHProducer(config, false);
        this.asyncWaiters = new ConcurrentHashMap<>();
    }

    @Override
    public void getWfSpec(GetWfSpecPb req, StreamObserver<GetWfSpecReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            WfSpec.class,
            GetWfSpecReplyPb.class,
            config
        );

        if (req.hasVersion()) {
            internalComms.getBytesAsync(
                StoreUtils.getFullStoreKey(
                    WfSpec.getSubKey(req.getName(), req.getVersion()),
                    WfSpec.class
                ),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        } else {
            internalComms.getBytesAsync(
                WfSpec.getFullPrefixByName(req.getName()),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        }
    }

    @Override
    public void getTaskDef(GetTaskDefPb req, StreamObserver<GetTaskDefReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            TaskDef.class,
            GetTaskDefReplyPb.class,
            config
        );

        if (req.hasVersion()) {
            internalComms.getBytesAsync(
                StoreUtils.getFullStoreKey(
                    TaskDef.getSubKey(req.getName(), req.getVersion()),
                    TaskDef.class
                ),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        } else {
            internalComms.getBytesAsync(
                TaskDef.getFullPrefixByName(req.getName()),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        }
    }

    @Override
    public void getExternalEventDef(
        GetExternalEventDefPb req,
        StreamObserver<GetExternalEventDefReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            ExternalEventDef.class,
            GetExternalEventDefReplyPb.class,
            config
        );

        if (req.hasVersion()) {
            internalComms.getBytesAsync(
                StoreUtils.getFullStoreKey(
                    ExternalEventDef.getSubKey(req.getName(), req.getVersion()),
                    ExternalEventDef.class
                ),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        } else {
            internalComms.getBytesAsync(
                ExternalEventDef.getFullPrefixByName(req.getName()),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        }
    }

    @Override
    public void putTaskDef(PutTaskDefPb req, StreamObserver<PutTaskDefReplyPb> ctx) {
        PutTaskDef ptd = PutTaskDef.fromProto(req);
        PutTaskDefReply response = backend.process(ptd, PutTaskDefReply.class);
        ctx.onNext(response.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void putExternalEvent(
        PutExternalEventPb req,
        StreamObserver<PutExternalEventReplyPb> ctx
    ) {
        PutExternalEvent peed = PutExternalEvent.fromProto(req);
        PutExternalEventReply response = backend.process(
            peed,
            PutExternalEventReply.class
        );
        ctx.onNext(response.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void putExternalEventDef(
        PutExternalEventDefPb req,
        StreamObserver<PutExternalEventDefReplyPb> ctx
    ) {
        PutExternalEventDef peed = PutExternalEventDef.fromProto(req);
        PutExternalEventDefReply response = backend.process(
            peed,
            PutExternalEventDefReply.class
        );
        ctx.onNext(response.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void putWfSpec(PutWfSpecPb req, StreamObserver<PutWfSpecReplyPb> ctx) {
        PutWfSpec ptd = PutWfSpec.fromProto(req);
        PutWfSpecReply response = backend.process(ptd, PutWfSpecReply.class);
        ctx.onNext(response.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void runWf(RunWfPb req, StreamObserver<RunWfReplyPb> ctx) {
        RunWf rwf = RunWf.fromProto(req);
        RunWfReply response = backend.process(rwf, RunWfReply.class);
        ctx.onNext(response.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public StreamObserver<PollTaskPb> pollTask(StreamObserver<PollTaskReplyPb> ctx) {
        return new TaskQueueStreamObserver(ctx, godzilla);
    }

    @Override
    public StreamObserver<RegisterTaskWorkerPb> registerTaskWorker(
        StreamObserver<RegisterTaskWorkerReplyPb> responseObserver
    ) {
        return new StreamObserver<RegisterTaskWorkerPb>() {
            @Override
            public void onCompleted() {
                // Nothing to do
            }

            @Override
            public void onError(Throwable t) {
                // Nothing to do
            }

            @Override
            public void onNext(RegisterTaskWorkerPb request) {
                responseObserver.onNext(backend.registerTaskWorker(request));
            }
        };
    }

    @Override
    public void reportTask(
        TaskResultEventPb req,
        StreamObserver<ReportTaskReplyPb> ctx
    ) {
        TaskResultEvent tre = TaskResultEvent.fromProto(req);
        ReportTaskReply rtr = backend.process(tre, ReportTaskReply.class);
        ctx.onNext(rtr.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void getWfRun(GetWfRunPb req, StreamObserver<GetWfRunReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            WfRun.class,
            GetWfRunReplyPb.class,
            config
        );

        internalComms.getBytesAsync(
            StoreUtils.getFullStoreKey(req.getId(), WfRun.class),
            req.getId(),
            observer
        );
    }

    @Override
    public void getNodeRun(GetNodeRunPb req, StreamObserver<GetNodeRunReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            NodeRun.class,
            GetNodeRunReplyPb.class,
            config
        );

        internalComms.getBytesAsync(
            StoreUtils.getFullStoreKey(
                NodeRun.getStoreKey(
                    req.getWfRunId(),
                    req.getThreadRunNumber(),
                    req.getPosition()
                ),
                NodeRun.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void getVariable(
        GetVariablePb req,
        StreamObserver<GetVariableReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            Variable.class,
            GetVariableReplyPb.class,
            config
        );

        internalComms.getBytesAsync(
            StoreUtils.getFullStoreKey(
                Variable.getStoreKey(
                    req.getWfRunId(),
                    req.getThreadRunNumber(),
                    req.getVarName()
                ),
                Variable.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void getExternalEvent(
        GetExternalEventPb req,
        StreamObserver<GetExternalEventReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            ExternalEvent.class,
            GetExternalEventReplyPb.class,
            config
        );

        internalComms.getBytesAsync(
            StoreUtils.getFullStoreKey(
                ExternalEvent.getStoreKey(
                    req.getWfRunId(),
                    req.getExternalEventDefName(),
                    req.getGuid()
                ),
                ExternalEvent.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void searchWfRun(
        SearchWfRunPb req,
        StreamObserver<SearchWfRunReplyPb> ctx
    ) {
        ctx.onNext(backend.searchWfRun(req));
        ctx.onCompleted();
    }

    @Override
    public void stopWfRun(StopWfRunPb req, StreamObserver<StopWfRunReplyPb> ctx) {
        StopWfRun swr = StopWfRun.fromProto(req);
        ctx.onNext(backend.process(swr, StopWfRunReply.class).toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void resumeWfRun(
        ResumeWfRunPb req,
        StreamObserver<ResumeWfRunReplyPb> ctx
    ) {
        ResumeWfRun rwr = ResumeWfRun.fromProto(req);
        ctx.onNext(backend.process(rwr, ResumeWfRunReply.class).toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void deleteWfRun(
        DeleteWfRunPb req,
        StreamObserver<DeleteWfRunReplyPb> ctx
    ) {
        DeleteWfRun dwr = DeleteWfRun.fromProto(req);
        ctx.onNext(backend.process(dwr, DeleteWfRunReply.class).toProto().build());
        ctx.onCompleted();
    }

    @SuppressWarnings("unchecked")
    private <
        U extends MessageOrBuilder, T extends AbstractResponse<U>
    > void processCommandAndSendResponse(
        SubCommand<?> subCmd,
        Class<T> responseCls,
        StreamObserver<U> observer,
        boolean shouldComplete
    ) {
        // TODO
        if (!subCmd.hasResponse()) {
            throw new RuntimeException(
                "Not possible; expected only respondable commands."
            );
        }

        Command cmd = new Command();
        cmd.commandId = LHUtil.generateGuid();
        cmd.time = new Date();
        cmd.setSubCommand(subCmd);

        LHAsyncWaiter<U> waiter = new LHAsyncWaiter<>(
            u -> {
                // if (shouldComplete)
            },
            LHUtil.getProtoBaseClass(responseCls)
        );

        asyncWaiters.put(cmd.commandId, waiter);

        // Now just record the command. If, for some reason, we can't
        // record the command, we need to send back an error from here.
        try {
            this.recordCommand(cmd);
        } catch (LHConnectionError exn) {
            try {
                asyncWaiters.remove(waiter.commandId);

                T out = responseCls.getDeclaredConstructor().newInstance();
                out.code = LHResponseCodePb.CONNECTION_ERROR;
                out.message = exn.getMessage();
                observer.onNext((U) out.toProto().build());
                if (shouldComplete) {
                    observer.onCompleted();
                }
            } catch (Exception impossible) {
                // Not possible
                exn.printStackTrace();
                throw new RuntimeException(exn);
            }
        }
    }

    public void recordCommand(Command command) throws LHConnectionError {
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
            throw new LHConnectionError(
                exn,
                "May have failed recording event: " + exn.getMessage()
            );
        }
    }

    public void onTaskScheduled(String taskDefName, String tsrObjectId) {
        // TODO: Use the GodzillaTaskQueueManager
    }

    public void onResponseReceived(String commandId, AbstractResponse<?> resp) {
        LHAsyncWaiter<?> waiter = asyncWaiters.get(commandId);
        LHUtil.log("Received response, returning now!");
        if (waiter != null) {
            waiter.onResponse(resp);
            asyncWaiters.remove(commandId);
        }
    }

    public void start() throws IOException {
        coreStreams.start();
        timerStreams.start();
        internalComms.start();
        grpcServer.start();
    }

    public void close() {
        grpcServer.shutdown();
        timerStreams.close();
        coreStreams.close();
        internalComms.close();
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

class IntermediateResp<
    U extends MessageOrBuilder,
    T extends LHSerializable<U>,
    V extends MessageOrBuilder
> {

    public String message;
    public LHResponseCodePb code;
    public T result;

    private Class<V> responseCls;

    public IntermediateResp(Class<V> responseCls) {
        this.responseCls = responseCls;
    }

    // EMPLOYEE_TODO: figure out why all my reflection is "unsafe" or "unchecked"
    @SuppressWarnings("unchecked")
    public V toProto() {
        try {
            GeneratedMessageV3.Builder<?> b = (GeneratedMessageV3.Builder<?>) responseCls
                .getMethod("newBuilder")
                .invoke(null);
            if (message != null) {
                responseCls.getMethod("setMessage", String.class).invoke(b, message);
            }
            responseCls.getMethod("setCode", LHResponseCodePb.class).invoke(b, code);
            if (result != null) {
                U resultProto = (U) result.toProto().build();
                responseCls
                    .getMethod("setResult", resultProto.getClass())
                    .invoke(b, resultProto);
            }
            return (V) b.build();
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new RuntimeException("Yikerz, not possible");
        }
    }
}

class GETStreamObserver<
    U extends MessageOrBuilder, T extends Storeable<U>, V extends MessageOrBuilder
>
    implements StreamObserver<CentralStoreQueryReplyPb> {

    private StreamObserver<V> ctx;
    private LHConfig config;
    private Class<T> getableCls;

    private IntermediateResp<U, T, V> out;

    public GETStreamObserver(
        StreamObserver<V> responseObserver,
        Class<T> getableCls,
        Class<V> responseCls,
        LHConfig config
    ) {
        this.ctx = responseObserver;
        this.getableCls = getableCls;
        this.config = config;

        this.out = new IntermediateResp<U, T, V>(responseCls);
    }

    public void onError(Throwable t) {
        // TODO
        out.code = LHResponseCodePb.CONNECTION_ERROR;
        out.message = "Failed connecting to backend: " + t.getMessage();
        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }

    public void onCompleted() {
        LHUtil.log("Unexpected call to onCompleted()");
    }

    public void onNext(CentralStoreQueryReplyPb reply) {
        // TODO
        if (reply.hasResult()) {
            out.code = LHResponseCodePb.OK;
            try {
                out.result =
                    LHSerializable.fromBytes(
                        reply.getResult().toByteArray(),
                        getableCls,
                        config
                    );
            } catch (LHSerdeError exn) {
                out.code = LHResponseCodePb.CONNECTION_ERROR;
                out.message =
                    "Impossible: got unreadable response from backend: " +
                    exn.getMessage();
            }
        } else {
            out.code = LHResponseCodePb.NOT_FOUND_ERROR;
        }

        ctx.onNext(out.toProto());
        ctx.onCompleted();
    }
}

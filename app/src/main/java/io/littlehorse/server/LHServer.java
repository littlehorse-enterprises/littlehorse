package io.littlehorse.server;

import com.google.protobuf.MessageOrBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
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
import io.littlehorse.server.streamsbackend.KafkaStreamsBackend;
import io.littlehorse.server.streamsbackend.storeinternals.BackendInternalComms;
import io.littlehorse.server.streamsbackend.taskqueue.GodzillaTaskQueueManager;
import io.littlehorse.server.streamsbackend.taskqueue.TaskQueueStreamObserver;
import io.littlehorse.server.streamsbackend.util.LHAsyncWaiter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;

public class LHServer extends LHPublicApiImplBase {

    private LHConfig config;
    private KafkaStreamsBackend backend;
    private Server grpcServer;
    private GodzillaTaskQueueManager godzilla;

    private BackendInternalComms internalComms;
    private LHProducer producer;

    private ConcurrentHashMap<String, LHAsyncWaiter<?>> asyncWaiters;

    public LHServer(LHConfig config) {
        this.config = config;

        // Hypothetically we could implement different backends in the future...
        // perhaps a Pulsar/Cassandra/Yugabyte backend.
        HealthStatusManager grpcHealthCheckThingy = new HealthStatusManager();
        this.godzilla = new GodzillaTaskQueueManager();
        backend =
            new KafkaStreamsBackend(this.config, grpcHealthCheckThingy, godzilla);
        godzilla.setBackend(backend);

        this.grpcServer =
            ServerBuilder
                .forPort(config.getApiBindPort())
                .addService(this)
                .addService(grpcHealthCheckThingy.getHealthService())
                .build();

        this.producer = new LHProducer(config, false);
        this.asyncWaiters = new ConcurrentHashMap<>();
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
        ctx.onNext(backend.getWfRun(req));
        ctx.onCompleted();
    }

    @Override
    public void getNodeRun(GetNodeRunPb req, StreamObserver<GetNodeRunReplyPb> ctx) {
        ctx.onNext(backend.getNodeRun(req));
        ctx.onCompleted();
    }

    @Override
    public void getVariable(
        GetVariablePb req,
        StreamObserver<GetVariableReplyPb> ctx
    ) {
        ctx.onNext(backend.getVariable(req));
        ctx.onCompleted();
    }

    @Override
    public void getExternalEvent(
        GetExternalEventPb req,
        StreamObserver<GetExternalEventReplyPb> ctx
    ) {
        ctx.onNext(backend.getExternalEvent(req));
        ctx.onCompleted();
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

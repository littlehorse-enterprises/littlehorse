package io.littlehorse.server;

import com.google.protobuf.MessageOrBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
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
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.common.proto.DeleteWfRunPb;
import io.littlehorse.common.proto.DeleteWfRunReplyPb;
import io.littlehorse.common.proto.GetExternalEventDefPb;
import io.littlehorse.common.proto.GetExternalEventDefReplyPb;
import io.littlehorse.common.proto.GetExternalEventPb;
import io.littlehorse.common.proto.GetExternalEventReplyPb;
import io.littlehorse.common.proto.GetMetricsReplyPb;
import io.littlehorse.common.proto.GetMetricsRequestPb;
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
import io.littlehorse.common.proto.HealthCheckPb;
import io.littlehorse.common.proto.HealthCheckReplyPb;
import io.littlehorse.common.proto.InternalSearchReplyPb;
import io.littlehorse.common.proto.LHHealthResultPb;
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
import io.littlehorse.common.proto.SearchNodeRunPb;
import io.littlehorse.common.proto.SearchReplyPb;
import io.littlehorse.common.proto.SearchTaskDefPb;
import io.littlehorse.common.proto.SearchVariablePb;
import io.littlehorse.common.proto.SearchWfRunPb;
import io.littlehorse.common.proto.SearchWfSpecPb;
import io.littlehorse.common.proto.StopWfRunPb;
import io.littlehorse.common.proto.StopWfRunReplyPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.TaskResultEventPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.BackendInternalComms;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.searchutils.LHPublicSearch;
import io.littlehorse.server.streamsimpl.searchutils.publicrequests.SearchNodeRun;
import io.littlehorse.server.streamsimpl.searchutils.publicrequests.SearchTaskDef;
import io.littlehorse.server.streamsimpl.searchutils.publicrequests.SearchVariable;
import io.littlehorse.server.streamsimpl.searchutils.publicrequests.SearchWfRun;
import io.littlehorse.server.streamsimpl.searchutils.publicrequests.SearchWfSpec;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import io.littlehorse.server.streamsimpl.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streamsimpl.taskqueue.TaskQueueManager;
import io.littlehorse.server.streamsimpl.util.GETStreamObserver;
import io.littlehorse.server.streamsimpl.util.POSTStreamObserver;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;

public class KafkaStreamsServerImpl extends LHPublicApiImplBase {

    private LHConfig config;
    private Server grpcServer;
    private TaskQueueManager taskQueueManager;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    private State coreState;
    private State timerState;

    private BackendInternalComms internalComms;

    public KafkaStreamsServerImpl(LHConfig config) {
        this.config = config;

        this.taskQueueManager = new TaskQueueManager(this);

        coreStreams =
            new KafkaStreams(
                ServerTopology.initCoreTopology(config, this),
                // For now we have to do EOS here, so we just pray...
                config.getStreamsConfig("core", true)
            );
        timerStreams =
            new KafkaStreams(
                ServerTopology.initTimerTopology(config),
                // Kafka Streams 3.4.0 should fix the issue with EOS and hanging
                // transactions on punctionations, which means we can (hopefully)
                // make the timer stream also EOS.
                config.getStreamsConfig("timer", false)
            );

        Executor executor = Executors.newFixedThreadPool(16);

        this.grpcServer =
            ServerBuilder
                .forPort(config.getApiBindPort())
                .addService(this)
                .executor(executor)
                .build();

        coreStreams.setStateListener((newState, oldState) -> {
            coreState = newState;
            LHUtil.log(new Date(), "New state for core:", coreState);
        });

        timerStreams.setStateListener((newState, oldState) -> {
            timerState = newState;
            LHUtil.log(new Date(), "New state for timer:", timerState);
        });

        internalComms = new BackendInternalComms(config, coreStreams);
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
            internalComms.getLastFromPrefixAsync(
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
            internalComms.getLastFromPrefixAsync(
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
            internalComms.getLastFromPrefixAsync(
                ExternalEventDef.getFullPrefixByName(req.getName()),
                LHConstants.META_PARTITION_KEY,
                observer
            );
        }
    }

    @Override
    public void putTaskDef(PutTaskDefPb req, StreamObserver<PutTaskDefReplyPb> ctx) {
        processCommand(req, ctx, PutTaskDef.class, PutTaskDefReplyPb.class);
    }

    @Override
    public void putExternalEvent(
        PutExternalEventPb req,
        StreamObserver<PutExternalEventReplyPb> ctx
    ) {
        processCommand(
            req,
            ctx,
            PutExternalEvent.class,
            PutExternalEventReplyPb.class
        );
    }

    @Override
    public void putExternalEventDef(
        PutExternalEventDefPb req,
        StreamObserver<PutExternalEventDefReplyPb> ctx
    ) {
        processCommand(
            req,
            ctx,
            PutExternalEventDef.class,
            PutExternalEventDefReplyPb.class
        );
    }

    @Override
    public void putWfSpec(PutWfSpecPb req, StreamObserver<PutWfSpecReplyPb> ctx) {
        processCommand(req, ctx, PutWfSpec.class, PutWfSpecReplyPb.class);
    }

    @Override
    public void runWf(RunWfPb req, StreamObserver<RunWfReplyPb> ctx) {
        processCommand(req, ctx, RunWf.class, RunWfReplyPb.class);
    }

    @Override
    public StreamObserver<PollTaskPb> pollTask(StreamObserver<PollTaskReplyPb> ctx) {
        return new PollTaskRequestObserver(ctx, taskQueueManager);
    }

    @Override
    public void registerTaskWorker(
        RegisterTaskWorkerPb req,
        StreamObserver<RegisterTaskWorkerReplyPb> responseObserver
    ) {
        responseObserver.onNext(internalComms.registerTaskWorker(req));
        responseObserver.onCompleted();
    }

    @Override
    public void reportTask(
        TaskResultEventPb req,
        StreamObserver<ReportTaskReplyPb> ctx
    ) {
        processCommand(req, ctx, TaskResultEvent.class, ReportTaskReplyPb.class);
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
    public void searchWfRun(SearchWfRunPb req, StreamObserver<SearchReplyPb> ctx) {
        handleSearch(SearchWfRun.fromProto(req), ctx);
    }

    @Override
    public void searchNodeRun(
        SearchNodeRunPb req,
        StreamObserver<SearchReplyPb> ctx
    ) {
        handleSearch(SearchNodeRun.fromProto(req), ctx);
    }

    @Override
    public void searchVariable(
        SearchVariablePb req,
        StreamObserver<SearchReplyPb> ctx
    ) {
        handleSearch(SearchVariable.fromProto(req), ctx);
    }

    @Override
    public void searchTaskDef(
        SearchTaskDefPb req,
        StreamObserver<SearchReplyPb> ctx
    ) {
        handleSearch(SearchTaskDef.fromProto(req), ctx);
    }

    @Override
    public void searchWfSpec(SearchWfSpecPb req, StreamObserver<SearchReplyPb> ctx) {
        handleSearch(SearchWfSpec.fromProto(req), ctx);
    }

    // EMPLOYEE_TODO: this is a synchronous call. Make it asynchronous.
    // This will require refactoring the PaginatedTagQuery logic, which will be
    // hard. Once an employee can do this, they will have earned their lightsaber
    // and graduated to the rank of Jedi Padawan.
    //
    // EMPLOYEE_TODO: Right now, we just swallow any malformed requests and raise
    // a RuntimeException (eg. when the bookmark bytes are malformed), which means
    // the client just gets an internal error. Figure out a way to refactor this
    // so that we can return a useful error message to the client.
    private void handleSearch(
        LHPublicSearch<?> req,
        StreamObserver<SearchReplyPb> ctx
    ) {
        SearchReplyPb.Builder out = SearchReplyPb.newBuilder();

        try {
            InternalSearchReplyPb raw = internalComms.doSearch(
                req.getInternalSearch(internalComms.getGlobalStoreImpl())
            );
            out.setCode(LHResponseCodePb.OK);
            if (raw.hasUpdatedBookmark()) {
                out.setBookmark(raw.getUpdatedBookmark().toByteString());
            }
            out.addAllObjectIds(raw.getObjectIdsList());
        } catch (LHConnectionError exn) {
            out.setCode(LHResponseCodePb.CONNECTION_ERROR);
            out.setMessage("Failed connecting to backend: " + exn.getMessage());
        } catch (LHValidationError exn) {
            out.setCode(LHResponseCodePb.VALIDATION_ERROR);
            out.setMessage("Failed validating search request: " + exn.getMessage());
        }

        ctx.onNext(out.build());
        ctx.onCompleted();
    }

    @Override
    public void stopWfRun(StopWfRunPb req, StreamObserver<StopWfRunReplyPb> ctx) {
        processCommand(req, ctx, StopWfRun.class, StopWfRunReplyPb.class);
    }

    @Override
    public void resumeWfRun(
        ResumeWfRunPb req,
        StreamObserver<ResumeWfRunReplyPb> ctx
    ) {
        processCommand(req, ctx, ResumeWfRun.class, ResumeWfRunReplyPb.class);
    }

    @Override
    public void deleteWfRun(
        DeleteWfRunPb req,
        StreamObserver<DeleteWfRunReplyPb> ctx
    ) {
        processCommand(req, ctx, DeleteWfRun.class, DeleteWfRunReplyPb.class);
    }

    @Override
    public void healthCheck(
        HealthCheckPb req,
        StreamObserver<HealthCheckReplyPb> ctx
    ) {
        ctx.onNext(
            HealthCheckReplyPb
                .newBuilder()
                .setCoreState(kafkaStateToLhHealthState(coreState))
                .setTimerState(kafkaStateToLhHealthState(timerState))
                .build()
        );
        ctx.onCompleted();
    }

    private LHHealthResultPb kafkaStateToLhHealthState(State kState) {
        switch (kState) {
            case CREATED:
            case NOT_RUNNING:
            case REBALANCING:
                return LHHealthResultPb.LH_HEALTH_REBALANCING;
            case RUNNING:
                return LHHealthResultPb.LH_HEALTH_RUNNING;
            case PENDING_ERROR:
            case PENDING_SHUTDOWN:
            case ERROR:
                return LHHealthResultPb.LH_HEALTH_ERROR;
            default:
                throw new RuntimeException("Unknown health status");
        }
    }

    @Override
    public void getMetrics(
        GetMetricsRequestPb req,
        StreamObserver<GetMetricsReplyPb> ctx
    ) {
        Map<MetricName, ? extends Metric> metrics = coreStreams.metrics();

        StringBuilder out = new StringBuilder();
        for (Map.Entry<MetricName, ? extends Metric> entry : metrics.entrySet()) {
            out.append(entry.getKey().group() + ".");
            out.append(entry.getKey().name());
            out.append(": ");
            out.append(entry.getValue().metricValue().toString());
            out.append("\n");
        }

        ctx.onNext(GetMetricsReplyPb.newBuilder().setMetrics(out.toString()).build());
        ctx.onCompleted();
    }

    public void returnTaskToClient(
        TaskScheduleRequest tsr,
        PollTaskRequestObserver client
    ) {
        TaskClaimEvent claimEvent = new TaskClaimEvent();
        claimEvent.wfRunId = tsr.wfRunId;
        claimEvent.threadRunNumber = tsr.threadRunNumber;
        claimEvent.taskRunPosition = tsr.taskRunPosition;
        claimEvent.taskRunNumber = tsr.taskRunNumber;
        claimEvent.time = new Date();

        Command taskClaimCommand = new Command();
        taskClaimCommand.type = CommandCase.TASK_CLAIM_EVENT;
        taskClaimCommand.taskClaimEvent = claimEvent;
        taskClaimCommand.time = new Date();

        // the old way which synchronously processes:
        processCommand(
            claimEvent.toProto().build(),
            client.getResponseObserver(),
            TaskClaimEvent.class,
            PollTaskReplyPb.class,
            false // it's a stream, so we don't want to complete it.
        );
        // // // the new way which short-circuits the need to wait for processing.
        // recordClaimEventAndReturnTask(
        //     taskClaimCommand,
        //     tsr,
        //     client.getResponseObserver()
        // );
    }

    // // DO NOT DELETE THIS
    // private void recordClaimEventAndReturnTask(
    //     Command taskClaimCommand,
    //     TaskScheduleRequest tsr,
    //     StreamObserver<PollTaskReplyPb> observer
    // ) {
    //     internalComms
    //         .getProducer()
    //         .send(
    //             taskClaimCommand.getPartitionKey(),
    //             taskClaimCommand,
    //             config.getCoreCmdTopicName(),
    //             (recordMeta, exn) -> {
    //                 if (exn != null) {
    //                     // Then the command wasn't successfully claimed. Just return
    //                     // an empty reply and get the client to try again later.
    //                     observer.onNext(
    //                         PollTaskReplyPb
    //                             .newBuilder()
    //                             .setCode(LHResponseCodePb.CONNECTION_ERROR)
    //                             .setMessage(
    //                                 "Unable to claim command, had a kafka error: " +
    //                                 exn.getMessage()
    //                             )
    //                             .build()
    //                     );
    //                 } else {
    //                     // Then the message has been accepted by Kafka. It's time to
    //                     // finally return the task to client.
    //                     observer.onNext(
    //                         PollTaskReplyPb
    //                             .newBuilder()
    //                             .setCode(LHResponseCodePb.OK)
    //                             .setResult(tsr.toProto())
    //                             .build()
    //                     );
    //                 }
    //             }
    //         );
    // }

    public LHProducer getProducer() {
        return internalComms.getProducer();
    }

    public void onResponseReceived(String commandId, WaitForCommandReplyPb response) {
        internalComms.onResponseReceived(commandId, response);
    }

    private <
        U extends MessageOrBuilder,
        T extends SubCommand<U>,
        V extends MessageOrBuilder
    > void processCommand(
        U request,
        StreamObserver<V> responseObserver,
        Class<T> subCmdCls,
        Class<V> responseCls
    ) {
        processCommand(request, responseObserver, subCmdCls, responseCls, true);
    }

    private <
        U extends MessageOrBuilder,
        T extends SubCommand<U>,
        V extends MessageOrBuilder
    > void processCommand(
        U request,
        StreamObserver<V> responseObserver,
        Class<T> subCmdCls,
        Class<V> responseCls,
        boolean shouldComplete
    ) {
        T subCmd = LHSerializable.fromProto(request, subCmdCls);
        Command command = new Command(subCmd);
        command.commandId = LHUtil.generateGuid();
        StreamObserver<WaitForCommandReplyPb> observer = new POSTStreamObserver<>(
            responseObserver,
            responseCls,
            shouldComplete
        );

        // Now actually record the command.
        internalComms
            .getProducer()
            .send(
                command.getPartitionKey(), // partition key
                command, // payload
                config.getCoreCmdTopicName(), // topic name
                (meta, exn) -> { // callback
                    if (exn != null) {
                        // Then we report back to the observer that we failed to record
                        // the command.
                        observer.onNext(
                            WaitForCommandReplyPb
                                .newBuilder()
                                .setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE)
                                .setMessage(
                                    "Failed recording command to Kafka: " +
                                    exn.getMessage()
                                )
                                .build()
                        );
                        // EMPLOYEE_TODO: determine whether or not to use onError()
                        // instead.
                        observer.onCompleted();
                    } else {
                        // Now we wait for the processing
                        internalComms.waitForCommand(command, observer);
                    }
                }
            );
    }

    public void onTaskScheduled(String taskDefName, TaskScheduleRequest tsr) {
        taskQueueManager.onTaskScheduled(taskDefName, tsr);
    }

    public void start() throws IOException {
        coreStreams.start();
        timerStreams.start();
        internalComms.start();
        grpcServer.start();
    }

    public void close() {
        CountDownLatch latch = new CountDownLatch(3);

        new Thread(() -> {
            LHUtil.log("Closing timer");
            timerStreams.close();
            latch.countDown();
        })
            .start();

        new Thread(() -> {
            LHUtil.log("Closing core");
            coreStreams.close();
            latch.countDown();
        })
            .start();

        new Thread(() -> {
            LHUtil.log("Closing internalComms");
            internalComms.close();
            latch.countDown();
        })
            .start();

        try {
            LHUtil.log("Shutting down main server");
            grpcServer.shutdownNow();
            grpcServer.awaitTermination();
        } catch (InterruptedException ignored) {}

        try {
            latch.await();
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
    }

    public static void doMain(LHConfig config)
        throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        KafkaStreamsServerImpl server = new KafkaStreamsServerImpl(config);
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    System.out.println("Closing now!");
                    server.close();
                    config.cleanup();
                    latch.countDown();
                })
            );
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException exn) {
                throw new RuntimeException(exn);
            }
        })
            .start();

        System.out.println("Hello there!");
        latch.await();
        System.out.println("Done waiting for countdown latch");
    }
}

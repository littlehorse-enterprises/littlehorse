package io.littlehorse.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.server.listener.ListenersManager;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListExternalEventsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListNodeRunsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListTaskMetricsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListVariablesRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListWfMetricsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchExternalEventDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchExternalEventRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchNodeRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchTaskDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchTaskRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchUserTaskDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchUserTaskRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchVariableRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWfRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWfSpecRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListExternalEventsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListNodeRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListVariablesReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.GETStreamObserverNew;
import io.littlehorse.server.streams.util.HealthService;
import io.littlehorse.server.streams.util.POSTStreamObserver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;

@Slf4j
public class KafkaStreamsServerImpl extends LHPublicApiImplBase {

    private LHConfig config;
    private TaskQueueManager taskQueueManager;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    private State coreState;
    private State timerState;

    private BackendInternalComms internalComms;

    private ListenersManager listenerManager;
    private HealthService healthService;
    private ReadOnlyMetadataStore metaStore;

    public KafkaStreamsServerImpl(LHConfig config) {
        this.config = config;
        this.taskQueueManager = new TaskQueueManager(this);
        this.coreStreams = new KafkaStreams(
                ServerTopology.initCoreTopology(config, this),
                // Core topology must be EOS
                config.getStreamsConfig("core", true));
        this.timerStreams = new KafkaStreams(
                ServerTopology.initTimerTopology(config),
                // We don't want the Timer topology to be EOS. The reason for this
                // has to do with the fact that:
                // a) Timer is idempotent, so it doesn't really matter
                // b) If it's EOS, then there will be transactional records on
                //    the core command topic. With the EOS for the core topology,
                //    that means processing will block until the commit() of the
                //    timer, which means latency will jump from 15ms to >100ms
                config.getStreamsConfig("timer", false));
        this.healthService = new HealthService(config, coreStreams, timerStreams);

        Executor networkThreadpool = Executors.newFixedThreadPool(config.getNumNetworkThreads());
        this.listenerManager = new ListenersManager(config, this, networkThreadpool, healthService.getMeterRegistry());

        this.internalComms = new BackendInternalComms(config, coreStreams, timerStreams, networkThreadpool);
    }

    public String getInstanceId() {
        return config.getLHInstanceId();
    }

    @Override
    public void getWfSpec(WfSpecId req, StreamObserver<WfSpec> ctx) {
        WfSpecModel wfSpec = metaStore.getWfSpec(req.getName(), req.getVersion());
        if (wfSpec == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec"));
        } else {
            ctx.onNext(wfSpec.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void getLatestWfSpec(GetLatestWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        WfSpecModel wfSpec = metaStore.getWfSpec(req.getName(), null);
        if (wfSpec == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec"));
        } else {
            ctx.onNext(wfSpec.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void getLatestUserTaskDef(GetLatestUserTaskDefRequest req, StreamObserver<UserTaskDef> ctx) {
        UserTaskDefModel utd = metaStore.getUserTaskDef(req.getName(), null);
        if (utd == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified UserTaskDef"));
        } else {
            ctx.onNext(utd.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void getUserTaskDef(UserTaskDefId req, StreamObserver<UserTaskDef> ctx) {
        UserTaskDefModel utd = metaStore.getUserTaskDef(req.getName(), req.getVersion());
        if (utd == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified UserTaskDef"));
        } else {
            ctx.onNext(utd.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void getTaskDef(TaskDefId req, StreamObserver<TaskDef> ctx) {
        TaskDefModel td = metaStore.getTaskDef(req.getName());
        if (td == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified TaskDef"));
        } else {
            ctx.onNext(td.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void getExternalEventDef(ExternalEventDefId req, StreamObserver<ExternalEventDef> ctx) {
        ExternalEventDefModel eed = metaStore.getExternalEventDef(req.getName());
        if (eed == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified ExternalEventDef"));
        } else {
            ctx.onNext(eed.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    public void putTaskDef(PutTaskDefRequest req, StreamObserver<TaskDef> ctx) {
        processMetadataCommand(req, ctx, PutTaskDefRequestModel.class, PutTaskDefResponse.class);
    }

    @Override
    public void putExternalEvent(
            io.littlehorse.sdk.common.proto.PutExternalEventRequest req, StreamObserver<PutExternalEventResponse> ctx) {
        processCommand(req, ctx, PutExternalEventRequestModel.class, PutExternalEventResponse.class);
    }

    @Override
    public void putExternalEventDef(
            io.littlehorse.sdk.common.proto.PutExternalEventDefRequest req,
            StreamObserver<PutExternalEventDefResponse> ctx) {
        processMetadataCommand(req, ctx, PutExternalEventDefRequestModel.class, PutExternalEventDefResponse.class);
    }

    @Override
    public void putUserTaskDef(
            io.littlehorse.sdk.common.proto.PutUserTaskDefRequest req, StreamObserver<PutUserTaskDefResponse> ctx) {
        processMetadataCommand(req, ctx, PutUserTaskDefRequestModel.class, PutUserTaskDefResponse.class);
    }

    @Override
    public void assignUserTaskRun(AssignUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        processCommand(req, ctx, AssignUserTaskRunRequestModel.class, AssignUserTaskRunResponse.class);
    }

    @Override
    public void completeUserTaskRun(CompleteUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        processCommand(req, ctx, CompleteUserTaskRunRequestModel.class, CompleteUserTaskRunResponse.class);
    }

    @Override
    public void cancelUserTaskRun(CancelUserTaskRunRequest req, StreamObserver<CancelUserTaskRunResponse> ctx) {
        processCommand(req, ctx, CancelUserTaskRunRequestModel.class, CancelUserTaskRunResponse.class);
    }

    @Override
    public void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest req, StreamObserver<PutWfSpecResponse> ctx) {
        processMetadataCommand(req, ctx, PutWfSpecRequestModel.class, PutWfSpecResponse.class);
    }

    @Override
    public void runWf(RunWfRequest req, StreamObserver<RunWfResponse> ctx) {
        processCommand(req, ctx, RunWfRequestModel.class, RunWfResponse.class);
    }

    @Override
    public StreamObserver<PollTaskRequest> pollTask(StreamObserver<PollTaskResponse> ctx) {
        return new PollTaskRequestObserver(ctx, taskQueueManager);
    }

    @Override
    public void registerTaskWorker(
            RegisterTaskWorkerRequest req, StreamObserver<RegisterTaskWorkerResponse> responseObserver) {
        log.trace("Receiving RegisterTaskWorkerRequest (heartbeat) from: " + req.getClientId());

        TaskWorkerHeartBeatRequest heartBeatPb = TaskWorkerHeartBeatRequest.newBuilder()
                .setClientId(req.getClientId())
                .setListenerName(req.getListenerName())
                .setTaskDefName(req.getTaskDefName())
                .build();

        processCommand(
                heartBeatPb, responseObserver, TaskWorkerHeartBeatRequestModel.class, RegisterTaskWorkerResponse.class);
    }

    @Override
    public void reportTask(ReportTaskRun req, StreamObserver<ReportTaskResponse> ctx) {
        processCommand(req, ctx, ReportTaskRunModel.class, ReportTaskResponse.class);
    }

    @Override
    public void getWfRun(WfRunId req, StreamObserver<GetWfRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserverNew<>(ctx, WfRunModel.class, GetWfRunResponse.class, config);
        WfRunIdModel id = LHSerializable.fromProto(req, WfRunIdModel.class);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE, StoreUtils.getFullStoreKey(id, WfRunModel.class), req.getId(), observer);
    }

    @Override
    public void getNodeRun(NodeRunId req, StreamObserver<GetNodeRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserverNew<>(ctx, NodeRunModel.class, GetNodeRunResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(
                        new NodeRunIdModel(req.getWfRunId(), req.getThreadRunNumber(), req.getPosition()),
                        NodeRunModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void getTaskRun(TaskRunId req, StreamObserver<TaskRun> ctx) {
        TaskRunIdModel id = LHSerializable.fromProto(req, TaskRunIdModel.class);
        try {
            TaskRunModel taskRun = internalComms.getObject(id, TaskRunModel.class);
            ctx.onNext(taskRun.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            ctx.onError(exn);
        }
    }

    @Override
    public void getUserTaskRun(UserTaskRunId req, StreamObserver<GetUserTaskRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserverNew<>(ctx, UserTaskRunModel.class, GetUserTaskRunResponse.class, config);

        UserTaskRunIdModel userTaskRunId = LHSerializable.fromProto(req, UserTaskRunIdModel.class);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(userTaskRunId.getStoreKey(), UserTaskRunModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void taskDefMetrics(TaskDefMetricsQueryRequest req, StreamObserver<TaskDefMetricsResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserver<>(ctx, TaskDefMetricsModel.class, TaskDefMetricsResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_REPARTITION_STORE,
                StoreUtils.getFullStoreKey(TaskDefMetricsModel.getObjectId(req), TaskDefMetricsModel.class),
                req.getTaskDefName(),
                observer);
    }

    @Override
    public void wfSpecMetrics(WfSpecMetricsQueryRequest req, StreamObserver<WfSpecMetricsResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserver<>(ctx, WfSpecMetricsModel.class, WfSpecMetricsResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_REPARTITION_STORE,
                StoreUtils.getFullStoreKey(WfSpecMetricsModel.getObjectId(req), WfSpecMetricsModel.class),
                req.getWfSpecName(),
                observer);
    }

    @Override
    public void getVariable(VariableId req, StreamObserver<GetVariableResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserverNew<>(ctx, VariableModel.class, GetVariableResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(
                        new VariableIdModel(req.getWfRunId(), req.getThreadRunNumber(), req.getName()),
                        VariableModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void getExternalEvent(ExternalEventId req, StreamObserver<GetExternalEventResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer =
                new GETStreamObserverNew<>(ctx, ExternalEventModel.class, GetExternalEventResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(
                        new ExternalEventIdModel(req.getWfRunId(), req.getExternalEventDefName(), req.getGuid()),
                        ExternalEventModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void searchWfRun(SearchWfRunRequest req, StreamObserver<WfRunIdList> ctx) {
        handleScan(SearchWfRunRequestModel.fromProto(req), ctx, SearchWfRunReply.class);
    }

    @Override
    public void searchExternalEvent(SearchExternalEventRequest req, StreamObserver<ExternalEventIdList> ctx) {
        SearchExternalEventRequestModel see = LHSerializable.fromProto(req, SearchExternalEventRequestModel.class);
        handleScan(see, ctx, SearchExternalEventReply.class);
    }

    @Override
    public void searchNodeRun(SearchNodeRunRequest req, StreamObserver<NodeRunIdList> ctx) {
        handleScan(SearchNodeRunRequestModel.fromProto(req), ctx, SearchNodeRunReply.class);
    }

    @Override
    public void searchTaskRun(SearchTaskRunRequest req, StreamObserver<TaskRunIdList> ctx) {
        handleScan(SearchTaskRunRequestModel.fromProto(req), ctx, SearchTaskRunReply.class);
    }

    @Override
    public void searchUserTaskRun(SearchUserTaskRunRequest req, StreamObserver<UserTaskRunIdList> ctx) {
        handleScan(SearchUserTaskRunRequestModel.fromProto(req), ctx, SearchUserTaskRunReply.class);
    }

    @Override
    public void searchVariable(SearchVariableRequest req, StreamObserver<VariableIdList> ctx) {
        handleScan(SearchVariableRequestModel.fromProto(req), ctx, SearchVariableReply.class);
    }

    @Override
    public void searchTaskDef(SearchTaskDefRequest req, StreamObserver<TaskDefIdList> ctx) {
        handleScan(SearchTaskDefRequestModel.fromProto(req), ctx, SearchTaskDefReply.class);
    }

    @Override
    public void searchUserTaskDef(SearchUserTaskDefRequest req, StreamObserver<UserTaskDefIdList> ctx) {
        handleScan(SearchUserTaskDefRequestModel.fromProto(req), ctx, SearchUserTaskDefReply.class);
    }

    @Override
    public void searchWfSpec(SearchWfSpecRequest req, StreamObserver<WfSpecIdList> ctx) {
        handleScan(SearchWfSpecRequestModel.fromProto(req), ctx, SearchWfSpecReply.class);
    }

    @Override
    public void searchExternalEventDef(SearchExternalEventDefRequest req, StreamObserver<ExternalEventDefIdList> ctx) {
        handleScan(SearchExternalEventDefRequestModel.fromProto(req), ctx, SearchExternalEventDefReply.class);
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
    @SuppressWarnings("unchecked")
    private <
                    T extends Message,
                    RP extends Message,
                    OP extends Message,
                    OJ extends LHSerializable<OP>,
                    R extends PublicScanReply<RP, OP, OJ>>
            void handleScan(PublicScanRequest<T, RP, OP, OJ, R> req, StreamObserver<RP> ctx, Class<R> replyCls) {
        R out;
        try {
            out = replyCls.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException exn) {
            ctx.onError(exn);
            return;
        }

        try {
            InternalScanResponse raw = internalComms.doScan(req.getInternalSearch(internalComms.getGlobalStoreImpl()));
            if (raw.hasUpdatedBookmark()) {
                out.bookmark = raw.getUpdatedBookmark().toByteString();
            }
            for (ByteString responseEntry : raw.getResultsList()) {
                out.results.add(LHSerializable.fromBytes(responseEntry.toByteArray(), out.getResultJavaClass()));
            }
            ctx.onNext((RP) out.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            ctx.onError(exn);
        }
    }

    @Override
    public void listNodeRuns(ListNodeRunsRequest req, StreamObserver<NodeRunList> ctx) {
        ListNodeRunsRequestModel lnr = LHSerializable.fromProto(req, ListNodeRunsRequestModel.class);
        handleScan(lnr, ctx, ListNodeRunReply.class);
    }

    @Override
    public void listVariables(ListVariablesRequest req, StreamObserver<VariableList> ctx) {
        ListVariablesRequestModel lv = LHSerializable.fromProto(req, ListVariablesRequestModel.class);
        handleScan(lv, ctx, ListVariablesReply.class);
    }

    @Override
    public void listExternalEvents(ListExternalEventsRequest req, StreamObserver<ExternalEventList> ctx) {
        ListExternalEventsRequestModel lv = LHSerializable.fromProto(req, ListExternalEventsRequestModel.class);
        handleScan(lv, ctx, ListExternalEventsReply.class);
    }

    @Override
    public void listTaskDefMetrics(ListTaskMetricsRequest req, StreamObserver<ListTaskMetricsResponse> ctx) {
        ListTaskMetricsRequestModel ltm = LHSerializable.fromProto(req, ListTaskMetricsRequestModel.class);
        handleScan(ltm, ctx, ListTaskMetricsReply.class);
    }

    @Override
    public void listWfSpecMetrics(ListWfMetricsRequest req, StreamObserver<ListWfMetricsResponse> ctx) {
        ListWfMetricsRequestModel ltm = LHSerializable.fromProto(req, ListWfMetricsRequestModel.class);
        handleScan(ltm, ctx, ListWfMetricsReply.class);
    }

    @Override
    public void stopWfRun(StopWfRunRequest req, StreamObserver<Empty> ctx) {
        StopWfRunRequestModel reqModel = LHSerializable.fromProto(req, StopWfRunRequestModel.class);
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void resumeWfRun(ResumeWfRunRequest req, StreamObserver<Empty> ctx) {
        ResumeWfRunRequestModel reqModel = LHSerializable.fromProto(req, ResumeWfRunRequestModel.class);
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void deleteWfRun(DeleteWfRunRequest req, StreamObserver<Empty> ctx) {
        DeleteWfRunRequestModel reqModel = LHSerializable.fromProto(req, DeleteWfRunRequestModel.class);
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void deleteWfSpec(DeleteWfSpecRequest req, StreamObserver<Empty> ctx) {
        DeleteWfSpecRequestModel reqModel = LHSerializable.fromProto(req, DeleteWfSpecRequestModel.class);
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void deleteTaskDef(DeleteTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteTaskDefRequestModel reqModel = LHSerializable.fromProto(req, DeleteTaskDefRequestModel.class);
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void deleteUserTaskDef(DeleteUserTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteUserTaskDefRequestModel reqModel = LHSerializable.fromProto(req, DeleteUserTaskDefRequestModel.class);
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    public void deleteExternalEventDef(DeleteExternalEventDefRequest req, StreamObserver<Empty> ctx) {
        DeleteExternalEventDefRequestModel deedr =
                LHSerializable.fromProto(req, DeleteExternalEventDefRequestModel.class);
        processCommand(new MetadataCommandModel(deedr), ctx, Empty.class, true);
    }

    @Override
    public void healthCheck(Empty req, StreamObserver<HealthCheckResponse> ctx) {
        ctx.onNext(HealthCheckResponse.newBuilder()
                .setCoreState(kafkaStateToLhHealthState(coreState))
                .setTimerState(kafkaStateToLhHealthState(timerState))
                .build());
        ctx.onCompleted();
    }

    private LHHealthResult kafkaStateToLhHealthState(State kState) {
        switch (kState) {
            case CREATED:
            case NOT_RUNNING:
            case REBALANCING:
                return LHHealthResult.LH_HEALTH_REBALANCING;
            case RUNNING:
                return LHHealthResult.LH_HEALTH_RUNNING;
            case PENDING_ERROR:
            case PENDING_SHUTDOWN:
            case ERROR:
                return LHHealthResult.LH_HEALTH_ERROR;
            default:
                throw new RuntimeException("Unknown health status");
        }
    }

    public void returnTaskToClient(ScheduledTaskModel scheduledTask, PollTaskRequestObserver client) {
        TaskClaimEvent claimEvent = new TaskClaimEvent(scheduledTask, client);
        processCommand(new CommandModel(claimEvent), client.getResponseObserver(), PollTaskResponse.class, false);
    }

    public LHProducer getProducer() {
        return internalComms.getProducer();
    }

    public void onResponseReceived(String commandId, WaitForCommandResponse response) {
        internalComms.onResponseReceived(commandId, response);
    }

    public void sendErrorToClient(String commandId, Exception caught) {
        internalComms.sendErrorToClientForCommand(commandId, caught);
    }

    private <AC extends Message, RC extends Message> void processCommand(
            AbstractCommand<AC> command,
            StreamObserver<RC> responseObserver,
            Class<RC> responseCls,
            boolean shouldCompleteStream) {
        StreamObserver<WaitForCommandResponse> commandObserver =
                new POSTStreamObserver<>(responseObserver, responseCls, shouldCompleteStream);

        internalComms
                .getProducer()
                .send(
                        command.getPartitionKey(), // partition key
                        command, // payload
                        command.getTopic(config), // topic name
                        (meta, exn) -> this.productionCallback(meta, exn, commandObserver, command));
    }

    private void productionCallback(
            RecordMetadata meta,
            Exception exn,
            StreamObserver<WaitForCommandResponse> observer,
            AbstractCommand<?> command) {
        if (exn != null) {
            observer.onError(new LHApiException(Status.UNAVAILABLE, "Failed recording command to Kafka"));
        } else {
            internalComms.waitForCommand(command, observer);
        }
    }

    public void onTaskScheduled(TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask) {
        taskQueueManager.onTaskScheduled(taskDef, scheduledTask);
    }

    public void start() throws IOException {
        coreStreams.start();
        timerStreams.start();
        internalComms.start();
        listenerManager.start();
        healthService.start();
    }

    public void close() {
        CountDownLatch latch = new CountDownLatch(4);

        new Thread(() -> {
                    log.info("Closing timer");
                    timerStreams.close();
                    latch.countDown();
                })
                .start();

        new Thread(() -> {
                    log.info("Closing core");
                    coreStreams.close();
                    latch.countDown();
                })
                .start();

        new Thread(() -> {
                    log.info("Closing internalComms");
                    internalComms.close();
                    latch.countDown();
                })
                .start();

        new Thread(() -> {
                    log.info("Closing health service");
                    healthService.close();
                    latch.countDown();
                })
                .start();

        log.info("Shutting down main servers");
        listenerManager.close();

        try {
            latch.await();
        } catch (Exception exn) {
            throw new RuntimeException(exn);
        }
    }

    public static void doMain(LHConfig config) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        KafkaStreamsServerImpl server = new KafkaStreamsServerImpl(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Closing now!");
            server.close();
            config.cleanup();
            latch.countDown();
        }));
        new Thread(() -> {
                    try {
                        server.start();
                    } catch (IOException exn) {
                        throw new RuntimeException(exn);
                    }
                })
                .start();

        latch.await();
        log.info("Done waiting for countdown latch");
    }

    public Set<HostModel> getAllInternalHosts() {
        return internalComms.getAllInternalHosts();
    }

    public LHHostInfo getAdvertisedHost(HostModel host, String listenerName)
            throws LHBadRequestError, LHConnectionError {
        return internalComms.getAdvertisedHost(host, listenerName);
    }
}

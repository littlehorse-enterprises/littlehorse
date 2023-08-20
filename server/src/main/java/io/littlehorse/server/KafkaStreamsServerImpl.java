package io.littlehorse.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.command.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.command.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.command.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskWorkerHeartBeatRequestModel;
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
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.repartitioned.taskmetrics.TaskDefMetricsModel;
import io.littlehorse.common.model.getable.repartitioned.workflowmetrics.WfSpecMetricsModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.proto.CentralStoreQueryResponse;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.DeleteObjectResponse;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.GetExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.GetExternalEventResponse;
import io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.GetNodeRunResponse;
import io.littlehorse.sdk.common.proto.GetTaskDefResponse;
import io.littlehorse.sdk.common.proto.GetTaskRunResponse;
import io.littlehorse.sdk.common.proto.GetUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.GetUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.GetVariableResponse;
import io.littlehorse.sdk.common.proto.GetWfRunResponse;
import io.littlehorse.sdk.common.proto.GetWfSpecResponse;
import io.littlehorse.sdk.common.proto.HealthCheckRequest;
import io.littlehorse.sdk.common.proto.HealthCheckResponse;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.LHHealthResult;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.ListExternalEventsRequest;
import io.littlehorse.sdk.common.proto.ListExternalEventsResponse;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.ListNodeRunsResponse;
import io.littlehorse.sdk.common.proto.ListTaskMetricsRequest;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.ListVariablesResponse;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.ListWfMetricsResponse;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.PutExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.PutExternalEventResponse;
import io.littlehorse.sdk.common.proto.PutTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutWfSpecResponse;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.ReportTaskResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.sdk.common.proto.ResumeWfRunResponse;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.RunWfResponse;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventResponse;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest;
import io.littlehorse.sdk.common.proto.SearchNodeRunResponse;
import io.littlehorse.sdk.common.proto.SearchTaskDefResponse;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunResponse;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.SearchVariableResponse;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunResponse;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecResponse;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.StopWfRunResponse;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest;
import io.littlehorse.sdk.common.proto.TaskDefMetricsResponse;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest;
import io.littlehorse.sdk.common.proto.WfSpecMetricsResponse;
import io.littlehorse.server.listener.ListenersManager;
import io.littlehorse.server.streamsimpl.BackendInternalComms;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListExternalEventsRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListNodeRunsRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListTaskMetricsRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListVariablesRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListWfMetricsRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchExternalEventDefRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchExternalEventRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchNodeRunRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchTaskDefRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchTaskRunRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchUserTaskDefRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchUserTaskRunRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchVariableRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchWfRunRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchWfSpecRequestModel;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListExternalEventsReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListNodeRunsReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListVariablesReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchUserTaskDefReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import io.littlehorse.server.streamsimpl.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streamsimpl.taskqueue.TaskQueueManager;
import io.littlehorse.server.streamsimpl.util.GETStreamObserver;
import io.littlehorse.server.streamsimpl.util.GETStreamObserverNew;
import io.littlehorse.server.streamsimpl.util.HealthService;
import io.littlehorse.server.streamsimpl.util.POSTStreamObserver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
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
                // the core command topic. With the EOS for the core topology,
                // that means processing will block until the commit() of the
                // timer, which means latency will jump from 15ms to >100ms
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
    public void getWfSpec(WfSpecId req, StreamObserver<GetWfSpecResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, WfSpecModel.class,
                GetWfSpecResponse.class, config);
        internalComms.getStoreBytesAsync(
                ServerTopology.METADATA_STORE,
                StoreUtils.getFullStoreKey(new WfSpecIdModel(req.getName(), req.getVersion()), WfSpecModel.class),
                LHConstants.META_PARTITION_KEY,
                observer);
    }

    @Override
    public void getLatestWfSpec(GetLatestWfSpecRequest req, StreamObserver<GetWfSpecResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, WfSpecModel.class,
                GetWfSpecResponse.class, config);
        internalComms.getLastFromPrefixAsync(
                StoreUtils.getFullPrefixByName(req.getName(), WfSpecModel.class),
                LHConstants.META_PARTITION_KEY,
                observer,
                ServerTopology.METADATA_STORE);
    }

    @Override
    public void getLatestUserTaskDef(GetLatestUserTaskDefRequest req, StreamObserver<GetUserTaskDefResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, UserTaskDefModel.class,
                GetUserTaskDefResponse.class, config);

        // TODO MVP-140: Remove StoreUtils.java. Then in here we would pass in
        // a GetableClassEnum.
        internalComms.getLastFromPrefixAsync(
                StoreUtils.getFullPrefixByName(req.getName(), UserTaskDefModel.class),
                LHConstants.META_PARTITION_KEY,
                observer,
                ServerTopology.METADATA_STORE);
    }

    @Override
    public void getUserTaskDef(UserTaskDefId req, StreamObserver<GetUserTaskDefResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, UserTaskDefModel.class,
                GetUserTaskDefResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.METADATA_STORE,
                StoreUtils.getFullStoreKey(
                        new UserTaskDefIdModel(req.getName(), req.getVersion()), UserTaskDefModel.class),
                LHConstants.META_PARTITION_KEY,
                observer);
    }

    @Override
    public void getTaskDef(TaskDefId req, StreamObserver<GetTaskDefResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, TaskDefModel.class,
                GetTaskDefResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.METADATA_STORE,
                StoreUtils.getFullStoreKey(new TaskDefIdModel(req.getName()), TaskDefModel.class),
                LHConstants.META_PARTITION_KEY,
                observer);
    }

    @Override
    public void getExternalEventDef(ExternalEventDefId req, StreamObserver<GetExternalEventDefResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, ExternalEventDefModel.class,
                GetExternalEventDefResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.METADATA_STORE,
                StoreUtils.getFullStoreKey(new ExternalEventDefIdModel(req.getName()), ExternalEventDefModel.class),
                LHConstants.META_PARTITION_KEY,
                observer);
    }

    @Override
    public void putTaskDef(
            io.littlehorse.sdk.common.proto.PutTaskDefRequest req, StreamObserver<PutTaskDefResponse> ctx) {
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
    public void assignUserTaskRun(AssignUserTaskRunRequest req, StreamObserver<AssignUserTaskRunResponse> ctx) {
        processCommand(req, ctx, AssignUserTaskRunRequestModel.class, AssignUserTaskRunResponse.class);
    }

    @Override
    public void completeUserTaskRun(CompleteUserTaskRunRequest req, StreamObserver<CompleteUserTaskRunResponse> ctx) {
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
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, WfRunModel.class,
                GetWfRunResponse.class, config);
        WfRunIdModel id = LHSerializable.fromProto(req, WfRunIdModel.class);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE, StoreUtils.getFullStoreKey(id, WfRunModel.class), req.getId(), observer);
    }

    @Override
    public void getNodeRun(NodeRunId req, StreamObserver<GetNodeRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, NodeRunModel.class,
                GetNodeRunResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(
                        new NodeRunIdModel(req.getWfRunId(), req.getThreadRunNumber(), req.getPosition()),
                        NodeRunModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void getTaskRun(TaskRunId req, StreamObserver<GetTaskRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, TaskRunModel.class,
                GetTaskRunResponse.class, config);

        TaskRunIdModel taskRunId = LHSerializable.fromProto(req, TaskRunIdModel.class);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(taskRunId.getStoreKey(), TaskRunModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void getUserTaskRun(UserTaskRunId req, StreamObserver<GetUserTaskRunResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, UserTaskRunModel.class,
                GetUserTaskRunResponse.class, config);

        UserTaskRunIdModel userTaskRunId = LHSerializable.fromProto(req, UserTaskRunIdModel.class);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(userTaskRunId.getStoreKey(), UserTaskRunModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void taskDefMetrics(TaskDefMetricsQueryRequest req, StreamObserver<TaskDefMetricsResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, TaskDefMetricsModel.class,
                TaskDefMetricsResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_REPARTITION_STORE,
                StoreUtils.getFullStoreKey(TaskDefMetricsModel.getObjectId(req), TaskDefMetricsModel.class),
                req.getTaskDefName(),
                observer);
    }

    @Override
    public void wfSpecMetrics(WfSpecMetricsQueryRequest req, StreamObserver<WfSpecMetricsResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserver<>(ctx, WfSpecMetricsModel.class,
                WfSpecMetricsResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_REPARTITION_STORE,
                StoreUtils.getFullStoreKey(WfSpecMetricsModel.getObjectId(req), WfSpecMetricsModel.class),
                req.getWfSpecName(),
                observer);
    }

    @Override
    public void getVariable(VariableId req, StreamObserver<GetVariableResponse> ctx) {
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, VariableModel.class,
                GetVariableResponse.class, config);

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
        StreamObserver<CentralStoreQueryResponse> observer = new GETStreamObserverNew<>(ctx, ExternalEventModel.class,
                GetExternalEventResponse.class, config);

        internalComms.getStoreBytesAsync(
                ServerTopology.CORE_STORE,
                StoreUtils.getFullStoreKey(
                        new ExternalEventIdModel(req.getWfRunId(), req.getExternalEventDefName(), req.getGuid()),
                        ExternalEventModel.class),
                req.getWfRunId(),
                observer);
    }

    @Override
    public void searchWfRun(SearchWfRunRequest req, StreamObserver<SearchWfRunResponse> ctx) {
        handleScan(SearchWfRunRequestModel.fromProto(req), ctx, SearchWfRunReply.class);
    }

    @Override
    public void searchExternalEvent(SearchExternalEventRequest req, StreamObserver<SearchExternalEventResponse> ctx) {
        SearchExternalEventRequestModel see = LHSerializable.fromProto(req, SearchExternalEventRequestModel.class);
        handleScan(see, ctx, SearchExternalEventReply.class);
    }

    @Override
    public void searchNodeRun(SearchNodeRunRequest req, StreamObserver<SearchNodeRunResponse> ctx) {
        handleScan(SearchNodeRunRequestModel.fromProto(req), ctx, SearchNodeRunReply.class);
    }

    @Override
    public void searchTaskRun(SearchTaskRunRequest req, StreamObserver<SearchTaskRunResponse> ctx) {
        handleScan(SearchTaskRunRequestModel.fromProto(req), ctx, SearchTaskRunReply.class);
    }

    @Override
    public void searchUserTaskRun(SearchUserTaskRunRequest req, StreamObserver<SearchUserTaskRunResponse> ctx) {
        handleScan(SearchUserTaskRunRequestModel.fromProto(req), ctx, SearchUserTaskRunReply.class);
    }

    @Override
    public void searchVariable(SearchVariableRequest req, StreamObserver<SearchVariableResponse> ctx) {
        handleScan(SearchVariableRequestModel.fromProto(req), ctx, SearchVariableReply.class);
    }

    @Override
    public void searchTaskDef(
            io.littlehorse.sdk.common.proto.SearchTaskDefRequest req, StreamObserver<SearchTaskDefResponse> ctx) {
        handleScan(SearchTaskDefRequestModel.fromProto(req), ctx, SearchTaskDefReply.class);
    }

    @Override
    public void searchUserTaskDef(SearchUserTaskDefRequest req, StreamObserver<SearchUserTaskDefResponse> ctx) {
        handleScan(SearchUserTaskDefRequestModel.fromProto(req), ctx, SearchUserTaskDefReply.class);
    }

    @Override
    public void searchWfSpec(SearchWfSpecRequest req, StreamObserver<SearchWfSpecResponse> ctx) {
        handleScan(SearchWfSpecRequestModel.fromProto(req), ctx, SearchWfSpecReply.class);
    }

    @Override
    public void searchExternalEventDef(
            SearchExternalEventDefRequest req, StreamObserver<SearchExternalEventDefResponse> ctx) {
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
    private <T extends Message, RP extends Message, OP extends Message, OJ extends LHSerializable<OP>, R extends PublicScanReply<RP, OP, OJ>> void handleScan(
            PublicScanRequest<T, RP, OP, OJ, R> req, StreamObserver<RP> ctx, Class<R> replyCls) {
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
            out.code = LHResponseCode.OK;
            if (raw.hasUpdatedBookmark()) {
                out.bookmark = raw.getUpdatedBookmark().toByteString();
            }
            for (ByteString responseEntry : raw.getResultsList()) {
                out.results.add(
                        LHSerializable.fromBytes(responseEntry.toByteArray(), out.getResultJavaClass(), config));
            }
        } catch (LHSerdeError | LHConnectionError exn) {
            log.error("Error with interactive query between instances", exn);
            out.code = LHResponseCode.CONNECTION_ERROR;
            out.message = "Failed connecting to backend: " + exn.getMessage();
        } catch (LHValidationError exn) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "Failed validating search request: " + exn.getMessage();
        }

        ctx.onNext((RP) out.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void listNodeRuns(ListNodeRunsRequest req, StreamObserver<ListNodeRunsResponse> ctx) {
        ListNodeRunsRequestModel lnr = LHSerializable.fromProto(req, ListNodeRunsRequestModel.class);
        handleScan(lnr, ctx, ListNodeRunsReply.class);
    }

    @Override
    public void listVariables(ListVariablesRequest req, StreamObserver<ListVariablesResponse> ctx) {
        ListVariablesRequestModel lv = LHSerializable.fromProto(req, ListVariablesRequestModel.class);
        handleScan(lv, ctx, ListVariablesReply.class);
    }

    @Override
    public void listExternalEvents(ListExternalEventsRequest req, StreamObserver<ListExternalEventsResponse> ctx) {
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
    public void stopWfRun(StopWfRunRequest req, StreamObserver<StopWfRunResponse> ctx) {
        processCommand(req, ctx, StopWfRunRequestModel.class, StopWfRunResponse.class);
    }

    @Override
    public void resumeWfRun(ResumeWfRunRequest req, StreamObserver<ResumeWfRunResponse> ctx) {
        processCommand(req, ctx, ResumeWfRunRequestModel.class, ResumeWfRunResponse.class);
    }

    @Override
    public void deleteWfRun(DeleteWfRunRequest req, StreamObserver<DeleteObjectResponse> ctx) {
        processCommand(req, ctx, DeleteWfRunRequestModel.class, DeleteObjectResponse.class);
    }

    @Override
    public void deleteWfSpec(DeleteWfSpecRequest req, StreamObserver<DeleteObjectResponse> ctx) {
        processMetadataCommand(req, ctx, DeleteWfSpecRequestModel.class, DeleteObjectResponse.class);
    }

    @Override
    public void deleteTaskDef(DeleteTaskDefRequest req, StreamObserver<DeleteObjectResponse> ctx) {
        processMetadataCommand(req, ctx, DeleteTaskDefRequestModel.class, DeleteObjectResponse.class);
    }

    @Override
    public void deleteUserTaskDef(DeleteUserTaskDefRequest req, StreamObserver<DeleteObjectResponse> ctx) {
        processMetadataCommand(req, ctx, DeleteUserTaskDefRequestModel.class, DeleteObjectResponse.class);
    }

    @Override
    public void deleteExternalEventDef(DeleteExternalEventDefRequest req, StreamObserver<DeleteObjectResponse> ctx) {
        processMetadataCommand(req, ctx, DeleteExternalEventDefRequestModel.class, DeleteObjectResponse.class);
    }

    @Override
    public void healthCheck(HealthCheckRequest req, StreamObserver<HealthCheckResponse> ctx) {
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
        processCommand(
                claimEvent.toProto().build(),
                client.getResponseObserver(),
                TaskClaimEvent.class,
                PollTaskResponse.class,
                false, // it's a stream, so we don't want to complete it.,
                config.getCoreCmdTopicName());
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

    private <U extends Message, T extends SubCommand<U>, V extends Message> void processCommand(
            U request, StreamObserver<V> responseObserver, Class<T> subCmdCls, Class<V> responseCls) {
        processCommand(request, responseObserver, subCmdCls, responseCls, true, config.getCoreCmdTopicName());
    }

    private <U extends Message, T extends SubCommand<U>, V extends Message> void processMetadataCommand(
            U request, StreamObserver<V> responseObserver, Class<T> subCmdCls, Class<V> responseCls) {
        processCommand(request, responseObserver, subCmdCls, responseCls, true, config.getMetadataCmdTopicName());
    }

    private <U extends Message, T extends SubCommand<U>, V extends Message> void processCommand(
            U request,
            StreamObserver<V> responseObserver,
            Class<T> subCmdCls,
            Class<V> responseCls,
            boolean shouldComplete, // TODO: Document this
            String topicName) {
        T subCmd = LHSerializable.fromProto(request, subCmdCls);
        CommandModel command = new CommandModel(subCmd);
        command.commandId = LHUtil.generateGuid();
        StreamObserver<WaitForCommandResponse> observer = new POSTStreamObserver<>(responseObserver, responseCls,
                shouldComplete);

        // Now actually record the command.
        internalComms
                .getProducer()
                .send(
                        command.getPartitionKey(), // partition key
                        command, // payload
                        topicName, // topic name
                        (meta, exn) -> { // callback
                            if (exn != null) {
                                // Then we report back to the observer that we failed to record
                                // the command.
                                observer.onNext(WaitForCommandResponse.newBuilder()
                                        .setCode(StoreQueryStatusPb.RSQ_NOT_AVAILABLE)
                                        .setMessage("Failed recording command to Kafka: " + exn.getMessage())
                                        .build());
                                // EMPLOYEE_TODO: determine whether or not to use onError()
                                // instead.
                                observer.onCompleted();
                            } else {
                                // Now we wait for the processing
                                internalComms.waitForCommand(command, observer);
                            }
                        });
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

    public HostInfo getAdvertisedHost(HostModel host, String listenerName) throws LHBadRequestError, LHConnectionError {
        return internalComms.getAdvertisedHost(host, listenerName);
    }
}

package io.littlehorse.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRun;
import io.littlehorse.common.model.command.subcommand.CancelUserTaskRun;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRun;
import io.littlehorse.common.model.command.subcommand.DeleteExternalEventDef;
import io.littlehorse.common.model.command.subcommand.DeleteTaskDef;
import io.littlehorse.common.model.command.subcommand.DeleteUserTaskDef;
import io.littlehorse.common.model.command.subcommand.DeleteWfRun;
import io.littlehorse.common.model.command.subcommand.DeleteWfSpec;
import io.littlehorse.common.model.command.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.command.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.model.command.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.command.subcommand.ResumeWfRun;
import io.littlehorse.common.model.command.subcommand.RunWf;
import io.littlehorse.common.model.command.subcommand.StopWfRun;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskWorkerHeartBeat;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.metrics.TaskDefMetricsModel;
import io.littlehorse.common.model.metrics.WfSpecMetricsModel;
import io.littlehorse.common.model.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.TaskDefIdModel;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.objectId.VariableIdModel;
import io.littlehorse.common.model.objectId.WfRunIdModel;
import io.littlehorse.common.model.objectId.WfSpecIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.ScheduledTaskModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunModel;
import io.littlehorse.common.proto.CentralStoreQueryReplyPb;
import io.littlehorse.common.proto.InternalScanReplyPb;
import io.littlehorse.common.proto.StoreQueryStatusPb;
import io.littlehorse.common.proto.WaitForCommandReplyPb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunPb;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb;
import io.littlehorse.sdk.common.proto.DeleteObjectReplyPb;
import io.littlehorse.sdk.common.proto.DeleteTaskDefPb;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb;
import io.littlehorse.sdk.common.proto.DeleteWfRunPb;
import io.littlehorse.sdk.common.proto.DeleteWfSpecPb;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb;
import io.littlehorse.sdk.common.proto.GetExternalEventReplyPb;
import io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecPb;
import io.littlehorse.sdk.common.proto.GetNodeRunReplyPb;
import io.littlehorse.sdk.common.proto.GetTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.GetTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.GetVariableReplyPb;
import io.littlehorse.sdk.common.proto.GetWfRunReplyPb;
import io.littlehorse.sdk.common.proto.GetWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.HealthCheckPb;
import io.littlehorse.sdk.common.proto.HealthCheckReplyPb;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.sdk.common.proto.LHHealthResult;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiImplBase;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.ListExternalEventsPb;
import io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb;
import io.littlehorse.sdk.common.proto.ListNodeRunsPb;
import io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb;
import io.littlehorse.sdk.common.proto.ListTaskMetricsPb;
import io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb;
import io.littlehorse.sdk.common.proto.ListVariablesPb;
import io.littlehorse.sdk.common.proto.ListVariablesReplyPb;
import io.littlehorse.sdk.common.proto.ListWfMetricsPb;
import io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.PutExternalEventDefResponse;
import io.littlehorse.sdk.common.proto.PutExternalEventResponse;
import io.littlehorse.sdk.common.proto.PutTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutUserTaskDefResponse;
import io.littlehorse.sdk.common.proto.PutWfSpecResponse;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb;
import io.littlehorse.sdk.common.proto.ReportTaskReplyPb;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ResumeWfRunPb;
import io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb;
import io.littlehorse.sdk.common.proto.RunWfPb;
import io.littlehorse.sdk.common.proto.RunWfReplyPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventPb;
import io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb;
import io.littlehorse.sdk.common.proto.SearchNodeRunPb;
import io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchVariablePb;
import io.littlehorse.sdk.common.proto.SearchVariableReplyPb;
import io.littlehorse.sdk.common.proto.SearchWfRunPb;
import io.littlehorse.sdk.common.proto.SearchWfRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchWfSpecPb;
import io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb;
import io.littlehorse.sdk.common.proto.StopWfRunPb;
import io.littlehorse.sdk.common.proto.StopWfRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb;
import io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatPb;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb;
import io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb;
import io.littlehorse.server.listener.ListenersManager;
import io.littlehorse.server.streamsimpl.BackendInternalComms;
import io.littlehorse.server.streamsimpl.ServerTopology;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListExternalEvents;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListNodeRuns;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListTaskMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListVariables;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.ListWfMetrics;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchExternalEvent;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchExternalEventDef;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchNodeRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchTaskDef;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchTaskRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchUserTaskDef;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchUserTaskRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchVariable;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchWfRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests.SearchWfSpec;
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
        this.coreStreams =
            new KafkaStreams(
                ServerTopology.initCoreTopology(config, this),
                // Core topology must be EOS
                config.getStreamsConfig("core", true)
            );
        this.timerStreams =
            new KafkaStreams(
                ServerTopology.initTimerTopology(config),
                // We don't want the Timer topology to be EOS. The reason for this
                // has to do with the fact that:
                // a) Timer is idempotent, so it doesn't really matter
                // b) If it's EOS, then there will be transactional records on
                //    the core command topic. With the EOS for the core topology,
                //    that means processing will block until the commit() of the
                //    timer, which means latency will jump from 15ms to >100ms
                config.getStreamsConfig("timer", false)
            );
        this.healthService = new HealthService(config, coreStreams, timerStreams);

        Executor networkThreadpool = Executors.newFixedThreadPool(
            config.getNumNetworkThreads()
        );
        this.listenerManager =
            new ListenersManager(
                config,
                this,
                networkThreadpool,
                healthService.getMeterRegistry()
            );

        this.internalComms =
            new BackendInternalComms(
                config,
                coreStreams,
                timerStreams,
                networkThreadpool
            );
    }

    public String getInstanceId() {
        return config.getLHInstanceId();
    }

    @Override
    public void getWfSpec(WfSpecId req, StreamObserver<GetWfSpecReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            WfSpecModel.class,
            GetWfSpecReplyPb.class,
            config
        );
        internalComms.getStoreBytesAsync(
            ServerTopology.METADATA_STORE,
            StoreUtils.getFullStoreKey(
                new WfSpecIdModel(req.getName(), req.getVersion()),
                WfSpecModel.class
            ),
            LHConstants.META_PARTITION_KEY,
            observer
        );
    }

    @Override
    public void getLatestWfSpec(
        GetLatestWfSpecPb req,
        StreamObserver<GetWfSpecReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            WfSpecModel.class,
            GetWfSpecReplyPb.class,
            config
        );
        internalComms.getLastFromPrefixAsync(
            StoreUtils.getFullPrefixByName(req.getName(), WfSpecModel.class),
            LHConstants.META_PARTITION_KEY,
            observer,
            ServerTopology.METADATA_STORE
        );
    }

    @Override
    public void getLatestUserTaskDef(
        GetLatestUserTaskDefPb req,
        StreamObserver<GetUserTaskDefReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            UserTaskDefModel.class,
            GetUserTaskDefReplyPb.class,
            config
        );

        // TODO MVP-140: Remove StoreUtils.java. Then in here we would pass in
        // a GetableClassEnum.
        internalComms.getLastFromPrefixAsync(
            StoreUtils.getFullPrefixByName(req.getName(), UserTaskDefModel.class),
            LHConstants.META_PARTITION_KEY,
            observer,
            ServerTopology.METADATA_STORE
        );
    }

    @Override
    public void getUserTaskDef(
        UserTaskDefId req,
        StreamObserver<GetUserTaskDefReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            UserTaskDefModel.class,
            GetUserTaskDefReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.METADATA_STORE,
            StoreUtils.getFullStoreKey(
                new UserTaskDefIdModel(req.getName(), req.getVersion()),
                UserTaskDefModel.class
            ),
            LHConstants.META_PARTITION_KEY,
            observer
        );
    }

    @Override
    public void getTaskDef(TaskDefId req, StreamObserver<GetTaskDefReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            TaskDefModel.class,
            GetTaskDefReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.METADATA_STORE,
            StoreUtils.getFullStoreKey(
                new TaskDefIdModel(req.getName()),
                TaskDefModel.class
            ),
            LHConstants.META_PARTITION_KEY,
            observer
        );
    }

    @Override
    public void getExternalEventDef(
        ExternalEventDefId req,
        StreamObserver<GetExternalEventDefReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            ExternalEventDefModel.class,
            GetExternalEventDefReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.METADATA_STORE,
            StoreUtils.getFullStoreKey(
                new ExternalEventDefIdModel(req.getName()),
                ExternalEventDefModel.class
            ),
            LHConstants.META_PARTITION_KEY,
            observer
        );
    }

    @Override
    public void putTaskDef(
        io.littlehorse.sdk.common.proto.PutTaskDefRequest req,
        StreamObserver<PutTaskDefResponse> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            PutTaskDefRequestModel.class,
            PutTaskDefResponse.class
        );
    }

    @Override
    public void putExternalEvent(
        io.littlehorse.sdk.common.proto.PutExternalEventRequest req,
        StreamObserver<PutExternalEventResponse> ctx
    ) {
        processCommand(
            req,
            ctx,
            PutExternalEventRequestModel.class,
            PutExternalEventResponse.class
        );
    }

    @Override
    public void putExternalEventDef(
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest req,
        StreamObserver<PutExternalEventDefResponse> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            PutExternalEventDefRequestModel.class,
            PutExternalEventDefResponse.class
        );
    }

    @Override
    public void putUserTaskDef(
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest req,
        StreamObserver<PutUserTaskDefResponse> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            PutUserTaskDefRequestModel.class,
            PutUserTaskDefResponse.class
        );
    }

    @Override
    public void assignUserTaskRun(
        AssignUserTaskRunPb req,
        StreamObserver<AssignUserTaskRunReplyPb> ctx
    ) {
        processCommand(
            req,
            ctx,
            AssignUserTaskRun.class,
            AssignUserTaskRunReplyPb.class
        );
    }

    @Override
    public void completeUserTaskRun(
        CompleteUserTaskRunPb req,
        StreamObserver<CompleteUserTaskRunReplyPb> ctx
    ) {
        processCommand(
            req,
            ctx,
            CompleteUserTaskRun.class,
            CompleteUserTaskRunReplyPb.class
        );
    }

    @Override
    public void cancelUserTaskRun(
        CancelUserTaskRunPb req,
        StreamObserver<CancelUserTaskRunReplyPb> ctx
    ) {
        processCommand(
            req,
            ctx,
            CancelUserTaskRun.class,
            CancelUserTaskRunReplyPb.class
        );
    }

    @Override
    public void putWfSpec(
        io.littlehorse.sdk.common.proto.PutWfSpecRequest req,
        StreamObserver<PutWfSpecResponse> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            PutWfSpecRequestModel.class,
            PutWfSpecResponse.class
        );
    }

    @Override
    public void runWf(RunWfPb req, StreamObserver<RunWfReplyPb> ctx) {
        processCommand(req, ctx, RunWf.class, RunWfReplyPb.class);
    }

    @Override
    public StreamObserver<PollTaskRequest> pollTask(
        StreamObserver<PollTaskResponse> ctx
    ) {
        return new PollTaskRequestObserver(ctx, taskQueueManager);
    }

    @Override
    public void registerTaskWorker(
        RegisterTaskWorkerPb req,
        StreamObserver<RegisterTaskWorkerReplyPb> responseObserver
    ) {
        log.trace(
            "Receiving RegisterTaskWorkerPb (heartbeat) from: " + req.getClientId()
        );

        TaskWorkerHeartBeatPb heartBeatPb = TaskWorkerHeartBeatPb
            .newBuilder()
            .setClientId(req.getClientId())
            .setListenerName(req.getListenerName())
            .setTaskDefName(req.getTaskDefName())
            .build();

        processCommand(
            heartBeatPb,
            responseObserver,
            TaskWorkerHeartBeat.class,
            RegisterTaskWorkerReplyPb.class
        );
    }

    @Override
    public void reportTask(ReportTaskRun req, StreamObserver<ReportTaskReplyPb> ctx) {
        processCommand(req, ctx, ReportTaskRunModel.class, ReportTaskReplyPb.class);
    }

    @Override
    public void getWfRun(WfRunId req, StreamObserver<GetWfRunReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            WfRunModel.class,
            GetWfRunReplyPb.class,
            config
        );
        WfRunIdModel id = LHSerializable.fromProto(req, WfRunIdModel.class);

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(id, WfRunModel.class),
            req.getId(),
            observer
        );
    }

    @Override
    public void getNodeRun(NodeRunId req, StreamObserver<GetNodeRunReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            NodeRunModel.class,
            GetNodeRunReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(
                new NodeRunIdModel(
                    req.getWfRunId(),
                    req.getThreadRunNumber(),
                    req.getPosition()
                ),
                NodeRunModel.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void getTaskRun(TaskRunId req, StreamObserver<GetTaskRunReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            TaskRunModel.class,
            GetTaskRunReplyPb.class,
            config
        );

        TaskRunIdModel taskRunId = LHSerializable.fromProto(
            req,
            TaskRunIdModel.class
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(taskRunId.getStoreKey(), TaskRunModel.class),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void getUserTaskRun(
        UserTaskRunId req,
        StreamObserver<GetUserTaskRunReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            UserTaskRunModel.class,
            GetUserTaskRunReplyPb.class,
            config
        );

        UserTaskRunIdModel userTaskRunId = LHSerializable.fromProto(
            req,
            UserTaskRunIdModel.class
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(
                userTaskRunId.getStoreKey(),
                UserTaskRunModel.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void taskDefMetrics(
        TaskDefMetricsQueryPb req,
        StreamObserver<TaskDefMetricsReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            TaskDefMetricsModel.class,
            TaskDefMetricsReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_REPARTITION_STORE,
            StoreUtils.getFullStoreKey(
                TaskDefMetricsModel.getObjectId(req),
                TaskDefMetricsModel.class
            ),
            req.getTaskDefName(),
            observer
        );
    }

    @Override
    public void wfSpecMetrics(
        WfSpecMetricsQueryPb req,
        StreamObserver<WfSpecMetricsReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserver<>(
            ctx,
            WfSpecMetricsModel.class,
            WfSpecMetricsReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_REPARTITION_STORE,
            StoreUtils.getFullStoreKey(
                WfSpecMetricsModel.getObjectId(req),
                WfSpecMetricsModel.class
            ),
            req.getWfSpecName(),
            observer
        );
    }

    @Override
    public void getVariable(VariableId req, StreamObserver<GetVariableReplyPb> ctx) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            VariableModel.class,
            GetVariableReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(
                new VariableIdModel(
                    req.getWfRunId(),
                    req.getThreadRunNumber(),
                    req.getName()
                ),
                VariableModel.class
            ),
            req.getWfRunId(),
            observer
        );
    }

    @Override
    public void getExternalEvent(
        ExternalEventId req,
        StreamObserver<GetExternalEventReplyPb> ctx
    ) {
        StreamObserver<CentralStoreQueryReplyPb> observer = new GETStreamObserverNew<>(
            ctx,
            ExternalEventModel.class,
            GetExternalEventReplyPb.class,
            config
        );

        internalComms.getStoreBytesAsync(
            ServerTopology.CORE_STORE,
            StoreUtils.getFullStoreKey(
                new ExternalEventIdModel(
                    req.getWfRunId(),
                    req.getExternalEventDefName(),
                    req.getGuid()
                ),
                ExternalEventModel.class
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
        handleScan(SearchWfRun.fromProto(req), ctx, SearchWfRunReply.class);
    }

    @Override
    public void searchExternalEvent(
        SearchExternalEventPb req,
        StreamObserver<SearchExternalEventReplyPb> ctx
    ) {
        SearchExternalEvent see = LHSerializable.fromProto(
            req,
            SearchExternalEvent.class
        );
        handleScan(see, ctx, SearchExternalEventReply.class);
    }

    @Override
    public void searchNodeRun(
        SearchNodeRunPb req,
        StreamObserver<SearchNodeRunReplyPb> ctx
    ) {
        handleScan(SearchNodeRun.fromProto(req), ctx, SearchNodeRunReply.class);
    }

    @Override
    public void searchTaskRun(
        SearchTaskRunPb req,
        StreamObserver<SearchTaskRunReplyPb> ctx
    ) {
        handleScan(SearchTaskRun.fromProto(req), ctx, SearchTaskRunReply.class);
    }

    @Override
    public void searchUserTaskRun(
        SearchUserTaskRunPb req,
        StreamObserver<SearchUserTaskRunReplyPb> ctx
    ) {
        handleScan(
            SearchUserTaskRun.fromProto(req),
            ctx,
            SearchUserTaskRunReply.class
        );
    }

    @Override
    public void searchVariable(
        SearchVariablePb req,
        StreamObserver<SearchVariableReplyPb> ctx
    ) {
        handleScan(SearchVariable.fromProto(req), ctx, SearchVariableReply.class);
    }

    @Override
    public void searchTaskDef(
        SearchTaskDefPb req,
        StreamObserver<SearchTaskDefReplyPb> ctx
    ) {
        handleScan(SearchTaskDef.fromProto(req), ctx, SearchTaskDefReply.class);
    }

    @Override
    public void searchUserTaskDef(
        SearchUserTaskDefPb req,
        StreamObserver<SearchUserTaskDefReplyPb> ctx
    ) {
        handleScan(
            SearchUserTaskDef.fromProto(req),
            ctx,
            SearchUserTaskDefReply.class
        );
    }

    @Override
    public void searchWfSpec(
        SearchWfSpecPb req,
        StreamObserver<SearchWfSpecReplyPb> ctx
    ) {
        handleScan(SearchWfSpec.fromProto(req), ctx, SearchWfSpecReply.class);
    }

    @Override
    public void searchExternalEventDef(
        SearchExternalEventDefPb req,
        StreamObserver<SearchExternalEventDefReplyPb> ctx
    ) {
        handleScan(
            SearchExternalEventDef.fromProto(req),
            ctx,
            SearchExternalEventDefReply.class
        );
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
        R extends PublicScanReply<RP, OP, OJ>
    > void handleScan(
        PublicScanRequest<T, RP, OP, OJ, R> req,
        StreamObserver<RP> ctx,
        Class<R> replyCls
    ) {
        R out;
        try {
            out = replyCls.getDeclaredConstructor().newInstance();
        } catch (
            NoSuchMethodException
            | InvocationTargetException
            | InstantiationException
            | IllegalAccessException exn
        ) {
            ctx.onError(exn);
            return;
        }

        try {
            InternalScanReplyPb raw = internalComms.doScan(
                req.getInternalSearch(internalComms.getGlobalStoreImpl())
            );
            out.code = LHResponseCode.OK;
            if (raw.hasUpdatedBookmark()) {
                out.bookmark = raw.getUpdatedBookmark().toByteString();
            }
            for (ByteString responseEntry : raw.getResultsList()) {
                out.results.add(
                    LHSerializable.fromBytes(
                        responseEntry.toByteArray(),
                        out.getResultJavaClass(),
                        config
                    )
                );
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
    public void listNodeRuns(
        ListNodeRunsPb req,
        StreamObserver<ListNodeRunsReplyPb> ctx
    ) {
        ListNodeRuns lnr = LHSerializable.fromProto(req, ListNodeRuns.class);
        handleScan(lnr, ctx, ListNodeRunsReply.class);
    }

    @Override
    public void listVariables(
        ListVariablesPb req,
        StreamObserver<ListVariablesReplyPb> ctx
    ) {
        ListVariables lv = LHSerializable.fromProto(req, ListVariables.class);
        handleScan(lv, ctx, ListVariablesReply.class);
    }

    @Override
    public void listExternalEvents(
        ListExternalEventsPb req,
        StreamObserver<ListExternalEventsReplyPb> ctx
    ) {
        ListExternalEvents lv = LHSerializable.fromProto(
            req,
            ListExternalEvents.class
        );
        handleScan(lv, ctx, ListExternalEventsReply.class);
    }

    @Override
    public void listTaskDefMetrics(
        ListTaskMetricsPb req,
        StreamObserver<ListTaskMetricsReplyPb> ctx
    ) {
        ListTaskMetrics ltm = LHSerializable.fromProto(req, ListTaskMetrics.class);
        handleScan(ltm, ctx, ListTaskMetricsReply.class);
    }

    @Override
    public void listWfSpecMetrics(
        ListWfMetricsPb req,
        StreamObserver<ListWfMetricsReplyPb> ctx
    ) {
        ListWfMetrics ltm = LHSerializable.fromProto(req, ListWfMetrics.class);
        handleScan(ltm, ctx, ListWfMetricsReply.class);
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
        StreamObserver<DeleteObjectReplyPb> ctx
    ) {
        processCommand(req, ctx, DeleteWfRun.class, DeleteObjectReplyPb.class);
    }

    @Override
    public void deleteWfSpec(
        DeleteWfSpecPb req,
        StreamObserver<DeleteObjectReplyPb> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            DeleteWfSpec.class,
            DeleteObjectReplyPb.class
        );
    }

    @Override
    public void deleteTaskDef(
        DeleteTaskDefPb req,
        StreamObserver<DeleteObjectReplyPb> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            DeleteTaskDef.class,
            DeleteObjectReplyPb.class
        );
    }

    @Override
    public void deleteUserTaskDef(
        DeleteUserTaskDefPb req,
        StreamObserver<DeleteObjectReplyPb> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            DeleteUserTaskDef.class,
            DeleteObjectReplyPb.class
        );
    }

    @Override
    public void deleteExternalEventDef(
        DeleteExternalEventDefPb req,
        StreamObserver<DeleteObjectReplyPb> ctx
    ) {
        processMetadataCommand(
            req,
            ctx,
            DeleteExternalEventDef.class,
            DeleteObjectReplyPb.class
        );
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

    public void returnTaskToClient(
        ScheduledTaskModel scheduledTask,
        PollTaskRequestObserver client
    ) {
        TaskClaimEvent claimEvent = new TaskClaimEvent(scheduledTask, client);
        processCommand(
            claimEvent.toProto().build(),
            client.getResponseObserver(),
            TaskClaimEvent.class,
            PollTaskResponse.class,
            false, // it's a stream, so we don't want to complete it.,
            config.getCoreCmdTopicName()
        );
    }

    public LHProducer getProducer() {
        return internalComms.getProducer();
    }

    public void onResponseReceived(String commandId, WaitForCommandReplyPb response) {
        internalComms.onResponseReceived(commandId, response);
    }

    public void sendErrorToClient(String commandId, Exception caught) {
        internalComms.sendErrorToClientForCommand(commandId, caught);
    }

    private <
        U extends Message, T extends SubCommand<U>, V extends Message
    > void processCommand(
        U request,
        StreamObserver<V> responseObserver,
        Class<T> subCmdCls,
        Class<V> responseCls
    ) {
        processCommand(
            request,
            responseObserver,
            subCmdCls,
            responseCls,
            true,
            config.getCoreCmdTopicName()
        );
    }

    private <
        U extends Message, T extends SubCommand<U>, V extends Message
    > void processMetadataCommand(
        U request,
        StreamObserver<V> responseObserver,
        Class<T> subCmdCls,
        Class<V> responseCls
    ) {
        processCommand(
            request,
            responseObserver,
            subCmdCls,
            responseCls,
            true,
            config.getMetadataCmdTopicName()
        );
    }

    private <
        U extends Message, T extends SubCommand<U>, V extends Message
    > void processCommand(
        U request,
        StreamObserver<V> responseObserver,
        Class<T> subCmdCls,
        Class<V> responseCls,
        boolean shouldComplete, // TODO: Document this
        String topicName
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
                topicName, // topic name
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

    public void onTaskScheduled(
        TaskDefIdModel taskDef,
        ScheduledTaskModel scheduledTask
    ) {
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

    public static void doMain(LHConfig config)
        throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        KafkaStreamsServerImpl server = new KafkaStreamsServerImpl(config);
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    log.info("Closing now!");
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

        latch.await();
        log.info("Done waiting for countdown latch");
    }

    public Set<HostModel> getAllInternalHosts() {
        return internalComms.getAllInternalHosts();
    }

    public HostInfo getAdvertisedHost(HostModel host, String listenerName)
        throws LHBadRequestError, LHConnectionError {
        return internalComms.getAdvertisedHost(host, listenerName);
    }
}

package io.littlehorse.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
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
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.MigrateWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.proto.WaitForCommandResponse;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventDefIdList;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.ExternalEventIdList;
import io.littlehorse.sdk.common.proto.ExternalEventList;
import io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.ListExternalEventsRequest;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.ListTaskMetricsRequest;
import io.littlehorse.sdk.common.proto.ListTaskMetricsResponse;
import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.ListWfMetricsRequest;
import io.littlehorse.sdk.common.proto.ListWfMetricsResponse;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseImplBase;
import io.littlehorse.sdk.common.proto.MigrateWfSpecRequest;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.sdk.common.proto.NodeRunList;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.SearchExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchNodeRunRequest;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchVariableRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.SearchWfSpecRequest;
import io.littlehorse.sdk.common.proto.ServerVersionResponse;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefIdList;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskRunIdList;
import io.littlehorse.sdk.common.proto.TaskRunList;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import io.littlehorse.sdk.common.proto.UserTaskDefIdList;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunIdList;
import io.littlehorse.sdk.common.proto.UserTaskRunList;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.VariableIdList;
import io.littlehorse.sdk.common.proto.VariableList;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.common.proto.WfSpecIdList;
import io.littlehorse.server.auth.InternalCallCredentials;
import io.littlehorse.server.listener.ListenersManager;
import io.littlehorse.server.monitoring.HealthService;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListExternalEventsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListNodeRunsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListTaskMetricsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListTaskRunsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListUserTaskRunRequestModel;
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
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskRunsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListUserTaskRunReply;
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
import io.littlehorse.server.streams.taskqueue.ClusterHealthRequestObserver;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import io.littlehorse.server.streams.util.POSTStreamObserver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class KafkaStreamsServerImpl extends LittleHorseImplBase {

    private LHServerConfig config;
    private TaskQueueManager taskQueueManager;

    private KafkaStreams coreStreams;
    private KafkaStreams timerStreams;

    private BackendInternalComms internalComms;

    private ListenersManager listenerManager;
    private HealthService healthService;
    private Context.Key<RequestExecutionContext> contextKey = Context.key("executionContextKey");

    private static final boolean ENABLE_STALE_STORES = true;

    private RequestExecutionContext requestContext() {

        return contextKey.get();
    }

    public KafkaStreamsServerImpl(LHServerConfig config) {
        MetadataCache metadataCache = new MetadataCache();
        this.config = config;
        this.taskQueueManager = new TaskQueueManager(this);
        this.coreStreams = new KafkaStreams(
                ServerTopology.initCoreTopology(config, this, metadataCache, taskQueueManager),
                config.getCoreStreamsConfig());
        this.timerStreams = new KafkaStreams(ServerTopology.initTimerTopology(config), config.getTimerStreamsConfig());
        this.healthService = new HealthService(config, coreStreams, timerStreams);
        Executor networkThreadpool = Executors.newFixedThreadPool(config.getNumNetworkThreads());
        this.listenerManager = new ListenersManager(
                config,
                this,
                networkThreadpool,
                healthService.getMeterRegistry(),
                metadataCache,
                contextKey,
                this::readOnlyStore);

        this.internalComms = new BackendInternalComms(
                config, coreStreams, timerStreams, networkThreadpool, metadataCache, contextKey, this::readOnlyStore);
    }

    public String getInstanceId() {
        return config.getLHInstanceId();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getWfSpec(WfSpecId req, StreamObserver<WfSpec> ctx) {
        WfSpecIdModel wfSpecId = LHSerializable.fromProto(req, WfSpecIdModel.class, requestContext());
        WfSpecModel wfSpec = requestContext().metadataManager().get(wfSpecId);
        if (wfSpec == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec"));
        } else {
            ctx.onNext(wfSpec.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_PRINCIPAL, actions = ACLAction.WRITE_METADATA)
    public void putPrincipal(PutPrincipalRequest req, StreamObserver<Principal> ctx) {
        PutPrincipalRequestModel reqModel =
                LHSerializable.fromProto(req, PutPrincipalRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Principal.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getLatestWfSpec(GetLatestWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        Integer majorVersion = req.hasMajorVersion() ? req.getMajorVersion() : null;
        WfSpecModel wfSpec = requestContext().service().getWfSpec(req.getName(), majorVersion, null);

        if (wfSpec == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec"));
        } else {
            ctx.onNext(wfSpec.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void getLatestUserTaskDef(GetLatestUserTaskDefRequest req, StreamObserver<UserTaskDef> ctx) {
        UserTaskDefModel utd = getServiceFromContext().getUserTaskDef(req.getName(), null);
        if (utd == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskDef %s".formatted(req.getName())));
        } else {
            ctx.onNext(utd.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void getUserTaskDef(UserTaskDefId req, StreamObserver<UserTaskDef> ctx) {
        UserTaskDefModel utd = getServiceFromContext().getUserTaskDef(req.getName(), req.getVersion());
        if (utd == null) {
            ctx.onError(new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find UserTaskDef %s versoin %d".formatted(req.getName(), req.getVersion())));
        } else {
            ctx.onNext(utd.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.READ)
    public void getTaskDef(TaskDefId req, StreamObserver<TaskDef> ctx) {
        TaskDefModel td = getServiceFromContext().getTaskDef(req.getName());
        if (td == null) {
            ctx.onError(new LHApiException(Status.NOT_FOUND, "Couldn't find TaskDef %s".formatted(req.getName())));
        } else {
            ctx.onNext(td.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void getExternalEventDef(ExternalEventDefId req, StreamObserver<ExternalEventDef> ctx) {
        ExternalEventDefModel eed = getServiceFromContext().getExternalEventDef(req.getName());
        if (eed == null) {
            ctx.onError(
                    new LHApiException(Status.NOT_FOUND, "Couldn't find ExternalEventDef %s".formatted(req.getName())));
        } else {
            ctx.onNext(eed.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void putTaskDef(PutTaskDefRequest req, StreamObserver<TaskDef> ctx) {
        PutTaskDefRequestModel reqModel = LHSerializable.fromProto(req, PutTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, TaskDef.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.RUN)
    public void putExternalEvent(PutExternalEventRequest req, StreamObserver<ExternalEvent> ctx) {
        PutExternalEventRequestModel reqModel =
                LHSerializable.fromProto(req, PutExternalEventRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, ExternalEvent.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.WRITE_METADATA)
    public void putExternalEventDef(PutExternalEventDefRequest req, StreamObserver<ExternalEventDef> ctx) {
        PutExternalEventDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutExternalEventDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, ExternalEventDef.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void putUserTaskDef(PutUserTaskDefRequest req, StreamObserver<UserTaskDef> ctx) {
        PutUserTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutUserTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, UserTaskDef.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void assignUserTaskRun(AssignUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        AssignUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, AssignUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.RUN)
    public void completeUserTaskRun(CompleteUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        CompleteUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, CompleteUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.RUN)
    public void cancelUserTaskRun(CancelUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        CancelUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, CancelUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void putWfSpec(PutWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        PutWfSpecRequestModel reqModel = LHSerializable.fromProto(req, PutWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, WfSpec.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void migrateWfSpec(MigrateWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        MigrateWfSpecRequestModel reqModel =
                LHSerializable.fromProto(req, MigrateWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, WfSpec.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.READ)
    public void listTaskRuns(ListTaskRunsRequest req, StreamObserver<TaskRunList> ctx) {
        ListTaskRunsRequestModel reqModel =
                LHSerializable.fromProto(req, ListTaskRunsRequestModel.class, requestContext());
        handleScan(reqModel, ctx, ListTaskRunsReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void listUserTaskRuns(ListUserTaskRunRequest req, StreamObserver<UserTaskRunList> ctx) {
        ListUserTaskRunRequestModel requestModel =
                LHSerializable.fromProto(req, ListUserTaskRunRequestModel.class, requestContext());
        handleScan(requestModel, ctx, ListUserTaskRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void runWf(RunWfRequest req, StreamObserver<WfRun> ctx) {
        RunWfRequestModel reqModel = LHSerializable.fromProto(req, RunWfRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, WfRun.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.READ)
    public StreamObserver<PollTaskRequest> pollTask(StreamObserver<PollTaskResponse> ctx) {
        return new PollTaskRequestObserver(ctx, taskQueueManager, requestContext());
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void registerTaskWorker(
            RegisterTaskWorkerRequest req, StreamObserver<RegisterTaskWorkerResponse> responseObserver) {
        log.trace("Receiving RegisterTaskWorkerRequest (heartbeat) from: " + req.getTaskWorkerId());

        TaskWorkerHeartBeatRequest heartBeatPb = TaskWorkerHeartBeatRequest.newBuilder()
                .setClientId(req.getTaskWorkerId())
                .setListenerName(req.getListenerName())
                .setTaskDefId(req.getTaskDefId())
                .build();

        TaskWorkerHeartBeatRequestModel heartBeat =
                LHSerializable.fromProto(heartBeatPb, TaskWorkerHeartBeatRequestModel.class, requestContext());

        // TODO: Refactor this, we should create a class for this
        StreamObserver<RegisterTaskWorkerResponse> clusterHealthRequestObserver =
                new ClusterHealthRequestObserver(responseObserver);

        processCommand(
                new CommandModel(heartBeat), clusterHealthRequestObserver, RegisterTaskWorkerResponse.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void reportTask(ReportTaskRun req, StreamObserver<Empty> ctx) {
        ReportTaskRunModel reqModel = LHSerializable.fromProto(req, ReportTaskRunModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getWfRun(WfRunId req, StreamObserver<WfRun> ctx) {
        WfRunIdModel id = LHSerializable.fromProto(req, WfRunIdModel.class, requestContext());
        try {
            WfRunModel wfRun = internalComms.getObject(id, WfRunModel.class, requestContext());
            ctx.onNext(wfRun.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getNodeRun(NodeRunId req, StreamObserver<NodeRun> ctx) {
        NodeRunIdModel id = LHSerializable.fromProto(req, NodeRunIdModel.class, requestContext());
        try {
            NodeRunModel nodeRun = internalComms.getObject(id, NodeRunModel.class, requestContext());
            ctx.onNext(nodeRun.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getTaskRun(TaskRunId req, StreamObserver<TaskRun> ctx) {
        TaskRunIdModel id = LHSerializable.fromProto(req, TaskRunIdModel.class, requestContext());
        try {
            TaskRunModel taskRun = internalComms.getObject(id, TaskRunModel.class, requestContext());
            ctx.onNext(taskRun.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void getUserTaskRun(UserTaskRunId req, StreamObserver<UserTaskRun> ctx) {
        UserTaskRunIdModel id = LHSerializable.fromProto(req, UserTaskRunIdModel.class, requestContext());
        try {
            UserTaskRunModel userTaskRun = internalComms.getObject(id, UserTaskRunModel.class, requestContext());
            ctx.onNext(userTaskRun.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getVariable(VariableId req, StreamObserver<Variable> ctx) {
        VariableIdModel id = LHSerializable.fromProto(req, VariableIdModel.class, requestContext());
        try {
            VariableModel variable = internalComms.getObject(id, VariableModel.class, requestContext());
            ctx.onNext(variable.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void getExternalEvent(ExternalEventId req, StreamObserver<ExternalEvent> ctx) {
        ExternalEventIdModel id = LHSerializable.fromProto(req, ExternalEventIdModel.class, requestContext());
        try {
            ExternalEventModel externalEvent = internalComms.getObject(id, ExternalEventModel.class, requestContext());
            ctx.onNext(externalEvent.toProto().build());
            ctx.onCompleted();
        } catch (Exception exn) {
            if (!LHUtil.isUserError(exn)) log.error("Error handling request", exn);
            ctx.onError(exn);
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TENANT, actions = ACLAction.WRITE_METADATA)
    public void putTenant(PutTenantRequest req, StreamObserver<Tenant> ctx) {
        PutTenantRequestModel reqModel = LHSerializable.fromProto(req, PutTenantRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Tenant.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchWfRun(SearchWfRunRequest req, StreamObserver<WfRunIdList> ctx) {
        handleScan(SearchWfRunRequestModel.fromProto(req, requestContext()), ctx, SearchWfRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void searchExternalEvent(SearchExternalEventRequest req, StreamObserver<ExternalEventIdList> ctx) {
        SearchExternalEventRequestModel see =
                LHSerializable.fromProto(req, SearchExternalEventRequestModel.class, requestContext());
        handleScan(see, ctx, SearchExternalEventReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchNodeRun(SearchNodeRunRequest req, StreamObserver<NodeRunIdList> ctx) {
        handleScan(SearchNodeRunRequestModel.fromProto(req, requestContext()), ctx, SearchNodeRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.READ)
    public void searchTaskRun(SearchTaskRunRequest req, StreamObserver<TaskRunIdList> ctx) {
        handleScan(SearchTaskRunRequestModel.fromProto(req, requestContext()), ctx, SearchTaskRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void searchUserTaskRun(SearchUserTaskRunRequest req, StreamObserver<UserTaskRunIdList> ctx) {
        handleScan(SearchUserTaskRunRequestModel.fromProto(req, requestContext()), ctx, SearchUserTaskRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchVariable(SearchVariableRequest req, StreamObserver<VariableIdList> ctx) {
        handleScan(SearchVariableRequestModel.fromProto(req, requestContext()), ctx, SearchVariableReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchTaskDef(SearchTaskDefRequest req, StreamObserver<TaskDefIdList> ctx) {
        handleScan(SearchTaskDefRequestModel.fromProto(req, requestContext()), ctx, SearchTaskDefReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void searchUserTaskDef(SearchUserTaskDefRequest req, StreamObserver<UserTaskDefIdList> ctx) {
        handleScan(SearchUserTaskDefRequestModel.fromProto(req, requestContext()), ctx, SearchUserTaskDefReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchWfSpec(SearchWfSpecRequest req, StreamObserver<WfSpecIdList> ctx) {
        handleScan(SearchWfSpecRequestModel.fromProto(req, requestContext()), ctx, SearchWfSpecReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void searchExternalEventDef(SearchExternalEventDefRequest req, StreamObserver<ExternalEventDefIdList> ctx) {
        handleScan(
                SearchExternalEventDefRequestModel.fromProto(req, requestContext()),
                ctx,
                SearchExternalEventDefReply.class);
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
            log.error("Failed constructing search reply class", exn);
            ctx.onError(LHUtil.toGrpcError(exn));
            return;
        }

        try {
            InternalScanResponse raw = internalComms.doScan(req.getInternalSearch(requestContext()));
            if (raw.hasUpdatedBookmark()) {
                out.bookmark = raw.getUpdatedBookmark().toByteString();
            }
            for (ByteString responseEntry : raw.getResultsList()) {
                out.results.add(LHSerializable.fromBytes(
                        responseEntry.toByteArray(), out.getResultJavaClass(), requestContext()));
            }
            ctx.onNext((RP) out.toProto().build());
            ctx.onCompleted();
        } catch (StatusRuntimeException exn) {
            ctx.onError(exn);
        } catch (Exception exn) {
            log.error("Failed handling a search", exn);
            ctx.onError(LHUtil.toGrpcError(exn));
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void listNodeRuns(ListNodeRunsRequest req, StreamObserver<NodeRunList> ctx) {
        ListNodeRunsRequestModel lnr = LHSerializable.fromProto(req, ListNodeRunsRequestModel.class, requestContext());
        handleScan(lnr, ctx, ListNodeRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void listVariables(ListVariablesRequest req, StreamObserver<VariableList> ctx) {
        ListVariablesRequestModel lv = LHSerializable.fromProto(req, ListVariablesRequestModel.class, requestContext());
        handleScan(lv, ctx, ListVariablesReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void listExternalEvents(ListExternalEventsRequest req, StreamObserver<ExternalEventList> ctx) {
        ListExternalEventsRequestModel lv =
                LHSerializable.fromProto(req, ListExternalEventsRequestModel.class, requestContext());
        handleScan(lv, ctx, ListExternalEventsReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void listTaskDefMetrics(ListTaskMetricsRequest req, StreamObserver<ListTaskMetricsResponse> ctx) {
        ListTaskMetricsRequestModel ltm =
                LHSerializable.fromProto(req, ListTaskMetricsRequestModel.class, requestContext());
        handleScan(ltm, ctx, ListTaskMetricsReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void listWfSpecMetrics(ListWfMetricsRequest req, StreamObserver<ListWfMetricsResponse> ctx) {
        ListWfMetricsRequestModel ltm =
                LHSerializable.fromProto(req, ListWfMetricsRequestModel.class, requestContext());
        handleScan(ltm, ctx, ListWfMetricsReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void stopWfRun(StopWfRunRequest req, StreamObserver<Empty> ctx) {
        StopWfRunRequestModel reqModel = LHSerializable.fromProto(req, StopWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void resumeWfRun(ResumeWfRunRequest req, StreamObserver<Empty> ctx) {
        ResumeWfRunRequestModel reqModel =
                LHSerializable.fromProto(req, ResumeWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void deleteWfRun(DeleteWfRunRequest req, StreamObserver<Empty> ctx) {
        DeleteWfRunRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void deleteWfSpec(DeleteWfSpecRequest req, StreamObserver<Empty> ctx) {
        DeleteWfSpecRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void deleteTaskDef(DeleteTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void deleteUserTaskDef(DeleteUserTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteUserTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteUserTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class, true);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.WRITE_METADATA)
    public void deleteExternalEventDef(DeleteExternalEventDefRequest req, StreamObserver<Empty> ctx) {
        DeleteExternalEventDefRequestModel deedr =
                LHSerializable.fromProto(req, DeleteExternalEventDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(deedr), ctx, Empty.class, true);
    }

    @Override
    @Authorize(
            resources = {},
            actions = {})
    public void whoami(Empty request, StreamObserver<Principal> responseObserver) {
        RequestExecutionContext requestContext = requestContext();
        AuthorizationContext authorizationContext = requestContext.authorization();
        PrincipalIdModel principalId = authorizationContext.principalId();
        PrincipalModel principal = requestContext.service().getPrincipal(principalId);
        responseObserver.onNext(principal.toProto().build());
        responseObserver.onCompleted();
    }

    @Override
    @Authorize(
            resources = {},
            actions = {})
    public void getServerVersion(Empty request, StreamObserver<ServerVersionResponse> ctx) {
        ctx.onNext(ServerVersionResponse.newBuilder()
                .setMajorVersion(0)
                .setMinorVersion(7)
                .setPatchVersion(2)
                .build());
        ctx.onCompleted();
    }

    /*
     * Sends a command to Kafka and simultaneously does a waitForProcessing() internal
     * grpc call that asynchronously waits for the command to be processed. It
     * infers the request context from the GRPC Context.
     */
    public void returnTaskToClient(ScheduledTaskModel scheduledTask, PollTaskRequestObserver client) {
        TaskClaimEvent claimEvent = new TaskClaimEvent(scheduledTask, client);
        processCommand(
                new CommandModel(claimEvent),
                client.getResponseObserver(),
                PollTaskResponse.class,
                false,
                client.getRequestContext());
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

    /*
     * Sends a command to Kafka and simultaneously does a waitForProcessing() internal
     * grpc call that asynchronously waits for the command to be processed.
     *
     * Explicit request context. Useful for callers who do not have access to the GRPC
     * context, for example the `returnTaskToClient()` method. That method is called
     * from within the CommandProcessor#process() method.
     *
     * REFACTOR_SUGGESTION: We should create a CommandSender.java class which is responsible
     * for sending commands to Kafka and waiting for the execution. That class should
     * not depend on RequestExecutionContext but rather the AuthorizationContext. The
     * `TaskClaimEvent#reportTaskToClient()` flow should not go through KafkaStreamsServerImpl
     * anymore.
     */
    private <AC extends Message, RC extends Message> void processCommand(
            AbstractCommand<AC> command,
            StreamObserver<RC> responseObserver,
            Class<RC> responseCls,
            boolean shouldCompleteStream) {
        RequestExecutionContext requestContext = requestContext();
        processCommand(command, responseObserver, responseCls, shouldCompleteStream, requestContext);
    }

    /*
     * This method is called from within the `CommandProcessor#process()` method (specifically, on the
     * TaskClaimEvent#process()) method. Therefore, we cannot infer the RequestExecutionContext like
     * we do in the other places, because the GRPC context does not exist in this case.
     * Note that this is not a GRPC method that @Override's a super method and takes in
     * a protobuf + StreamObserver.
     */
    private <AC extends Message, RC extends Message> void processCommand(
            AbstractCommand<AC> command,
            StreamObserver<RC> responseObserver,
            Class<RC> responseCls,
            boolean shouldCompleteStream,
            RequestExecutionContext requestContext) {
        StreamObserver<WaitForCommandResponse> commandObserver =
                new POSTStreamObserver<>(responseObserver, responseCls, shouldCompleteStream);

        Callback callback = (meta, exn) -> this.productionCallback(meta, exn, commandObserver, command);

        command.setCommandId(LHUtil.generateGuid());

        Headers commandMetadata = HeadersUtil.metadataHeadersFor(
                requestContext.authorization().tenantId(),
                requestContext.authorization().principalId());
        internalComms
                .getProducer()
                .send(
                        command.getPartitionKey(),
                        command,
                        command.getTopic(config),
                        callback,
                        commandMetadata.toArray());
    }

    public ReadOnlyKeyValueStore<String, Bytes> readOnlyStore(Integer specificPartition, String storeName) {
        StoreQueryParameters<ReadOnlyKeyValueStore<String, Bytes>> params =
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());

        if (ENABLE_STALE_STORES) {
            params = params.enableStaleStores();
        }

        if (specificPartition != null) {
            params = params.withPartition(specificPartition);
        }

        return coreStreams.store(params);
    }

    private WfService getServiceFromContext() {
        return requestContext().service();
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

    public void onTaskScheduled(TaskDefIdModel taskDef, ScheduledTaskModel scheduledTask, TenantIdModel tenantId) {
        taskQueueManager.onTaskScheduled(taskDef, scheduledTask, tenantId);
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

    public static void doMain(LHServerConfig config) throws IOException, InterruptedException {
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

    public LHHostInfo getAdvertisedHost(
            HostModel host, String listenerName, InternalCallCredentials internalCredentials) {
        return internalComms.getAdvertisedHost(host, listenerName, internalCredentials);
    }
}

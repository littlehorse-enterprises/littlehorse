package io.littlehorse.server;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.grpc.Context;
import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteScheduledWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.PutCorrelatedEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.RescueThreadRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SaveUserTaskRunProgressRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ScheduleWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.ScheduledWfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.CorrelatedEventIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.ScheduledWfRunIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskWorkerGroupIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteStructDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWorkflowEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.MigrateWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutStructDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWorkflowEventDefRequestModel;
import io.littlehorse.common.proto.InternalScanResponse;
import io.littlehorse.common.util.InlineStructDefUtil;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseImplBase;
import io.littlehorse.server.listener.ServerListenerConfig;
import io.littlehorse.server.streams.BackendInternalComms;
import io.littlehorse.server.streams.CommandSender;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListExternalEventsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListNodeRunsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListTaskMetricsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListTaskRunsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListUserTaskRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListVariablesRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListWfMetricsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.ListWorkflowEventsRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchExternalEventDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchExternalEventRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchNodeRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchPrincipalRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchScheduledWfRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchTaskDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchTaskRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchTenantRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchUserTaskDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchUserTaskRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchVariableRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWfRunRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWfSpecRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWorkflowEventDefRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicrequests.SearchWorkflowEventRequestModel;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListExternalEventsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListNodeRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskMetricsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListTaskRunsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListUserTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListVariablesReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListWfMetricsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListWorkflowEventsReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchExternalEventReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchNodeRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchPrincipalRequestReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchScheduledWfRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchTenantRequestReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchUserTaskRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchVariableReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfRunReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWfSpecReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWorkflowEventDefReply;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.SearchWorkflowEventReply;
import io.littlehorse.server.streams.taskqueue.ClusterHealthRequestObserver;
import io.littlehorse.server.streams.taskqueue.PollTaskRequestObserver;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.topology.core.WfService;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.errors.InvalidStateStoreException;

/**
 * This class provides the implementation for public RPCs.
 * Any exception thrown by an RPC method within this class will be intercepted and
 * handled by the {@code GlobalExceptionHandler} to ensure consistent error management.
 */
@Slf4j
public class LHServerListener extends LittleHorseImplBase implements Closeable {

    private final Context.Key<RequestExecutionContext> contextKey;
    private final LHServerConfig serverConfig;
    private final TaskQueueManager taskQueueManager;
    private final BackendInternalComms internalComms;
    private final MetadataCache metadataCache;
    private final CoreStoreProvider coreStoreProvider;
    private final String listenerName;
    private final CommandSender commandSender;
    private final Duration successDurationTimeout;
    private final AsyncWaiters asyncWaiters;
    private final LHInternalClient lhInternalClient;

    private Server grpcListener;

    private RequestExecutionContext requestContext() {
        return contextKey.get();
    }

    public LHServerListener(
            ServerListenerConfig listenerConfig,
            TaskQueueManager taskQueueManager,
            BackendInternalComms internalComms,
            ExecutorService networkThreads,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            List<ServerInterceptor> interceptors,
            Context.Key<RequestExecutionContext> contextKey,
            CommandSender commandSender,
            AsyncWaiters asyncWaiters,
            LHInternalClient lhInternalClient) {

        // All dependencies are passed in as arguments; nothing is instantiated here,
        // because all listeners share the same threading infrastructure.
        this.lhInternalClient = lhInternalClient;
        this.asyncWaiters = asyncWaiters;
        this.metadataCache = metadataCache;
        this.serverConfig = listenerConfig.getConfig();
        this.taskQueueManager = taskQueueManager;
        this.coreStoreProvider = coreStoreProvider;
        this.internalComms = internalComms;
        this.listenerName = listenerConfig.getName();
        this.contextKey = contextKey;
        this.successDurationTimeout =
                Duration.ofMillis(serverConfig.getStreamsSessionTimeout()).plusSeconds(10);

        this.grpcListener = null;

        ServerBuilder<?> builder = Grpc.newServerBuilderForPort(
                        listenerConfig.getPort(), listenerConfig.getCredentials())
                .permitKeepAliveTime(15, TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .addService(this)
                .executor(networkThreads);

        for (ServerInterceptor interceptor : interceptors) {
            builder.intercept(interceptor);
        }
        builder.intercept(new GlobalExceptionHandler());
        this.grpcListener = builder.build();
        this.commandSender = commandSender;
    }

    public void start() throws IOException {
        log.debug("Starting listener {}", listenerName);
        grpcListener.start();
    }

    @Override
    public void close() {
        // This forcibly closes all grpc connections rather than waiting for them to
        // complete. This cuts off all streaming connections too.
        grpcListener.shutdownNow();
        try {
            grpcListener.awaitTermination();
        } catch (InterruptedException e) {
            log.warn("InterruptedException Ignored", e);
        }
        log.info("GRPC Server for Listener {} was stopped", listenerName);
    }

    public String getInstanceName() {
        return serverConfig.getLHInstanceName();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getWfSpec(WfSpecId req, StreamObserver<WfSpec> ctx) {
        WfSpecIdModel wfSpecId = LHSerializable.fromProto(req, WfSpecIdModel.class, requestContext());
        WfSpecModel wfSpec = requestContext()
                .metadataManager()
                .getOrThrow(wfSpecId, () -> new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec"));
        ctx.onNext(wfSpec.toProto().build());
        ctx.onCompleted();
    }

    @Override
    public void getScheduledWfRun(ScheduledWfRunId req, StreamObserver<ScheduledWfRun> ctx) {
        ScheduledWfRunIdModel scheduledWfId =
                LHSerializable.fromProto(req, ScheduledWfRunIdModel.class, requestContext());
        ScheduledWfRunModel scheduledWfRun = requestContext()
                .getableManager()
                .getOrThrow(
                        scheduledWfId, () -> new LHApiException(Status.NOT_FOUND, "Couldn't find specified object"));
        ctx.onNext(scheduledWfRun.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_PRINCIPAL, actions = ACLAction.WRITE_METADATA)
    public void putPrincipal(PutPrincipalRequest req, StreamObserver<Principal> ctx) {
        PutPrincipalRequestModel reqModel =
                LHSerializable.fromProto(req, PutPrincipalRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Principal.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_PRINCIPAL, actions = ACLAction.WRITE_METADATA)
    public void deletePrincipal(DeletePrincipalRequest req, StreamObserver<Empty> ctx) {
        DeletePrincipalRequestModel reqModel =
                LHSerializable.fromProto(req, DeletePrincipalRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getLatestWfSpec(GetLatestWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        Integer majorVersion = req.hasMajorVersion() ? req.getMajorVersion() : null;
        WfSpecModel wfSpec = requestContext().service().getWfSpec(req.getName(), majorVersion, null);
        if (wfSpec == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec");
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
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find UserTaskDef %s".formatted(req.getName()));
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
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find UserTaskDef %s version %d".formatted(req.getName(), req.getVersion()));
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
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find TaskDef %s".formatted(req.getName()));
        } else {
            ctx.onNext(td.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_STRUCT, actions = ACLAction.READ)
    public void getStructDef(StructDefId req, StreamObserver<StructDef> ctx) {
        if (serverConfig.areStructDefsEnabled() == false) {
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }
        StructDefModel sd = getServiceFromContext().getStructDef(req.getName(), req.getVersion());

        if (sd == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find StructDef %s version %d".formatted(req.getName(), req.getVersion()));
        } else {
            ctx.onNext(sd.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK_WORKER_GROUP, actions = ACLAction.READ)
    public void getTaskWorkerGroup(TaskDefId taskDefIdPb, StreamObserver<TaskWorkerGroup> ctx) {
        TaskDefIdModel taskDefId = TaskDefIdModel.fromProto(taskDefIdPb, TaskDefIdModel.class, requestContext());
        TaskWorkerGroupIdModel twgid = new TaskWorkerGroupIdModel(taskDefId);
        TaskWorkerGroupModel taskWorkerGroup =
                internalComms.getObject(twgid, TaskWorkerGroupModel.class, requestContext());
        ctx.onNext(taskWorkerGroup.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void getExternalEventDef(ExternalEventDefId req, StreamObserver<ExternalEventDef> ctx) {
        ExternalEventDefModel eed = getServiceFromContext().getExternalEventDef(req.getName());
        if (eed == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find ExternalEventDef %s".formatted(req.getName()));
        } else {
            ctx.onNext(eed.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.READ)
    public void getWorkflowEventDef(WorkflowEventDefId req, StreamObserver<WorkflowEventDef> ctx) {
        WorkflowEventDefModel wed = getServiceFromContext().getWorkflowEventDef(req.getName());
        if (wed == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WorkflowEventDef %s".formatted(req.getName()));
        } else {
            ctx.onNext(wed.toProto().build());
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void putTaskDef(PutTaskDefRequest req, StreamObserver<TaskDef> ctx) {
        PutTaskDefRequestModel reqModel = LHSerializable.fromProto(req, PutTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, TaskDef.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_STRUCT, actions = ACLAction.WRITE_METADATA)
    public void putStructDef(PutStructDefRequest req, StreamObserver<StructDef> ctx) {
        if (serverConfig.areStructDefsEnabled() == false) {
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }

        PutStructDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutStructDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, StructDef.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_STRUCT, actions = ACLAction.READ)
    public void validateStructDefEvolution(
            ValidateStructDefEvolutionRequest req, StreamObserver<ValidateStructDefEvolutionResponse> ctx) {
        if (serverConfig.areStructDefsEnabled() == false) {
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }

        InlineStructDefModel newInlineStructDef =
                LHSerializable.fromProto(req.getStructDef(), InlineStructDefModel.class, requestContext());
        newInlineStructDef.validate();

        StructDefIdModel sdId =
                LHSerializable.fromProto(req.getStructDefId(), StructDefIdModel.class, requestContext());
        StructDefModel existingStructDef = getServiceFromContext().getStructDef(sdId);

        if (existingStructDef == null) {
            ctx.onNext(ValidateStructDefEvolutionResponse.newBuilder()
                    .setIsValid(true)
                    .build());
            ctx.onCompleted();
        } else {
            InlineStructDefModel oldInlineStructDef = existingStructDef.getStructDef();

            Set<String> invalidFields = InlineStructDefUtil.getIncompatibleFields(
                    req.getCompatibilityType(), newInlineStructDef, oldInlineStructDef);

            System.out.println(invalidFields);

            ValidateStructDefEvolutionResponse resp = ValidateStructDefEvolutionResponse.newBuilder()
                    .setIsValid(invalidFields.isEmpty())
                    .build();

            ctx.onNext(resp);
            ctx.onCompleted();
        }
    }
    ;

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.WRITE_METADATA)
    public void putWorkflowEventDef(PutWorkflowEventDefRequest req, StreamObserver<WorkflowEventDef> ctx) {
        PutWorkflowEventDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutWorkflowEventDefRequestModel.class, requestContext());
        this.processCommand(new MetadataCommandModel(reqModel), ctx, WorkflowEventDef.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.RUN)
    public void putExternalEvent(PutExternalEventRequest req, StreamObserver<ExternalEvent> ctx) {
        PutExternalEventRequestModel reqModel =
                LHSerializable.fromProto(req, PutExternalEventRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, ExternalEvent.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.WRITE_METADATA)
    public void putExternalEventDef(PutExternalEventDefRequest req, StreamObserver<ExternalEventDef> ctx) {
        PutExternalEventDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutExternalEventDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, ExternalEventDef.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.RUN)
    public void putCorrelatedEvent(PutCorrelatedEventRequest req, StreamObserver<CorrelatedEvent> observer) {
        PutCorrelatedEventRequestModel reqModel =
                LHSerializable.fromProto(req, PutCorrelatedEventRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), observer, CorrelatedEvent.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void putUserTaskDef(PutUserTaskDefRequest req, StreamObserver<UserTaskDef> ctx) {
        PutUserTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, PutUserTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, UserTaskDef.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void assignUserTaskRun(AssignUserTaskRunRequest req, StreamObserver<Empty> ctx) {

        if (req.hasUserId() && req.getUserId().trim().isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "UserId can't be empty");
        }

        if (req.hasUserGroup() && req.getUserGroup().trim().isEmpty()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "UserGroup can't be empty");
        }

        AssignUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, AssignUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.RUN)
    public void completeUserTaskRun(CompleteUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        CompleteUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, CompleteUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.RUN)
    public void saveUserTaskRunProgress(SaveUserTaskRunProgressRequest req, StreamObserver<UserTaskRun> ctx) {
        SaveUserTaskRunProgressRequestModel reqModel =
                LHSerializable.fromProto(req, SaveUserTaskRunProgressRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, UserTaskRun.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.RUN)
    public void cancelUserTaskRun(CancelUserTaskRunRequest req, StreamObserver<Empty> ctx) {
        CancelUserTaskRunRequestModel reqModel =
                LHSerializable.fromProto(req, CancelUserTaskRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void putWfSpec(PutWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        PutWfSpecRequestModel reqModel = LHSerializable.fromProto(req, PutWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, WfSpec.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void migrateWfSpec(MigrateWfSpecRequest req, StreamObserver<WfSpec> ctx) {
        MigrateWfSpecRequestModel reqModel =
                LHSerializable.fromProto(req, MigrateWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, WfSpec.class);
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
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void searchScheduledWfRun(SearchScheduledWfRunRequest req, StreamObserver<ScheduledWfRunIdList> ctx) {
        SearchScheduledWfRunRequestModel requestModel =
                LHSerializable.fromProto(req, SearchScheduledWfRunRequestModel.class, requestContext());
        handleScan(requestModel, ctx, SearchScheduledWfRunReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void runWf(RunWfRequest req, StreamObserver<WfRun> ctx) {
        if (Strings.isNullOrEmpty(req.getWfSpecName())) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Missing required argument 'wf_spec_name'");
        }

        if (req.hasId()) {
            if (req.getId().equals("") || !LHUtil.isValidLHName(req.getId())) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "Optional argument 'id' must be a valid hostname");
            }
        }

        RunWfRequestModel reqModel = LHSerializable.fromProto(req, RunWfRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, WfRun.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void scheduleWf(ScheduleWfRequest req, StreamObserver<ScheduledWfRun> ctx) {
        ScheduleWfRequestModel reqModel = LHSerializable.fromProto(req, ScheduleWfRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, ScheduledWfRun.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.READ)
    public StreamObserver<PollTaskRequest> pollTask(StreamObserver<PollTaskResponse> ctx) {
        AuthorizationContext authorization = requestContext().authorization();
        return new PollTaskRequestObserver(
                ctx,
                taskQueueManager,
                authorization.tenantId(),
                authorization.principalId(),
                coreStoreProvider,
                metadataCache,
                serverConfig,
                requestContext());
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void registerTaskWorker(
            RegisterTaskWorkerRequest req, StreamObserver<RegisterTaskWorkerResponse> responseObserver) {
        log.trace("Receiving RegisterTaskWorkerRequest (heartbeat) from: " + req.getTaskWorkerId());

        TaskWorkerHeartBeatRequest heartBeatPb = TaskWorkerHeartBeatRequest.newBuilder()
                .setClientId(req.getTaskWorkerId())
                .setListenerName(this.listenerName)
                .setTaskDefId(req.getTaskDefId())
                .build();

        TaskWorkerHeartBeatRequestModel heartBeat =
                LHSerializable.fromProto(heartBeatPb, TaskWorkerHeartBeatRequestModel.class, requestContext());

        ClusterHealthRequestObserver clusterHealthRequestObserver = new ClusterHealthRequestObserver(responseObserver);

        processCommand(new CommandModel(heartBeat), clusterHealthRequestObserver, RegisterTaskWorkerResponse.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void reportTask(ReportTaskRun req, StreamObserver<Empty> ctx) {
        // There is no need to wait for the ReportTaskRun to actually be processed, because
        // we would just return a google.protobuf.Empty anyways. All we need to do is wait for
        // the Command to be persisted into Kafka.
        ReportTaskRunModel reqModel = LHSerializable.fromProto(req, ReportTaskRunModel.class, requestContext());
        TenantIdModel tenantId = requestContext().authorization().tenantId();
        PrincipalIdModel principalId = requestContext().authorization().principalId();
        commandSender.doSend(reqModel, ctx, principalId, tenantId);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getWfRun(WfRunId req, StreamObserver<WfRun> ctx) {
        WfRunIdModel id = LHSerializable.fromProto(req, WfRunIdModel.class, requestContext());
        WfRunModel wfRun = internalComms.getObject(id, WfRunModel.class, requestContext());
        ctx.onNext(wfRun.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getNodeRun(NodeRunId req, StreamObserver<NodeRun> ctx) {
        NodeRunIdModel id = LHSerializable.fromProto(req, NodeRunIdModel.class, requestContext());
        NodeRunModel nodeRun = internalComms.getObject(id, NodeRunModel.class, requestContext());
        ctx.onNext(nodeRun.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getTaskRun(TaskRunId req, StreamObserver<TaskRun> ctx) {
        TaskRunIdModel id = LHSerializable.fromProto(req, TaskRunIdModel.class, requestContext());
        TaskRunModel taskRun = internalComms.getObject(id, TaskRunModel.class, requestContext());
        ctx.onNext(taskRun.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.READ)
    public void getUserTaskRun(UserTaskRunId req, StreamObserver<UserTaskRun> ctx) {
        UserTaskRunIdModel id = LHSerializable.fromProto(req, UserTaskRunIdModel.class, requestContext());
        UserTaskRunModel userTaskRun = internalComms.getObject(id, UserTaskRunModel.class, requestContext());
        ctx.onNext(userTaskRun.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void getVariable(VariableId req, StreamObserver<Variable> ctx) {
        VariableIdModel id = LHSerializable.fromProto(req, VariableIdModel.class, requestContext());
        VariableModel variable = internalComms.getObject(id, VariableModel.class, requestContext());
        ctx.onNext(variable.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void getExternalEvent(ExternalEventId req, StreamObserver<ExternalEvent> ctx) {
        ExternalEventIdModel id = LHSerializable.fromProto(req, ExternalEventIdModel.class, requestContext());
        ExternalEventModel externalEvent = internalComms.getObject(id, ExternalEventModel.class, requestContext());
        ctx.onNext(externalEvent.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.READ)
    public void getCorrelatedEvent(CorrelatedEventId req, StreamObserver<CorrelatedEvent> ctx) {
        CorrelatedEventIdModel id = LHSerializable.fromProto(req, CorrelatedEventIdModel.class, requestContext());
        CorrelatedEventModel externalEvent = internalComms.getObject(id, CorrelatedEventModel.class, requestContext());
        ctx.onNext(externalEvent.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TENANT, actions = ACLAction.WRITE_METADATA)
    public void putTenant(PutTenantRequest req, StreamObserver<Tenant> ctx) {
        PutTenantRequestModel reqModel = LHSerializable.fromProto(req, PutTenantRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Tenant.class);
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
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.READ)
    public void searchWorkflowEvent(SearchWorkflowEventRequest req, StreamObserver<WorkflowEventIdList> ctx) {
        SearchWorkflowEventRequestModel see =
                LHSerializable.fromProto(req, SearchWorkflowEventRequestModel.class, requestContext());
        handleScan(see, ctx, SearchWorkflowEventReply.class);
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

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.READ)
    public void searchWorkflowEventDef(SearchWorkflowEventDefRequest req, StreamObserver<WorkflowEventDefIdList> ctx) {
        handleScan(
                SearchWorkflowEventDefRequestModel.fromProto(req, requestContext()),
                ctx,
                SearchWorkflowEventDefReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TENANT, actions = ACLAction.READ)
    public void searchTenant(SearchTenantRequest req, StreamObserver<TenantIdList> ctx) {
        handleScan(SearchTenantRequestModel.fromProto(req, requestContext()), ctx, SearchTenantRequestReply.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_PRINCIPAL, actions = ACLAction.READ)
    public void searchPrincipal(SearchPrincipalRequest req, StreamObserver<PrincipalIdList> ctx) {
        handleScan(
                SearchPrincipalRequestModel.fromProto(req, SearchPrincipalRequestModel.class, requestContext()),
                ctx,
                SearchPrincipalRequestReply.class);
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
        } catch (InvalidStateStoreException exn) {
            // TODO: check other partitions
            ctx.onError(new StatusRuntimeException(Status.UNAVAILABLE));
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
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.READ)
    public void listWorkflowEvents(ListWorkflowEventsRequest req, StreamObserver<WorkflowEventList> ctx) {
        ListWorkflowEventsRequestModel lv =
                LHSerializable.fromProto(req, ListWorkflowEventsRequestModel.class, requestContext());
        handleScan(lv, ctx, ListWorkflowEventsReply.class);
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
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void rescueThreadRun(RescueThreadRunRequest req, StreamObserver<WfRun> ctx) {
        RescueThreadRunRequestModel reqModel =
                LHSerializable.fromProto(req, RescueThreadRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, WfRun.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void resumeWfRun(ResumeWfRunRequest req, StreamObserver<Empty> ctx) {
        ResumeWfRunRequestModel reqModel =
                LHSerializable.fromProto(req, ResumeWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void deleteWfRun(DeleteWfRunRequest req, StreamObserver<Empty> ctx) {
        DeleteWfRunRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.WRITE_METADATA)
    public void deleteWfSpec(DeleteWfSpecRequest req, StreamObserver<Empty> ctx) {
        DeleteWfSpecRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteWfSpecRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_TASK, actions = ACLAction.WRITE_METADATA)
    public void deleteTaskDef(DeleteTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.RUN)
    public void deleteScheduledWfRun(DeleteScheduledWfRunRequest req, StreamObserver<Empty> ctx) {
        DeleteScheduledWfRunRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteScheduledWfRunRequestModel.class, requestContext());
        processCommand(new CommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_USER_TASK, actions = ACLAction.WRITE_METADATA)
    public void deleteUserTaskDef(DeleteUserTaskDefRequest req, StreamObserver<Empty> ctx) {
        DeleteUserTaskDefRequestModel reqModel =
                LHSerializable.fromProto(req, DeleteUserTaskDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(reqModel), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_EXTERNAL_EVENT, actions = ACLAction.WRITE_METADATA)
    public void deleteExternalEventDef(DeleteExternalEventDefRequest req, StreamObserver<Empty> ctx) {
        DeleteExternalEventDefRequestModel deedr =
                LHSerializable.fromProto(req, DeleteExternalEventDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(deedr), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.WRITE_METADATA)
    public void deleteWorkflowEventDef(DeleteWorkflowEventDefRequest req, StreamObserver<Empty> ctx) {
        DeleteWorkflowEventDefRequestModel dwedr =
                LHSerializable.fromProto(req, DeleteWorkflowEventDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(dwedr), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_STRUCT, actions = ACLAction.WRITE_METADATA)
    public void deleteStructDef(DeleteStructDefRequest req, StreamObserver<Empty> ctx) {
        if (serverConfig.areStructDefsEnabled() == false) {
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }
        DeleteStructDefRequestModel dsdr =
                LHSerializable.fromProto(req, DeleteStructDefRequestModel.class, requestContext());
        processCommand(new MetadataCommandModel(dsdr), ctx, Empty.class);
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW, actions = ACLAction.READ)
    public void awaitWorkflowEvent(AwaitWorkflowEventRequest req, StreamObserver<WorkflowEvent> ctx) {
        WfRunIdModel wfRunId = LHSerializable.fromProto(req.getWfRunId(), WfRunIdModel.class, requestContext());
        List<WorkflowEventDefIdModel> eventDefIds = req.getEventDefIdsList().stream()
                .map(pb -> LHSerializable.fromProto(pb, WorkflowEventDefIdModel.class, requestContext()))
                .toList();
        KeyQueryMetadata meta = internalComms.lookupPartitionKey(wfRunId);
        if (internalComms.isLocalObject(meta)) {
            CompletableFuture<Object> firstMatchingEvent = CompletableFuture.anyOf(asyncWaiters.getOrRegisterFuture(
                    requestContext().authorization().tenantId(), wfRunId, eventDefIds));
            try {
                WorkflowEvent event = (WorkflowEvent) firstMatchingEvent.get(
                        LHConstants.MAX_INCOMING_REQUEST_IDLE_TIME.getSeconds(), TimeUnit.SECONDS);
                ctx.onNext(event);
                ctx.onCompleted();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                ctx.onError(new LHApiException(Status.DEADLINE_EXCEEDED));
            }
        } else {
            WorkflowEvent workflowEvent =
                    lhInternalClient.remoteWaitForEvents(meta.activeHost(), requestContext(), req);
            ctx.onNext(workflowEvent);
            ctx.onCompleted();
        }
    }

    @Override
    @Authorize(resources = ACLResource.ACL_WORKFLOW_EVENT, actions = ACLAction.READ)
    public void getWorkflowEvent(WorkflowEventId req, StreamObserver<WorkflowEvent> ctx) {
        WorkflowEventIdModel id = LHSerializable.fromProto(req, WorkflowEventIdModel.class, requestContext());
        WorkflowEventModel workflowEvent = internalComms.getObject(id, WorkflowEventModel.class, requestContext());
        if (workflowEvent == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find WorkflowEvent");
        }
        ctx.onNext(workflowEvent.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(
            resources = {ACLResource.ACL_TENANT},
            actions = ACLAction.READ)
    public void getTenant(TenantId req, StreamObserver<Tenant> ctx) {
        RequestExecutionContext reqContext = requestContext();
        TenantIdModel tenantId = TenantIdModel.fromProto(req, TenantIdModel.class, reqContext);
        TenantModel result = reqContext
                .metadataManager()
                .getOrThrow(
                        tenantId,
                        () -> new LHApiException(Status.NOT_FOUND, "Could not find tenant %s".formatted(tenantId)));
        ctx.onNext(result.toProto().build());
        ctx.onCompleted();
    }

    @Override
    @Authorize(
            resources = {ACLResource.ACL_PRINCIPAL},
            actions = ACLAction.READ)
    public void getPrincipal(PrincipalId req, StreamObserver<Principal> ctx) {
        RequestExecutionContext reqContext = requestContext();
        PrincipalIdModel principalId = PrincipalIdModel.fromProto(req, PrincipalIdModel.class, reqContext);
        PrincipalModel result = reqContext
                .metadataManager()
                .getOrThrow(
                        principalId,
                        () -> new LHApiException(
                                Status.NOT_FOUND, "Could not find Principal %s".formatted(principalId)));
        ctx.onNext(result.toProto().build());
        ctx.onCompleted();
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
    public void getServerVersion(Empty request, StreamObserver<LittleHorseVersion> ctx) {
        ctx.onNext(Version.getCurrentServerVersion());
        ctx.onCompleted();
    }

    /*
     * Sends a command to Kafka and simultaneously does a waitForProcessing() internal
     * grpc call that asynchronously waits for the command to be processed.
     *
     * Explicit request context. Useful for callers who do not have access to the GRPC
     * context, for example the `returnTaskToClient()` method. That method is called
     * from within the CommandProcessor#process() method.
     */
    private <AC extends Message, RC extends Message> void processCommand(
            AbstractCommand<AC> command, StreamObserver<RC> responseObserver, Class<RC> responseCls) {
        command.setCommandId(LHUtil.generateGuid());
        RequestExecutionContext requestContext = requestContext();
        Future<Message> futureResponse = commandSender.doSend(
                command,
                responseCls,
                requestContext.authorization().principalId(),
                requestContext.authorization().tenantId(),
                requestContext);
        try {
            Message response =
                    futureResponse.get(LHConstants.MAX_INCOMING_REQUEST_IDLE_TIME.getSeconds(), TimeUnit.SECONDS);
            responseObserver.onNext((RC) response);
            responseObserver.onCompleted();
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.error("Failed to process command %s".formatted(command), cause);
            responseObserver.onError(cause);
        } catch (TimeoutException e) {
            responseObserver.onError(new StatusRuntimeException(Status.DEADLINE_EXCEEDED.withDescription(
                    "Could not process command in time id: %s".formatted(command.getCommandId()))));
        } catch (Throwable e) {
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription("Internal error")));
            log.error("Failed processing command", e);
        } finally {
            command.getCommandId().ifPresent(asyncWaiters::removeCommand);
        }
    }

    private WfService getServiceFromContext() {
        return requestContext().service();
    }
}

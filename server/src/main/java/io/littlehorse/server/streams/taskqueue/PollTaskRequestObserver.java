package io.littlehorse.server.streams.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.streams.topology.core.CoreStoreProvider;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import io.littlehorse.server.streams.util.MetadataCache;

public class PollTaskRequestObserver implements StreamObserver<PollTaskRequest> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PollTaskRequestObserver.class);
    private StreamObserver<PollTaskResponse> responseObserver;
    private TaskQueueManager taskQueueManager;
    private final PrincipalIdModel principalId;

    private TaskDefIdModel taskDefId;
    private String clientId;
    private String taskWorkerVersion;
    private final TenantIdModel tenantId;
    private final RequestExecutionContext requestContext;

    private CoreStoreProvider coreStoreProvider;
    private final MetadataCache metadataCache;
    private LHServerConfig config;

    public PollTaskRequestObserver(
            StreamObserver<PollTaskResponse> responseObserver,
            TaskQueueManager manager,
            TenantIdModel tenantId,
            PrincipalIdModel principalId,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            LHServerConfig config,
            RequestExecutionContext requestContext) {
        this.responseObserver = responseObserver;
        this.taskQueueManager = manager;
        this.principalId = principalId;
        this.tenantId = tenantId;
        this.coreStoreProvider = coreStoreProvider;
        this.metadataCache = metadataCache;
        this.config = config;
        this.clientId = null;
        this.requestContext = requestContext;
    }

    public String getTaskWorkerVersion() {
        return taskWorkerVersion;
    }

    public String getTaskDefId() {
        return taskDefId.getName();
    }

    public String getClientId() {
        return clientId;
    }

    public StreamObserver<PollTaskResponse> getResponseObserver() {
        return responseObserver;
    }

    @Override
    public void onError(Throwable t) {
        taskQueueManager.onRequestDisconnected(this, tenantId);
        try {
            // Sometimes this observer receives an error not related to the client connection,
            // so we need to make sure we send an error back to the client.
            // See Javadoc for StreamObserver.onError for more details.
            responseObserver.onError(t);
        } catch (Exception e) {
        }
        // Smallowed to fail silently since the client connection is already broken, so there's no point in trying
        // to send an error back to the client.
        log.debug(
                "Instance {}: Client {} disconnected from task queue {}",
                taskQueueManager.getBackend().getInstanceName(),
                clientId,
                taskDefId);
    }

    @Override
    public void onNext(PollTaskRequest req) {
        if (clientId == null) {
            clientId = req.getClientId();
        }
        RequestExecutionContext requestContext = getFreshExecutionContext();
        if (taskDefId == null) {
            taskDefId = LHSerializable.fromProto(req.getTaskDefId(), TaskDefIdModel.class, requestContext);
        } else if (!taskDefId.getName().equals(req.getTaskDefId().getName())) {
            log.error("TaskDefName not null: {} but doesnt match {}", taskDefId, req.getTaskDefId());
        }

        taskDefId = LHSerializable.fromProto(req.getTaskDefId(), TaskDefIdModel.class, requestContext);
        clientId = req.getClientId();
        taskWorkerVersion = req.getTaskWorkerVersion();

        taskQueueManager.onPollRequest(this, tenantId, requestContext);
    }

    @Override
    public void onCompleted() {
        if (taskDefId != null) {
            taskQueueManager.onRequestDisconnected(this, tenantId);
        }
    }

    RequestExecutionContext getFreshExecutionContext() {
        return new RequestExecutionContext(principalId, tenantId, coreStoreProvider, metadataCache, config, false);
    }

    public void sendResponse(ScheduledTaskModel toExecute) {
        if (toExecute == null) {
            // Tasks may be already claimed by other workers during rebalancing.
            // TODO: Add metrics to track the frequency of empty scheduled tasks to better monitor rebalancing impact
            log.debug("Processing pollTaskRequest for task that was already claimed");
            PollTaskResponse emptyResponse = PollTaskResponse.newBuilder().build();
            responseObserver.onNext(emptyResponse);
        } else {
            PollTaskResponse response =
                    PollTaskResponse.newBuilder().setResult(toExecute.toProto()).build();
            responseObserver.onNext(response);
        }
    }

    public PrincipalIdModel getPrincipalId() {
        return this.principalId;
    }

    public TenantIdModel getTenantId() {
        return this.tenantId;
    }

    public RequestExecutionContext getRequestContext() {
        return this.requestContext;
    }
}

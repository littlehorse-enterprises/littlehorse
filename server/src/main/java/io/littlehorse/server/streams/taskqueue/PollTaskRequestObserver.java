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
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollTaskRequestObserver implements StreamObserver<PollTaskRequest> {

    private StreamObserver<PollTaskResponse> responseObserver;
    private TaskQueueManager taskQueueManager;

    @Getter
    private final PrincipalIdModel principalId;

    private TaskDefIdModel taskDefId;
    private String clientId;
    private String taskWorkerVersion;

    @Getter
    private final TenantIdModel tenantId;

    @Getter
    private final RequestExecutionContext requestContext;

    private CoreStoreProvider coreStoreProvider;
    private final MetadataCache metadataCache;
    private LHServerConfig config;
    private final String instanceName;
    private final ExecutorService networkThreads;

    public PollTaskRequestObserver(
            String instanceName,
            StreamObserver<PollTaskResponse> responseObserver,
            TaskQueueManager manager,
            TenantIdModel tenantId,
            PrincipalIdModel principalId,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            LHServerConfig config,
            ExecutorService networkThreads,
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
        this.instanceName = instanceName;
        this.networkThreads = networkThreads;
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
        log.debug("Instance {}: Client {} disconnected from task queue {}", instanceName, clientId, taskDefId);
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
        taskQueueManager.onRequestDisconnected(this, tenantId);
    }

    public RequestExecutionContext getFreshExecutionContext() {
        return new RequestExecutionContext(principalId, tenantId, coreStoreProvider, metadataCache, config, false);
    }

    public void sendResponse(ScheduledTaskModel toExecute) {
        networkThreads.execute(() -> {
            PollTaskResponse response =
                    PollTaskResponse.newBuilder().setResult(toExecute.toProto()).build();
            responseObserver.onNext(response);
        });
    }
}

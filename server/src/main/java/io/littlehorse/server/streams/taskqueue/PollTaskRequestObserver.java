package io.littlehorse.server.streams.taskqueue;

import io.grpc.stub.ServerCallStreamObserver;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollTaskRequestObserver implements StreamObserver<PollTaskRequest> {

    private ServerCallStreamObserver<PollTaskResponse> responseObserver;
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
    // Guard against spurious onReady() calls caused by a race between onNext() and onReady(). If the transport
    // toggles isReady() from false to true while onNext() is executing, but before onNext() checks isReady(),
    // request(1) would be called twice - once by onNext() and once by the onReady() scheduled during onNext()'s
    // execution.
    private boolean wasReady = false;

    public PollTaskRequestObserver(
            ServerCallStreamObserver<PollTaskResponse> responseObserver,
            TaskQueueManager manager,
            TenantIdModel tenantId,
            PrincipalIdModel principalId,
            CoreStoreProvider coreStoreProvider,
            MetadataCache metadataCache,
            LHServerConfig config,
            RequestExecutionContext requestContext) {
        this.responseObserver = responseObserver;
        this.responseObserver.disableAutoRequest();
        this.responseObserver.setOnReadyHandler(new OnReadyHandler());
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
        // Check the provided ServerCallStreamObserver to see if it is still ready to accept more messages.
        if (responseObserver.isReady()) {
            // Signal the worker to send another request.
            responseObserver.request(1);
        } else {
            // back-pressure has begun.
            wasReady = false;
        }
    }

    @Override
    public void onCompleted() {
        taskQueueManager.onRequestDisconnected(this, tenantId);
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

    private final class OnReadyHandler implements Runnable {
        @Override
        public void run() {
            if (responseObserver.isReady() && !wasReady) {
                wasReady = true;
                log.trace("Response observer ready");
                // Signal the request sender to send one message. This happens when isReady() turns true, signaling that
                // the receive buffer has enough free space to receive more messages. Calling request() serves to prime
                // the message pump.
                responseObserver.request(1);
            }
        }
    }
}

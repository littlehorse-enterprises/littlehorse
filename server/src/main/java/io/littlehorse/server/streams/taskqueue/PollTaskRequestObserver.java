package io.littlehorse.server.streams.taskqueue;

import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollTaskRequestObserver implements StreamObserver<PollTaskRequest> {

    private StreamObserver<PollTaskResponse> responseObserver;
    private TaskQueueManager taskQueueManager;
    private String clientId;
    private TaskDefIdModel taskDefId;
    private String taskWorkerVersion;
    private final TenantIdModel tenantId;

    @Getter
    private final RequestExecutionContext requestContext;

    public PollTaskRequestObserver(
            StreamObserver<PollTaskResponse> responseObserver,
            TaskQueueManager manager,
            RequestExecutionContext requestContext) {
        this.responseObserver = responseObserver;
        this.taskQueueManager = manager;
        this.clientId = null;
        this.requestContext = requestContext;
        this.tenantId = requestContext.authorization().tenantId();
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
        log.info(
                "Instance {}: Client {} disconnected from task queue {}",
                taskQueueManager.backend.getInstanceId(),
                clientId,
                taskDefId);
        taskQueueManager.onRequestDisconnected(this, tenantId);
    }

    @Override
    public void onNext(PollTaskRequest req) {
        if (clientId == null) {
            clientId = req.getClientId();
        }

        if (taskDefId == null) {
            taskDefId = LHSerializable.fromProto(req.getTaskDefId(), TaskDefIdModel.class, requestContext);
        } else if (!taskDefId.getName().equals(req.getTaskDefId().getName())) {
            log.error("TaskDefName not null: {} but doesnt match {}", taskDefId, req.getTaskDefId());
        }

        taskDefId = LHSerializable.fromProto(req.getTaskDefId(), TaskDefIdModel.class, requestContext);
        clientId = req.getClientId();
        taskWorkerVersion = req.getTaskWorkerVersion();

        taskQueueManager.onPollRequest(this, tenantId);
    }

    @Override
    public void onCompleted() {
        taskQueueManager.onRequestDisconnected(this, tenantId);
    }
}

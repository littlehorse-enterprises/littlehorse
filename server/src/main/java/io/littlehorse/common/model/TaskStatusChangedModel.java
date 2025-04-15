package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TaskStatusChanged;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class TaskStatusChangedModel extends LHSerializable<TaskStatusChanged> {
    private TaskStatus previousStatus;
    private TaskStatus newStatus;

    public TaskStatusChangedModel() {
        // Used for des/serialization
    }

    public TaskStatusChangedModel(TaskStatus previousStatus, TaskStatus newStatus) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        TaskStatusChanged p = (TaskStatusChanged) proto;
        if (p.hasPreviousStatus()) {
            this.previousStatus = p.getPreviousStatus();
        }
        this.newStatus = p.getNewStatus();
    }

    @Override
    public TaskStatusChanged.Builder toProto() {
        TaskStatusChanged.Builder out = TaskStatusChanged.newBuilder();
        if (previousStatus != null) {
            out.setPreviousStatus(previousStatus);
        }
        out.setNewStatus(newStatus);
        return out;
    }

    @Override
    public Class<TaskStatusChanged> getProtoBaseClass() {
        return TaskStatusChanged.class;
    }

    public boolean isCompleted() {
        return this.newStatus.equals(TaskStatus.TASK_SUCCESS);
    }

    public boolean isStarted() {
        return this.previousStatus == null;
    }

    public boolean isErrored() {
        return this.newStatus.equals(TaskStatus.TASK_FAILED);
    }

    public boolean isScheduled() {
        return this.newStatus.equals(TaskStatus.TASK_SCHEDULED);
    }
}

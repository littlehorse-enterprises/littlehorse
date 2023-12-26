package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TaskStatusChanged;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class TaskStatusChangedModel extends LHSerializable<TaskStatusChanged> {
    private TaskStatus previousStatus;
    private TaskStatus newStatus;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
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
}

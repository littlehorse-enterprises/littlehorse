package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.StatusChanged;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class StatusChangedModel extends LHSerializable<StatusChanged> {

    private Date time;
    private LHStatusChangedModel lhStatusChanged;
    private TaskStatusChangedModel taskStatusChanged;

    public StatusChangedModel() {
        // used by LHSerializable
    }

    public StatusChangedModel(Date time, LHStatusChangedModel lhStatusChanged) {
        this.time = time;
        this.lhStatusChanged = lhStatusChanged;
    }

    public StatusChangedModel(Date time, TaskStatusChangedModel taskStatusChanged) {
        this.time = time;
        this.taskStatusChanged = taskStatusChanged;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        StatusChanged p = (StatusChanged) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        if (p.getStatusCase().equals(StatusChanged.StatusCase.LH_STATUS)) {
            lhStatusChanged = LHSerializable.fromProto(p.getLhStatus(), LHStatusChangedModel.class, context);
        } else if (p.getStatusCase().equals(StatusChanged.StatusCase.TASK_STATUS)) {
            taskStatusChanged = LHSerializable.fromProto(p.getTaskStatus(), TaskStatusChangedModel.class, context);
        } else {
            throw new LHApiException(Status.INTERNAL, "Unrecognized status case: " + p.getStatusCase());
        }
    }

    @Override
    public StatusChanged.Builder toProto() {
        StatusChanged.Builder out = StatusChanged.newBuilder();
        out.setTime(LHUtil.fromDate(time));
        if (lhStatusChanged != null) {
            out.setLhStatus(lhStatusChanged.toProto());
        } else if (taskStatusChanged != null) {
            out.setTaskStatus(taskStatusChanged.toProto());
        } else {
            throw new LHApiException(Status.INTERNAL, "Illegal state for StatusChanged");
        }
        return out;
    }

    @Override
    public Class<StatusChanged> getProtoBaseClass() {
        return StatusChanged.class;
    }
}

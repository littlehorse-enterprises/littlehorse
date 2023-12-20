package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.LHStatusChanged;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class LHStatusChangedModel extends LHSerializable<LHStatusChanged> {

    private LHStatus previousStatus;
    private LHStatus newStatus;

    public LHStatusChangedModel() {}

    public LHStatusChangedModel(LHStatus previousStatus, LHStatus newStatus) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        LHStatusChanged p = (LHStatusChanged) proto;
        if (p.hasPreviousStatus()) {
            this.previousStatus = p.getPreviousStatus();
        }
        newStatus = p.getNewStatus();
    }

    @Override
    public LHStatusChanged.Builder toProto() {
        LHStatusChanged.Builder out = LHStatusChanged.newBuilder();
        if (previousStatus != null) {
            out.setPreviousStatus(previousStatus);
        }
        out.setNewStatus(newStatus);
        return out;
    }

    @Override
    public Class<LHStatusChanged> getProtoBaseClass() {
        return LHStatusChanged.class;
    }
}

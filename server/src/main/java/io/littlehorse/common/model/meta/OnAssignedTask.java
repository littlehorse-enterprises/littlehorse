package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.OnAssignedTaskPb;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OnAssignedTask extends LHSerializable<OnAssignedTaskPb> {

    private VariableAssignment delaySeconds;

    @Override
    public Class<OnAssignedTaskPb> getProtoBaseClass() {
        return OnAssignedTaskPb.class;
    }

    @Override
    public void initFrom(Message proto) {
        OnAssignedTaskPb p = (OnAssignedTaskPb) proto;
        delaySeconds =
            LHSerializable.fromProto(p.getDelaySeconds(), VariableAssignment.class);
    }

    @Override
    public OnAssignedTaskPb.Builder toProto() {
        OnAssignedTaskPb.Builder out = OnAssignedTaskPb.newBuilder();
        out.setDelaySeconds(delaySeconds.toProto());
        return out;
    }
}

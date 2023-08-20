package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.PendingFailureHandlerHaltReason;

public class PendingFailureHandlerHaltReasonModel extends LHSerializable<PendingFailureHandlerHaltReason>
        implements SubHaltReason {

    public int nodeRunPosition;

    public boolean isResolved(WfRunModel wfRunModel) {
        // Should always return false because this HaltReason is manually
        // removed upon creation of the Failure Handler Thread
        return false;
    }

    public Class<PendingFailureHandlerHaltReason> getProtoBaseClass() {
        return PendingFailureHandlerHaltReason.class;
    }

    public PendingFailureHandlerHaltReason.Builder toProto() {
        PendingFailureHandlerHaltReason.Builder out = PendingFailureHandlerHaltReason.newBuilder();
        out.setNodeRunPosition(nodeRunPosition);
        return out;
    }

    public void initFrom(Message proto) {
        PendingFailureHandlerHaltReason p = (PendingFailureHandlerHaltReason) proto;
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static PendingFailureHandlerHaltReasonModel fromProto(PendingFailureHandlerHaltReason proto) {
        PendingFailureHandlerHaltReasonModel out = new PendingFailureHandlerHaltReasonModel();
        out.initFrom(proto);
        return out;
    }
}

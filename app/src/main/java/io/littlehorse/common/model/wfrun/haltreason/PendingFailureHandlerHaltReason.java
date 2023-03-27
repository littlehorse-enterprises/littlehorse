package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.PendingFailureHandlerHaltReasonPb;

public class PendingFailureHandlerHaltReason
    extends LHSerializable<PendingFailureHandlerHaltReasonPb>
    implements SubHaltReason {

    public int nodeRunPosition;

    public boolean isResolved(WfRun wfRun) {
        // Should always return false because this HaltReason is manually
        // removed upon creation of the Failure Handler Thread
        return false;
    }

    public Class<PendingFailureHandlerHaltReasonPb> getProtoBaseClass() {
        return PendingFailureHandlerHaltReasonPb.class;
    }

    public PendingFailureHandlerHaltReasonPb.Builder toProto() {
        PendingFailureHandlerHaltReasonPb.Builder out = PendingFailureHandlerHaltReasonPb.newBuilder();
        out.setNodeRunPosition(nodeRunPosition);
        return out;
    }

    public void initFrom(Message proto) {
        PendingFailureHandlerHaltReasonPb p = (PendingFailureHandlerHaltReasonPb) proto;
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static PendingFailureHandlerHaltReason fromProto(
        PendingFailureHandlerHaltReasonPb proto
    ) {
        PendingFailureHandlerHaltReason out = new PendingFailureHandlerHaltReason();
        out.initFrom(proto);
        return out;
    }
}

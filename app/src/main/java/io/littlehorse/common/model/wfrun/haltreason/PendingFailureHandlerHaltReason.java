package io.littlehorse.common.model.wfrun.haltreason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.PendingFailureHandlerHaltReasonPb;
import io.littlehorse.jlib.common.proto.PendingFailureHandlerHaltReasonPbOrBuilder;

public class PendingFailureHandlerHaltReason
    extends LHSerializable<PendingFailureHandlerHaltReasonPb>
    implements SubHaltReason {

    public int nodeRunPosition;

    @JsonIgnore
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

    public void initFrom(MessageOrBuilder proto) {
        PendingFailureHandlerHaltReasonPbOrBuilder p = (PendingFailureHandlerHaltReasonPbOrBuilder) proto;
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static PendingFailureHandlerHaltReason fromProto(
        PendingFailureHandlerHaltReasonPbOrBuilder proto
    ) {
        PendingFailureHandlerHaltReason out = new PendingFailureHandlerHaltReason();
        out.initFrom(proto);
        return out;
    }
}

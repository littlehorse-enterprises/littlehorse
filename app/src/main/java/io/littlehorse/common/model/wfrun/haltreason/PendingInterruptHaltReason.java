package io.littlehorse.common.model.wfrun.haltreason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.PendingInterruptHaltReasonPb;
import io.littlehorse.jlib.common.proto.PendingInterruptHaltReasonPbOrBuilder;

public class PendingInterruptHaltReason
    extends LHSerializable<PendingInterruptHaltReasonPb>
    implements SubHaltReason {

    public String externalEventId;

    @JsonIgnore
    public boolean isResolved(WfRun wfRun) {
        // Should always return false because this HaltReason is manually
        // removed upon creation of the Interrupt Thread
        return false;
    }

    public Class<PendingInterruptHaltReasonPb> getProtoBaseClass() {
        return PendingInterruptHaltReasonPb.class;
    }

    public PendingInterruptHaltReasonPb.Builder toProto() {
        PendingInterruptHaltReasonPb.Builder out = PendingInterruptHaltReasonPb.newBuilder();
        out.setExternalEventId(externalEventId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PendingInterruptHaltReasonPbOrBuilder p = (PendingInterruptHaltReasonPbOrBuilder) proto;
        externalEventId = p.getExternalEventId();
    }

    public static PendingInterruptHaltReason fromProto(
        PendingInterruptHaltReasonPbOrBuilder proto
    ) {
        PendingInterruptHaltReason out = new PendingInterruptHaltReason();
        out.initFrom(proto);
        return out;
    }
}

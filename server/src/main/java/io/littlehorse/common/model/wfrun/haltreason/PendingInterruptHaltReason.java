package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.sdk.common.proto.PendingInterruptHaltReasonPb;

public class PendingInterruptHaltReason
    extends LHSerializable<PendingInterruptHaltReasonPb>
    implements SubHaltReason {

    public ExternalEventId externalEventId;

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
        out.setExternalEventId(externalEventId.toProto());
        return out;
    }

    public void initFrom(Message proto) {
        PendingInterruptHaltReasonPb p = (PendingInterruptHaltReasonPb) proto;
        externalEventId =
            LHSerializable.fromProto(p.getExternalEventId(), ExternalEventId.class);
    }

    public static PendingInterruptHaltReason fromProto(
        PendingInterruptHaltReasonPb proto
    ) {
        PendingInterruptHaltReason out = new PendingInterruptHaltReason();
        out.initFrom(proto);
        return out;
    }
}

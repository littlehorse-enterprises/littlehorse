package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.PendingInterruptHaltReason;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class PendingInterruptHaltReasonModel extends LHSerializable<PendingInterruptHaltReason>
        implements SubHaltReason {

    public ExternalEventIdModel externalEventId;

    @Override
    public boolean isResolved(ThreadRunModel haltedThread) {
        // Should always return false because this HaltReason is manually
        // removed upon creation of the Interrupt Thread
        return false;
    }

    @Override
    public Class<PendingInterruptHaltReason> getProtoBaseClass() {
        return PendingInterruptHaltReason.class;
    }

    @Override
    public PendingInterruptHaltReason.Builder toProto() {
        PendingInterruptHaltReason.Builder out = PendingInterruptHaltReason.newBuilder();
        out.setExternalEventId(externalEventId.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PendingInterruptHaltReason p = (PendingInterruptHaltReason) proto;
        externalEventId = LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class, context);
    }

    public static PendingInterruptHaltReasonModel fromProto(
            PendingInterruptHaltReason proto, ExecutionContext context) {
        PendingInterruptHaltReasonModel out = new PendingInterruptHaltReasonModel();
        out.initFrom(proto, context);
        return out;
    }
}

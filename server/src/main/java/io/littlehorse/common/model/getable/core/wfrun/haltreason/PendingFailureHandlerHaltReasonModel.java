package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.PendingFailureHandlerHaltReason;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class PendingFailureHandlerHaltReasonModel extends LHSerializable<PendingFailureHandlerHaltReason>
        implements SubHaltReason {

    public int nodeRunPosition;

    @Override
    public boolean isResolved(ThreadRunModel haltedThread) {
        // Should always return false because this HaltReason is manually
        // removed upon creation of the Failure Handler Thread
        return false;
    }

    @Override
    public Class<PendingFailureHandlerHaltReason> getProtoBaseClass() {
        return PendingFailureHandlerHaltReason.class;
    }

    @Override
    public PendingFailureHandlerHaltReason.Builder toProto() {
        PendingFailureHandlerHaltReason.Builder out = PendingFailureHandlerHaltReason.newBuilder();
        out.setNodeRunPosition(nodeRunPosition);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PendingFailureHandlerHaltReason p = (PendingFailureHandlerHaltReason) proto;
        nodeRunPosition = p.getNodeRunPosition();
    }

    public static PendingFailureHandlerHaltReasonModel fromProto(
            PendingFailureHandlerHaltReason proto, ExecutionContext context) {
        PendingFailureHandlerHaltReasonModel out = new PendingFailureHandlerHaltReasonModel();
        out.initFrom(proto, context);
        return out;
    }
}

package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.haltreason.HandlingFailureHaltReasonModel;
import io.littlehorse.common.model.wfrun.haltreason.InterruptedModel;
import io.littlehorse.common.model.wfrun.haltreason.ManualHaltModel;
import io.littlehorse.common.model.wfrun.haltreason.ParentHaltedModel;
import io.littlehorse.common.model.wfrun.haltreason.PendingFailureHandlerHaltReasonModel;
import io.littlehorse.common.model.wfrun.haltreason.PendingInterruptHaltReasonModel;
import io.littlehorse.common.model.wfrun.haltreason.SubHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadHaltReasonModel extends LHSerializable<ThreadHaltReason> {

    public ParentHaltedModel parentHalted;
    public InterruptedModel interrupted;
    public PendingInterruptHaltReasonModel pendingInterrupt;
    public HandlingFailureHaltReasonModel handlingFailure;
    public PendingFailureHandlerHaltReasonModel pendingFailure;
    public ManualHaltModel manualHalt;

    public ReasonCase type;

    public ThreadRunModel threadRunModel;

    public WfRunModel wfRunModel;

    public Class<ThreadHaltReason> getProtoBaseClass() {
        return ThreadHaltReason.class;
    }

    private SubHaltReason getSubHaltReason() {
        switch (type) {
            case PARENT_HALTED:
                return parentHalted;
            case INTERRUPTED:
                return interrupted;
            case PENDING_INTERRUPT:
                return pendingInterrupt;
            case PENDING_FAILURE:
                return pendingFailure;
            case HANDLING_FAILURE:
                return handlingFailure;
            case MANUAL_HALT:
                return manualHalt;
            case REASON_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        throw new RuntimeException("Not possible to get here");
    }

    public boolean isResolved() {
        return getSubHaltReason().isResolved(threadRunModel.wfRunModel);
    }

    public ThreadHaltReason.Builder toProto() {
        ThreadHaltReason.Builder out = ThreadHaltReason.newBuilder();

        switch (type) {
            case PARENT_HALTED:
                out.setParentHalted(parentHalted.toProto());
                break;
            case INTERRUPTED:
                out.setInterrupted(interrupted.toProto());
                break;
            case PENDING_INTERRUPT:
                out.setPendingInterrupt(pendingInterrupt.toProto());
                break;
            case PENDING_FAILURE:
                out.setPendingFailure(pendingFailure.toProto());
                break;
            case HANDLING_FAILURE:
                out.setHandlingFailure(handlingFailure.toProto());
                break;
            case MANUAL_HALT:
                out.setManualHalt(manualHalt.toProto());
                break;
            case REASON_NOT_SET:
                throw new RuntimeException("not possible");
        }

        return out;
    }

    public void initFrom(Message proto) {
        ThreadHaltReason p = (ThreadHaltReason) proto;
        type = p.getReasonCase();

        switch (type) {
            case PARENT_HALTED:
                parentHalted = ParentHaltedModel.fromProto(p.getParentHalted());
                break;
            case INTERRUPTED:
                interrupted = InterruptedModel.fromProto(p.getInterrupted());
                break;
            case PENDING_INTERRUPT:
                pendingInterrupt =
                        PendingInterruptHaltReasonModel.fromProto(p.getPendingInterrupt());
                break;
            case HANDLING_FAILURE:
                handlingFailure = HandlingFailureHaltReasonModel.fromProto(p.getHandlingFailure());
                break;
            case PENDING_FAILURE:
                pendingFailure =
                        PendingFailureHandlerHaltReasonModel.fromProto(p.getPendingFailure());
                break;
            case MANUAL_HALT:
                manualHalt = ManualHaltModel.fromProto(p.getManualHalt());
                break;
            case REASON_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }

    public static ThreadHaltReasonModel fromProto(ThreadHaltReason p) {
        ThreadHaltReasonModel out = new ThreadHaltReasonModel();
        out.initFrom(p);
        return out;
    }
}

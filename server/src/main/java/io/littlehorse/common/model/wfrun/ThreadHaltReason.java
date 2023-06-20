package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.haltreason.HandlingFailureHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.Interrupted;
import io.littlehorse.common.model.wfrun.haltreason.ManualHalt;
import io.littlehorse.common.model.wfrun.haltreason.ParentHalted;
import io.littlehorse.common.model.wfrun.haltreason.PendingFailureHandlerHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.PendingInterruptHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.SubHaltReason;
import io.littlehorse.jlib.common.proto.ThreadHaltReasonPb;
import io.littlehorse.jlib.common.proto.ThreadHaltReasonPb.ReasonCase;

public class ThreadHaltReason extends LHSerializable<ThreadHaltReasonPb> {

    public ParentHalted parentHalted;
    public Interrupted interrupted;
    public PendingInterruptHaltReason pendingInterrupt;
    public HandlingFailureHaltReason handlingFailure;
    public PendingFailureHandlerHaltReason pendingFailure;
    public ManualHalt manualHalt;

    public ReasonCase type;

    public ThreadRun threadRun;

    public WfRun wfRun;

    public Class<ThreadHaltReasonPb> getProtoBaseClass() {
        return ThreadHaltReasonPb.class;
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
        return getSubHaltReason().isResolved(threadRun.wfRun);
    }

    public ThreadHaltReasonPb.Builder toProto() {
        ThreadHaltReasonPb.Builder out = ThreadHaltReasonPb.newBuilder();

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
        ThreadHaltReasonPb p = (ThreadHaltReasonPb) proto;
        type = p.getReasonCase();

        switch (type) {
            case PARENT_HALTED:
                parentHalted = ParentHalted.fromProto(p.getParentHalted());
                break;
            case INTERRUPTED:
                interrupted = Interrupted.fromProto(p.getInterrupted());
                break;
            case PENDING_INTERRUPT:
                pendingInterrupt =
                    PendingInterruptHaltReason.fromProto(p.getPendingInterrupt());
                break;
            case HANDLING_FAILURE:
                handlingFailure =
                    HandlingFailureHaltReason.fromProto(p.getHandlingFailure());
                break;
            case PENDING_FAILURE:
                pendingFailure =
                    PendingFailureHandlerHaltReason.fromProto(p.getPendingFailure());
                break;
            case MANUAL_HALT:
                manualHalt = ManualHalt.fromProto(p.getManualHalt());
                break;
            case REASON_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }

    public static ThreadHaltReason fromProto(ThreadHaltReasonPb p) {
        ThreadHaltReason out = new ThreadHaltReason();
        out.initFrom(p);
        return out;
    }
}

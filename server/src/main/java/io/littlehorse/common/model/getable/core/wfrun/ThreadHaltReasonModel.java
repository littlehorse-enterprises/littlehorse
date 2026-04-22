package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.HaltedByParentNodeHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.HandlingFailureHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.InterruptedModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.ManualHaltModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.ParentHaltedModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.PendingFailureHandlerHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.PendingInterruptHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.SubHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ThreadHaltReasonModel extends LHSerializable<ThreadHaltReason> {
    public ParentHaltedModel parentHalted;
    public InterruptedModel interrupted;
    public PendingInterruptHaltReasonModel pendingInterrupt;
    public HandlingFailureHaltReasonModel handlingFailure;
    public PendingFailureHandlerHaltReasonModel pendingFailure;
    public ManualHaltModel manualHalt;
    public HaltedByParentNodeHaltReasonModel haltedByParent;
    public ReasonCase type;
    public ThreadRunModel threadRun;
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
            case HALTED_BY_PARENT:
                return haltedByParent;
            case REASON_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        throw new RuntimeException("Not possible to get here");
    }

    public boolean isResolved() {
        return getSubHaltReason().isResolved(threadRun);
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
            case HALTED_BY_PARENT:
                out.setHaltedByParent(haltedByParent.toProto());
                break;
            case REASON_NOT_SET:
                throw new RuntimeException("not possible");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ThreadHaltReason p = (ThreadHaltReason) proto;
        type = p.getReasonCase();
        switch (type) {
            case PARENT_HALTED:
                parentHalted = ParentHaltedModel.fromProto(p.getParentHalted(), context);
                break;
            case INTERRUPTED:
                interrupted = InterruptedModel.fromProto(p.getInterrupted(), context);
                break;
            case PENDING_INTERRUPT:
                pendingInterrupt = PendingInterruptHaltReasonModel.fromProto(p.getPendingInterrupt(), context);
                break;
            case HANDLING_FAILURE:
                handlingFailure = HandlingFailureHaltReasonModel.fromProto(p.getHandlingFailure(), context);
                break;
            case PENDING_FAILURE:
                pendingFailure = PendingFailureHandlerHaltReasonModel.fromProto(p.getPendingFailure(), context);
                break;
            case MANUAL_HALT:
                manualHalt = ManualHaltModel.fromProto(p.getManualHalt(), context);
                break;
            case HALTED_BY_PARENT:
                haltedByParent = HaltedByParentNodeHaltReasonModel.fromProto(p.getHaltedByParent(), context);
                break;
            case REASON_NOT_SET:
                throw new RuntimeException("not possible");
        }
    }

    public boolean isTransitioningHaltState() {
        return type == ReasonCase.HANDLING_FAILURE
                || type == ReasonCase.PENDING_FAILURE
                || type == ReasonCase.PENDING_INTERRUPT;
    }

    public static ThreadHaltReasonModel fromProto(ThreadHaltReason p, ExecutionContext context) {
        ThreadHaltReasonModel out = new ThreadHaltReasonModel();
        out.initFrom(p, context);
        return out;
    }

    public ParentHaltedModel getParentHalted() {
        return this.parentHalted;
    }

    public InterruptedModel getInterrupted() {
        return this.interrupted;
    }

    public PendingInterruptHaltReasonModel getPendingInterrupt() {
        return this.pendingInterrupt;
    }

    public HandlingFailureHaltReasonModel getHandlingFailure() {
        return this.handlingFailure;
    }

    public PendingFailureHandlerHaltReasonModel getPendingFailure() {
        return this.pendingFailure;
    }

    public ManualHaltModel getManualHalt() {
        return this.manualHalt;
    }

    public HaltedByParentNodeHaltReasonModel getHaltedByParent() {
        return this.haltedByParent;
    }

    public ReasonCase getType() {
        return this.type;
    }

    public ThreadRunModel getThreadRun() {
        return this.threadRun;
    }

    public WfRunModel getWfRunModel() {
        return this.wfRunModel;
    }

    public void setParentHalted(final ParentHaltedModel parentHalted) {
        this.parentHalted = parentHalted;
    }

    public void setInterrupted(final InterruptedModel interrupted) {
        this.interrupted = interrupted;
    }

    public void setPendingInterrupt(final PendingInterruptHaltReasonModel pendingInterrupt) {
        this.pendingInterrupt = pendingInterrupt;
    }

    public void setHandlingFailure(final HandlingFailureHaltReasonModel handlingFailure) {
        this.handlingFailure = handlingFailure;
    }

    public void setPendingFailure(final PendingFailureHandlerHaltReasonModel pendingFailure) {
        this.pendingFailure = pendingFailure;
    }

    public void setManualHalt(final ManualHaltModel manualHalt) {
        this.manualHalt = manualHalt;
    }

    public void setHaltedByParent(final HaltedByParentNodeHaltReasonModel haltedByParent) {
        this.haltedByParent = haltedByParent;
    }

    public void setType(final ReasonCase type) {
        this.type = type;
    }

    public void setThreadRun(final ThreadRunModel threadRun) {
        this.threadRun = threadRun;
    }

    public void setWfRunModel(final WfRunModel wfRunModel) {
        this.wfRunModel = wfRunModel;
    }
}

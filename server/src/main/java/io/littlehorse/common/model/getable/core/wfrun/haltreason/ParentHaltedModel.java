package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ParentHalted;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentHaltedModel extends LHSerializable<ParentHalted> implements SubHaltReason {

    public int parentThreadId;

    @Override
    public Class<ParentHalted> getProtoBaseClass() {
        return ParentHalted.class;
    }

    @Override
    public ParentHalted.Builder toProto() {
        ParentHalted.Builder out = ParentHalted.newBuilder();
        out.setParentThreadId(parentThreadId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ParentHalted p = (ParentHalted) proto;
        parentThreadId = p.getParentThreadId();
    }

    @Override
    public boolean isResolved(ThreadRunModel haltedThread) {
        WfRunModel wfRun = haltedThread.getWfRun();
        ThreadRunModel parent = wfRun.getThreadRun(parentThreadId);

        if (parent.getStatus() == LHStatus.HALTING || parent.getStatus() == LHStatus.HALTED) {
            return isParentOnlyHaltedBecauseWeAreInterruptingIt(parent, haltedThread);
        } else if (parent.getStatus() == LHStatus.EXCEPTION || parent.getStatus() == LHStatus.ERROR) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isParentOnlyHaltedBecauseWeAreInterruptingIt(ThreadRunModel parent, ThreadRunModel haltedThread) {
        if (parent.getHaltReasons().size() > 1) return false;

        ThreadHaltReasonModel haltReason = parent.getHaltReasons().get(0);
        if (haltReason.getType() == ReasonCase.INTERRUPTED) {
            return haltReason.getInterrupted().interruptThreadId == haltedThread.getNumber();
        }
        return false;
    }

    public static ParentHaltedModel fromProto(ParentHalted proto, ExecutionContext context) {
        ParentHaltedModel out = new ParentHaltedModel();
        out.initFrom(proto, context);
        return out;
    }
}

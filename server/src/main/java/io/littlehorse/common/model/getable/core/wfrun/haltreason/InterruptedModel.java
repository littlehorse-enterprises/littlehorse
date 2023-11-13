package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.Interrupted;
import io.littlehorse.sdk.common.proto.LHStatus;

public class InterruptedModel extends LHSerializable<Interrupted> implements SubHaltReason {

    public int interruptThreadId;

    public boolean isResolved(WfRunModel wfRunModel) {
        ThreadRunModel iThread = wfRunModel.getThreadRun(interruptThreadId);
        return iThread.status == LHStatus.COMPLETED;
    }

    public Class<Interrupted> getProtoBaseClass() {
        return Interrupted.class;
    }

    public Interrupted.Builder toProto() {
        Interrupted.Builder out = Interrupted.newBuilder();
        out.setInterruptThreadId(interruptThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        Interrupted p = (Interrupted) proto;
        interruptThreadId = p.getInterruptThreadId();
    }

    public static InterruptedModel fromProto(Interrupted proto) {
        InterruptedModel out = new InterruptedModel();
        out.initFrom(proto);
        return out;
    }
}

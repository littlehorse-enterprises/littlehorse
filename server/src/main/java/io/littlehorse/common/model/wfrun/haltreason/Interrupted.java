package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.InterruptedPb;
import io.littlehorse.sdk.common.proto.LHStatus;

public class Interrupted
    extends LHSerializable<InterruptedPb>
    implements SubHaltReason {

    public int interruptThreadId;

    public boolean isResolved(WfRunModel wfRunModel) {
        ThreadRunModel iThread = wfRunModel.threadRunModels.get(interruptThreadId);
        return iThread.status == LHStatus.COMPLETED;
    }

    public Class<InterruptedPb> getProtoBaseClass() {
        return InterruptedPb.class;
    }

    public InterruptedPb.Builder toProto() {
        InterruptedPb.Builder out = InterruptedPb.newBuilder();
        out.setInterruptThreadId(interruptThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        InterruptedPb p = (InterruptedPb) proto;
        interruptThreadId = p.getInterruptThreadId();
    }

    public static Interrupted fromProto(InterruptedPb proto) {
        Interrupted out = new Interrupted();
        out.initFrom(proto);
        return out;
    }
}

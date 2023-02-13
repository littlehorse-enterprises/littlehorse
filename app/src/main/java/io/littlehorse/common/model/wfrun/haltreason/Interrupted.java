package io.littlehorse.common.model.wfrun.haltreason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.InterruptedPb;
import io.littlehorse.jlib.common.proto.InterruptedPbOrBuilder;
import io.littlehorse.jlib.common.proto.LHStatusPb;

public class Interrupted
    extends LHSerializable<InterruptedPb>
    implements SubHaltReason {

    public int interruptThreadId;

    @JsonIgnore
    public boolean isResolved(WfRun wfRun) {
        ThreadRun iThread = wfRun.threadRuns.get(interruptThreadId);
        return iThread.status == LHStatusPb.COMPLETED;
    }

    public Class<InterruptedPb> getProtoBaseClass() {
        return InterruptedPb.class;
    }

    public InterruptedPb.Builder toProto() {
        InterruptedPb.Builder out = InterruptedPb.newBuilder();
        out.setInterruptThreadId(interruptThreadId);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        InterruptedPbOrBuilder p = (InterruptedPbOrBuilder) proto;
        interruptThreadId = p.getInterruptThreadId();
    }

    public static Interrupted fromProto(InterruptedPbOrBuilder proto) {
        Interrupted out = new Interrupted();
        out.initFrom(proto);
        return out;
    }
}

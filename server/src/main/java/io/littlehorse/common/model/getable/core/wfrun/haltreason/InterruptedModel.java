package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.Interrupted;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class InterruptedModel extends LHSerializable<Interrupted> implements SubHaltReason {

    public int interruptThreadId;

    public boolean isResolved(ThreadRunModel haltedThread) {
        WfRunModel wfRunModel = haltedThread.getWfRun();
        ThreadRunModel iThread = wfRunModel.getThreadRun(interruptThreadId);
        if (iThread.status == LHStatus.COMPLETED) {
            return true;
        }
        return false;
    }

    public Class<Interrupted> getProtoBaseClass() {
        return Interrupted.class;
    }

    public Interrupted.Builder toProto() {
        Interrupted.Builder out = Interrupted.newBuilder();
        out.setInterruptThreadId(interruptThreadId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Interrupted p = (Interrupted) proto;
        interruptThreadId = p.getInterruptThreadId();
    }

    public static InterruptedModel fromProto(Interrupted proto, ExecutionContext context) {
        InterruptedModel out = new InterruptedModel();
        out.initFrom(proto, context);
        return out;
    }
}

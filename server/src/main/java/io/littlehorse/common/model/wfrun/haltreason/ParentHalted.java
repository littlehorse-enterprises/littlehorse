package io.littlehorse.common.model.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ParentHaltedPb;

public class ParentHalted
    extends LHSerializable<ParentHaltedPb>
    implements SubHaltReason {

    public int parentThreadId;

    public boolean isResolved(WfRunModel wfRunModel) {
        ThreadRunModel parent = wfRunModel.threadRunModels.get(parentThreadId);
        if (parent.status == LHStatus.COMPLETED) {
            throw new RuntimeException("Not possible.");
        }

        // If parent status is ERROR, then the thread halt reason is still valid.
        return (
            parent.status == LHStatus.RUNNING || parent.status == LHStatus.STARTING
        );
    }

    public Class<ParentHaltedPb> getProtoBaseClass() {
        return ParentHaltedPb.class;
    }

    public ParentHaltedPb.Builder toProto() {
        ParentHaltedPb.Builder out = ParentHaltedPb.newBuilder();
        out.setParentThreadId(parentThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        ParentHaltedPb p = (ParentHaltedPb) proto;
        parentThreadId = p.getParentThreadId();
    }

    public static ParentHalted fromProto(ParentHaltedPb proto) {
        ParentHalted out = new ParentHalted();
        out.initFrom(proto);
        return out;
    }
}

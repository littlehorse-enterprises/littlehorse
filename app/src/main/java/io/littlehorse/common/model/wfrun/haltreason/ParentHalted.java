package io.littlehorse.common.model.wfrun.haltreason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.ParentHaltedPb;
import io.littlehorse.jlib.common.proto.ParentHaltedPbOrBuilder;

public class ParentHalted
    extends LHSerializable<ParentHaltedPb>
    implements SubHaltReason {

    public int parentThreadId;

    @JsonIgnore
    public boolean isResolved(WfRun wfRun) {
        ThreadRun parent = wfRun.threadRuns.get(parentThreadId);
        if (parent.status == LHStatusPb.COMPLETED) {
            throw new RuntimeException("Not possible.");
        }

        // If parent status is ERROR, then the thread halt reason is still valid.
        return (
            parent.status == LHStatusPb.RUNNING ||
            parent.status == LHStatusPb.STARTING
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

    public void initFrom(MessageOrBuilder proto) {
        ParentHaltedPbOrBuilder p = (ParentHaltedPbOrBuilder) proto;
        parentThreadId = p.getParentThreadId();
    }

    public static ParentHalted fromProto(ParentHaltedPbOrBuilder proto) {
        ParentHalted out = new ParentHalted();
        out.initFrom(proto);
        return out;
    }
}

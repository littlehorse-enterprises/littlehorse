package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ParentHalted;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentHaltedModel extends LHSerializable<ParentHalted> implements SubHaltReason {

    public int parentThreadId;

    public boolean isResolved(WfRunModel wfRunModel) {
        ThreadRunModel parent = wfRunModel.getThreadRun(parentThreadId);
        if (parent.status == LHStatus.COMPLETED) {
            throw new RuntimeException("Not possible.");
        }

        // If parent status is ERROR, then the thread halt reason is still valid.
        return (parent.status == LHStatus.RUNNING || parent.status == LHStatus.STARTING);
    }

    public Class<ParentHalted> getProtoBaseClass() {
        return ParentHalted.class;
    }

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

    public static ParentHaltedModel fromProto(ParentHalted proto, ExecutionContext context) {
        ParentHaltedModel out = new ParentHaltedModel();
        out.initFrom(proto, context);
        return out;
    }
}

package io.littlehorse.common.model.getable.core.wfrun.haltreason;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.HaltedByParentNodeHaltReason;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HaltedByParentNodeHaltReasonModel extends LHSerializable<HaltedByParentNodeHaltReason>
        implements SubHaltReason {

    private int parentThreadRunNumber;
    private int waitingNodeRunPosition;

    public HaltedByParentNodeHaltReasonModel() {}

    public HaltedByParentNodeHaltReasonModel(int parentThreadRunNumber, int waitingNodeRunPosition) {
        this.parentThreadRunNumber = parentThreadRunNumber;
        this.waitingNodeRunPosition = waitingNodeRunPosition;
    }

    @Override
    public Class<HaltedByParentNodeHaltReason> getProtoBaseClass() {
        return HaltedByParentNodeHaltReason.class;
    }

    @Override
    public HaltedByParentNodeHaltReason.Builder toProto() {
        HaltedByParentNodeHaltReason.Builder out = HaltedByParentNodeHaltReason.newBuilder();
        out.setParentThreadRunNumber(parentThreadRunNumber);
        out.setWaitingNodeRunPosition(waitingNodeRunPosition);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        HaltedByParentNodeHaltReason p = (HaltedByParentNodeHaltReason) proto;
        parentThreadRunNumber = p.getParentThreadRunNumber();
        waitingNodeRunPosition = p.getWaitingNodeRunPosition();
    }

    @Override
    public boolean isResolved(ThreadRunModel haltedThreadRun) {
        // When halted by a parent node, there is no going back.
        return false;
    }

    public static HaltedByParentNodeHaltReasonModel fromProto(
            HaltedByParentNodeHaltReason proto, ExecutionContext context) {
        HaltedByParentNodeHaltReasonModel out = new HaltedByParentNodeHaltReasonModel();
        out.initFrom(proto, context);
        return out;
    }
}

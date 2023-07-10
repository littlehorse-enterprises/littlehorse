package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.subnoderun.WaitThreadRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.jlib.common.proto.WaitForThreadNodePb;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class WaitForThreadNode extends SubNode<WaitForThreadNodePb> {

    public VariableAssignment threadRunNumber;

    public Class<WaitForThreadNodePb> getProtoBaseClass() {
        return WaitForThreadNodePb.class;
    }

    public void initFrom(Message proto) {
        WaitForThreadNodePb p = (WaitForThreadNodePb) proto;
        threadRunNumber = VariableAssignment.fromProto(p.getThreadRunNumber());
    }

    public WaitForThreadNodePb.Builder toProto() {
        WaitForThreadNodePb.Builder out = WaitForThreadNodePb.newBuilder();
        out.setThreadRunNumber(threadRunNumber.toProto());
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        if (!threadRunNumber.canBeType(VariableTypePb.INT, node.threadSpec)) {
            throw new LHValidationError(
                null,
                "`threadRunNumber` for WAIT_FOR_THREAD node must resolve to INT!"
            );
        }
    }

    public WaitThreadRun createSubNodeRun(Date time) {
        return new WaitThreadRun();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        out.addAll(threadRunNumber.getRequiredWfRunVarNames());

        return out;
    }
}

package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.ThreadToWaitFor;
import io.littlehorse.common.model.wfrun.subnoderun.WaitForThreadsRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNodePb;
import io.littlehorse.sdk.common.proto.WaitForThreadsNodePb.ThreadToWaitForPb;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitForThreadsNode extends SubNode<WaitForThreadsNodePb> {

    private List<ThreadToWaitFor> threads;

    public Class<WaitForThreadsNodePb> getProtoBaseClass() {
        return WaitForThreadsNodePb.class;
    }

    public WaitForThreadsNode() {
        threads = new ArrayList<>();
    }

    public void initFrom(Message proto) {
        WaitForThreadsNodePb p = (WaitForThreadsNodePb) proto;
        for (ThreadToWaitForPb ttwf : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(ttwf, ThreadToWaitFor.class));
        }
    }

    public WaitForThreadsNodePb.Builder toProto() {
        WaitForThreadsNodePb.Builder out = WaitForThreadsNodePb.newBuilder();
        for (ThreadToWaitFor ttwf : threads) {
            out.addThreads(ttwf.toProto());
        }

        return out;
    }

    public WaitForThreadsRun createSubNodeRun(Date time) {
        return new WaitForThreadsRun();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (ThreadToWaitFor ttwf : threads) {
            out.addAll(ttwf.getThreadRunNumber().getRequiredWfRunVarNames());
        }

        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        for (ThreadToWaitFor ttwf : threads) {
            if (
                !ttwf
                    .getThreadRunNumber()
                    .canBeType(VariableType.INT, node.threadSpecModel)
            ) {
                throw new LHValidationError(
                    null,
                    "`threadRunNumber` for WAIT_FOR_THREAD node must resolve to INT!"
                );
            }
        }
    }
}

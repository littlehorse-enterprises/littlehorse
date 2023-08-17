package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.ThreadToWaitForModel;
import io.littlehorse.common.model.wfrun.subnoderun.WaitForThreadsRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadToWaitFor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitForThreadsNodeModel extends SubNode<WaitForThreadsNode> {

    private List<ThreadToWaitForModel> threads;

    public Class<WaitForThreadsNode> getProtoBaseClass() {
        return WaitForThreadsNode.class;
    }

    public WaitForThreadsNodeModel() {
        threads = new ArrayList<>();
    }

    public void initFrom(Message proto) {
        WaitForThreadsNode p = (WaitForThreadsNode) proto;
        for (ThreadToWaitFor ttwf : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(ttwf, ThreadToWaitForModel.class));
        }
    }

    public WaitForThreadsNode.Builder toProto() {
        WaitForThreadsNode.Builder out = WaitForThreadsNode.newBuilder();
        for (ThreadToWaitForModel ttwf : threads) {
            out.addThreads(ttwf.toProto());
        }

        return out;
    }

    public WaitForThreadsRunModel createSubNodeRun(Date time) {
        return new WaitForThreadsRunModel();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (ThreadToWaitForModel ttwf : threads) {
            out.addAll(ttwf.getThreadRunNumber().getRequiredWfRunVarNames());
        }

        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        for (ThreadToWaitForModel ttwf : threads) {
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

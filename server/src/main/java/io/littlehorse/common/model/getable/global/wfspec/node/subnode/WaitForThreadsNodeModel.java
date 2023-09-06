package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForThreadsRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.node.ThreadToWaitForModel;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadToWaitFor;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
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

    private WaitForThreadsPolicy policy;

    private VariableAssignment listThreads;

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
        policy = p.getPolicy();
        if (p.hasThreadList()) {
            listThreads = p.getThreadList();
        }
    }

    public WaitForThreadsNode.Builder toProto() {
        WaitForThreadsNode.Builder out = WaitForThreadsNode.newBuilder();
        for (ThreadToWaitForModel ttwf : threads) {
            out.addThreads(ttwf.toProto());
        }
        out.setPolicy(policy);
        if (listThreads != null) {
            out.setThreadList(listThreads);
        }
        return out;
    }

    public WaitForThreadsRunModel createSubNodeRun(Date time) {
        WaitForThreadsRunModel waitForThreadsRun = new WaitForThreadsRunModel();
        waitForThreadsRun.setPolicy(getPolicy());
        return waitForThreadsRun;
    }

    @Override
    public Set<String> getNeededVariableNames() {
        Set<String> out = new HashSet<>();
        for (ThreadToWaitForModel ttwf : threads) {
            out.addAll(ttwf.getThreadRunNumber().getRequiredWfRunVarNames());
        }

        return out;
    }

    public void validate(ReadOnlyMetadataStore stores, LHServerConfig config) throws LHApiException {
        for (ThreadToWaitForModel ttwf : threads) {
            if (!ttwf.getThreadRunNumber().canBeType(VariableType.INT, node.threadSpecModel)) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "`threadRunNumber` for WAIT_FOR_THREAD node must resolve to INT!");
            }
        }
    }
}

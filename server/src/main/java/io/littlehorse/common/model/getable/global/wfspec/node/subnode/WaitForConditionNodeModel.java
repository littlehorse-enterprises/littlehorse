package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForConditionNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.EdgeConditionModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.WaitForConditionNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import java.util.Date;
import java.util.Set;
import lombok.Getter;

public class WaitForConditionNodeModel extends SubNode<WaitForConditionNode> {

    @Getter
    private EdgeConditionModel condition;

    @Override
    public Class<WaitForConditionNode> getProtoBaseClass() {
        return WaitForConditionNode.class;
    }

    @Override
    public WaitForConditionNode.Builder toProto() {
        WaitForConditionNode.Builder out = WaitForConditionNode.newBuilder().setCondition(condition.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        WaitForConditionNode p = (WaitForConditionNode) proto;
        this.condition = LHSerializable.fromProto(p.getCondition(), EdgeConditionModel.class, ctx);
    }

    @Override
    public void validate(MetadataProcessorContext context) {
        condition.validate();
    }

    @Override
    public Set<String> getNeededVariableNames() {
        return condition.getRequiredVariableNames();
    }

    @Override
    public WaitForConditionNodeRunModel createSubNodeRun(Date time, CoreProcessorContext ctx) {
        // There is no initialization needed. The `SubNodeRun#checkIfProcessingCompleted()` method
        // does everything we need.
        return new WaitForConditionNodeRunModel();
    }
}

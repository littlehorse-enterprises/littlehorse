package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForConditionNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.LegacyEdgeConditionModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.WaitForConditionNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

public class WaitForConditionNodeModel extends SubNode<WaitForConditionNode> {

    @Getter
    private LegacyEdgeConditionModel legacyCondition;

    @Getter
    private VariableAssignmentModel condition;

    @Override
    public Class<WaitForConditionNode> getProtoBaseClass() {
        return WaitForConditionNode.class;
    }

    @Override
    public WaitForConditionNode.Builder toProto() {
        WaitForConditionNode.Builder out = WaitForConditionNode.newBuilder();
        if (condition != null) {
            out.setCondition(condition.toProto());
        }
        if (legacyCondition != null) {
            out.setLegacyCondition(legacyCondition.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        WaitForConditionNode p = (WaitForConditionNode) proto;
        if (p.hasCondition()) {
            condition = VariableAssignmentModel.fromProto(p.getCondition(), ctx);
        }
        if (p.hasLegacyCondition()) {
            legacyCondition = LegacyEdgeConditionModel.fromProto(p.getLegacyCondition(), ctx);
        }
    }

    @Override
    public void validate(MetadataProcessorContext context) throws InvalidNodeException {
        try {
            if (condition != null) {
                condition.validate(node, context.metadataManager(), node.threadSpec);
            }
            if (legacyCondition != null) {
                legacyCondition.validate(node, context.metadataManager(), node.threadSpec);
            }
        } catch (LHValidationException exn) {
            throw new InvalidNodeException(exn, node);
        }
    }

    @Override
    public Collection<String> getNeededVariableNames() {
        if (legacyCondition != null) {
            return legacyCondition.getRequiredVariableNames();
        }
        return condition.getRequiredVariableNames();
    }

    @Override
    public WaitForConditionNodeRunModel createSubNodeRun(Date time, CoreProcessorContext ctx) {
        // There is no initialization needed. The `SubNodeRun#checkIfProcessingCompleted()` method
        // does everything we need.
        return new WaitForConditionNodeRunModel();
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // No output
        return Optional.of(new ReturnTypeModel());
    }

    public boolean isSatisfied(ThreadRunModel threadRun) throws NodeFailureException {
        try {
            if (legacyCondition != null) {
                return legacyCondition.isSatisfied(threadRun);
            } else {
                return condition.isSatisfied(threadRun);
            }
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed evaluating condition on WAIT_FOR_CONDITION_NODE: " + exn.getMessage(),
                    LHErrorType.VAR_SUB_ERROR.name()));
        }
    }
}

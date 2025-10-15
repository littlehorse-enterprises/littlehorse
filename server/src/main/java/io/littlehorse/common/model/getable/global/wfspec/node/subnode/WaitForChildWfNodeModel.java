package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForChildWfNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForChildWfNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

@Getter
public class WaitForChildWfNodeModel extends SubNode<WaitForChildWfNode> {

    private VariableAssignmentModel childWfRunId;
    private String childWfRunSourceNode;

    @Override
    public Class<WaitForChildWfNode> getProtoBaseClass() {
        return WaitForChildWfNode.class;
    }

    @Override
    public WaitForChildWfNode.Builder toProto() {
        return WaitForChildWfNode.newBuilder()
                .setChildWfRunId(childWfRunId.toProto())
                .setChildWfRunSourceNode(childWfRunSourceNode);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        WaitForChildWfNode p = (WaitForChildWfNode) proto;
        this.childWfRunSourceNode = p.getChildWfRunSourceNode();
        this.childWfRunId = VariableAssignmentModel.fromProto(p.getChildWfRunId(), ignored);
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        WfSpecModel childWfSpec = getSourceNode().getRunChildWfNode().getWfSpecToRun(manager);
        return childWfSpec.getOutputType(manager);
    }

    @Override
    public Set<String> getNeededVariableNames() {
        return childWfRunId.getRequiredWfRunVarNames();
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        NodeModel sourceNode = getSourceNode();
        if (sourceNode == null) {
            throw new InvalidNodeException(
                    "Specified node " + childWfRunSourceNode + " does not exist in threadSpec", node);
        }
        if (sourceNode.getRunChildWfNode() == null) {
            throw new InvalidNodeException(
                    "Specified node " + childWfRunSourceNode + " is not of type RunChildWf", node);
        }

        // Now validate the input
        if (!childWfRunId.canBeType(VariableType.WF_RUN_ID, node.getThreadSpec())) {
            throw new InvalidNodeException("Provided value is not a valid WF_RUN_ID", node);
        }
    }

    @Override
    public WaitForChildWfNodeRunModel createSubNodeRun(Date arrivalTime, CoreProcessorContext ctx) {
        return new WaitForChildWfNodeRunModel();
    }

    private NodeModel getSourceNode() {
        ThreadSpecModel threadSpec = node.getThreadSpec();
        return threadSpec.getNode(childWfRunSourceNode);
    }
}

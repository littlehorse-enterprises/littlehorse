package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ThrowEventNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ThrowEventNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;

public class ThrowEventNodeModel extends SubNode<ThrowEventNode> {
    private WorkflowEventDefIdModel workflowEventDefId;
    private VariableAssignmentModel variableAssignment;
    private ProcessorExecutionContext processorContext;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ThrowEventNode p = (ThrowEventNode) proto;
        this.workflowEventDefId = LHSerializable.fromProto(p.getEventDefId(), WorkflowEventDefIdModel.class, context);
        this.variableAssignment = LHSerializable.fromProto(p.getContent(), VariableAssignmentModel.class, context);
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    @Override
    public ThrowEventNode.Builder toProto() {
        return ThrowEventNode.newBuilder()
                .setEventDefId(workflowEventDefId.toProto())
                .setContent(variableAssignment.toProto());
    }

    @Override
    public Class<ThrowEventNode> getProtoBaseClass() {
        return ThrowEventNode.class;
    }

    @Override
    public SubNodeRun<?> createSubNodeRun(Date time) {
        return new ThrowEventNodeRunModel(workflowEventDefId, processorContext);
    }

    @Override
    public void validate() throws LHApiException {}
}

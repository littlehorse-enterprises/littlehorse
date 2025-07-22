package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ThrowEventNodeRunModel;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ThrowEventNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

public class ThrowEventNodeModel extends SubNode<ThrowEventNode> {

    @Getter
    private WorkflowEventDefIdModel workflowEventDefId;

    @Getter
    private VariableAssignmentModel content;

    private ExecutionContext context;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ThrowEventNode p = (ThrowEventNode) proto;
        this.workflowEventDefId = LHSerializable.fromProto(p.getEventDefId(), WorkflowEventDefIdModel.class, context);
        this.content = LHSerializable.fromProto(p.getContent(), VariableAssignmentModel.class, context);
        this.context = context;
    }

    @Override
    public ThrowEventNode.Builder toProto() {
        return ThrowEventNode.newBuilder()
                .setEventDefId(workflowEventDefId.toProto())
                .setContent(content.toProto());
    }

    @Override
    public Class<ThrowEventNode> getProtoBaseClass() {
        return ThrowEventNode.class;
    }

    @Override
    public SubNodeRun<?> createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new ThrowEventNodeRunModel(workflowEventDefId, context.castOnSupport(CoreProcessorContext.class));
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        WorkflowEventDefModel eventDef = context.service().getWorkflowEventDef(workflowEventDefId);
        if (eventDef == null) {
            throw new InvalidNodeException("Refers to missing workflowEventDef %s".formatted(workflowEventDefId), node);
        }
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        return Optional.of(new ReturnTypeModel());
    }
}

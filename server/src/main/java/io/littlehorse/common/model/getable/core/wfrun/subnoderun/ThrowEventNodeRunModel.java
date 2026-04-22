package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.events.WorkflowEventDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.ThrowEventNodeRun;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ThrowEventNodeRunModel extends SubNodeRun<ThrowEventNodeRun> {
    private WorkflowEventIdModel workflowEventId;
    private CoreProcessorContext processorExecutionContext;
    private WorkflowEventDefIdModel eventDefId;

    public ThrowEventNodeRunModel() {}

    public ThrowEventNodeRunModel(WorkflowEventDefIdModel defId, CoreProcessorContext processorContext) {
        this.eventDefId = defId;
        this.processorExecutionContext = processorContext;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        this.processorExecutionContext = context.castOnSupport(CoreProcessorContext.class);
        ThrowEventNodeRun p = (ThrowEventNodeRun) proto;
        this.workflowEventId = LHSerializable.fromProto(p.getWorkflowEventId(), WorkflowEventIdModel.class, context);
    }

    @Override
    public ThrowEventNodeRun.Builder toProto() {
        return ThrowEventNodeRun.newBuilder().setWorkflowEventId(workflowEventId.toProto());
    }

    @Override
    public Class<ThrowEventNodeRun> getProtoBaseClass() {
        return ThrowEventNodeRun.class;
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        Optional<WorkflowEventModel> currentWorkflowEvent =
                getCurrentWorkflowEventId(nodeRun.getId().getWfRunId(), this.eventDefId, processorContext);
        if (currentWorkflowEvent.isPresent()) {
            int currentId = currentWorkflowEvent.get().getId().getId();
            workflowEventId = new WorkflowEventIdModel(nodeRun.getId().getWfRunId(), this.eventDefId, ++currentId);
        } else {
            workflowEventId = new WorkflowEventIdModel(nodeRun.getId().getWfRunId(), this.eventDefId, 0);
        }
        try {
            VariableValueModel content = getNodeRun()
                    .getThreadRun()
                    .assignVariable(getNode().getThrowEventNode().getContent());
            // If the WorkflowEventDef defines a return type and it's an inline array,
            // set the authoritative element type on the produced content array so that
            // empty/native arrays inherit the expected element type (matches RunWf/Task
            // and ExternalEvent ingress behavior).
            WorkflowEventDefModel wed = processorContext.service().getWorkflowEventDef(eventDefId);
            if (wed != null
                    && wed.getContentType() != null
                    && content != null
                    && content.getValueType() == VariableValue.ValueCase.ARRAY) {
                Optional<TypeDefinitionModel> out = wed.getContentType().getOutputType();
                if (out.isPresent()
                        && out.get().getDefinedTypeCase() == TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF) {
                    content.getArray()
                            .setElementType(out.get().getInlineArrayDef().getArrayType());
                }
            }
            WorkflowEventModel event = new WorkflowEventModel(workflowEventId, content, nodeRun);
            processorContext.getableManager().put(event);
            processorContext.notifyOfEventThrown(event);
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed calculating content of workflow Event: %s".formatted(exn.getMessage()),
                    LHErrorType.VAR_SUB_ERROR.toString()));
        }
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        return true;
    }

    private Optional<WorkflowEventModel> getCurrentWorkflowEventId(
            WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId, CoreProcessorContext processorContext) {
        return processorContext.getableManager().getWorkflowEvents(wfRunId, eventDefId).stream()
                .max(Comparator.comparingInt(
                        workflowEvent -> workflowEvent.getId().getId()));
    }

    @Override
    public List<? extends CoreObjectId<?, ?, ?>> getCreatedSubGetableIds(CoreProcessorContext context) {
        return List.of(workflowEventId);
    }

    public void setWorkflowEventId(final WorkflowEventIdModel workflowEventId) {
        this.workflowEventId = workflowEventId;
    }

    public void setProcessorExecutionContext(final CoreProcessorContext processorExecutionContext) {
        this.processorExecutionContext = processorExecutionContext;
    }

    public void setEventDefId(final WorkflowEventDefIdModel eventDefId) {
        this.eventDefId = eventDefId;
    }
}

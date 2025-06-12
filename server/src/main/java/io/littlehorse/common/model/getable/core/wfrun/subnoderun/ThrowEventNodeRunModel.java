package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.ThrowEventNodeRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import lombok.Setter;

@Setter
public class ThrowEventNodeRunModel extends SubNodeRun<ThrowEventNodeRun> {

    private WorkflowEventIdModel workflowEventId;
    private ProcessorExecutionContext processorExecutionContext;
    private WorkflowEventDefIdModel eventDefId;

    public ThrowEventNodeRunModel() {}

    public ThrowEventNodeRunModel(WorkflowEventDefIdModel defId, ProcessorExecutionContext processorContext) {
        this.eventDefId = defId;
        this.processorExecutionContext = processorContext;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        this.processorExecutionContext = context.castOnSupport(ProcessorExecutionContext.class);
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
    public void arrive(Date time, ProcessorExecutionContext processorContext) throws NodeFailureException {
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
    public Optional<VariableValueModel> getOutput(ProcessorExecutionContext processorContext) {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted(ProcessorExecutionContext processorContext) throws NodeFailureException {
        return true;
    }

    private Optional<WorkflowEventModel> getCurrentWorkflowEventId(
            WfRunIdModel wfRunId, WorkflowEventDefIdModel eventDefId, ProcessorExecutionContext processorContext) {
        return processorContext.getableManager().getWorkflowEvents(wfRunId, eventDefId).stream()
                .max(Comparator.comparingInt(
                        workflowEvent -> workflowEvent.getId().getId()));
    }

    @Override
    public Optional<WorkflowEventIdModel> getCreatedSubGetableId() {
        return Optional.ofNullable(workflowEventId);
    }
}

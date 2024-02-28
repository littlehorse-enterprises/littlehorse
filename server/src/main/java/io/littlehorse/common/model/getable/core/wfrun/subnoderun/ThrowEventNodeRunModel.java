package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ThrowEventNodeRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
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
    public void arrive(Date time) throws NodeFailureException {
        workflowEventId = new WorkflowEventIdModel(nodeRun.getId().getWfRunId(), this.eventDefId, 0);
        WorkflowEventModel event = new WorkflowEventModel(workflowEventId, new VariableValueModel());
        processorExecutionContext.getableManager().put(event);

        processorExecutionContext.notifyOfEventThrown(event);
    }

    @Override
    public Optional<VariableValueModel> getOutput() {
        return Optional.empty();
    }

    @Override
    public boolean checkIfProcessingCompleted() throws NodeFailureException {
        return true;
    }
}

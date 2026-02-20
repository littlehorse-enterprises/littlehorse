package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.WaitForChildWfNode;
import io.littlehorse.sdk.common.proto.WaitForChildWfNodeRun;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class WaitForChildWfNodeRunModel extends SubNodeRun<WaitForChildWfNodeRun> {

    private WfRunIdModel childWfRunId;

    @Override
    public Class<WaitForChildWfNode> getProtoBaseClass() {
        return WaitForChildWfNode.class;
    }

    @Override
    public WaitForChildWfNodeRun.Builder toProto() {
        WaitForChildWfNodeRun.Builder out = WaitForChildWfNodeRun.newBuilder();

        if (childWfRunId != null) out.setChildWfRunId(childWfRunId.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        WaitForChildWfNodeRun p = (WaitForChildWfNodeRun) proto;
        if (p.hasChildWfRunId()) {
            this.childWfRunId = LHSerializable.fromProto(p.getChildWfRunId(), WfRunIdModel.class, ignored);
        }
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        try {
            VariableValueModel result = nodeRun.getThreadRun()
                    .assignVariable(getNode().getWaitForChildWfNode().getChildWfRunId());

            if (result.getWfRunId() == null) {
                throw new NodeFailureException(new FailureModel(
                        "Received non-WF_RUN_ID value of type " + result.getTypeDefinition(),
                        LHErrorType.VAR_SUB_ERROR.toString()));
            }

            this.childWfRunId = result.getWfRunId();

            GetableManager manager = processorContext.getableManager();
            WfRunModel childWf = manager.get(this.childWfRunId);
            if (childWf != null && childWf.getParentTrigger() != null) {
                childWf.getParentTrigger().setWaitingNodeRun(nodeRun.getId());
            }
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed to assign WfRunId to wait for: " + exn.getMessage(), LHErrorType.VAR_SUB_ERROR.toString()));
        }
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        GetableManager manager = processorContext.getableManager();
        WfRunModel childWf = manager.get(this.childWfRunId);

        switch (childWf.getStatus()) {
            case STARTING:
            case RUNNING:
            case HALTED:
            case HALTING:
                return false;
            case COMPLETED:
                return true;
            case ERROR:
                throw new NodeFailureException(new FailureModel(
                        "Child WfRun " + childWfRunId + " failed", LHErrorType.CHILD_FAILURE.toString()));
            case EXCEPTION:
                FailureModel childFailure = childWf.getThreadRun(0)
                        .getCurrentNodeRun()
                        .getLatestFailure()
                        .get();
                throw new NodeFailureException(childFailure.copyWithPrefix("Child WfRun failed: "));
            case UNRECOGNIZED:
        }
        throw new IllegalStateException("unrecognized proto enum value");
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        WfRunModel childWfRun = processorContext.getableManager().get(childWfRunId);
        return Optional.ofNullable(childWfRun.getThreadRun(0).getOutput());
    }
}

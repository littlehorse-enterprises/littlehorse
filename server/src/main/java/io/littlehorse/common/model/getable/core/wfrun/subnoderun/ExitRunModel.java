package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExitNodeModel;
import io.littlehorse.sdk.common.proto.ExitRun;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;

public class ExitRunModel extends SubNodeRun<ExitRun> {

    public ExitRunModel() {}

    @Override
    public Class<ExitRun> getProtoBaseClass() {
        return ExitRun.class;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {}

    @Override
    public ExitRun.Builder toProto() {
        return ExitRun.newBuilder();
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) {}

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        // If the EXIT node has a failure defined, the whole point of the Node is to fail.
        ExitNodeModel node = getNode().getExitNode();
        if (node.getFailureDef() != null) {
            FailureModel failureToThrow = node.getFailureDef().getFailure(nodeRun.getThreadRun());
            throw new NodeFailureException(failureToThrow);
        }

        // Next, check all children.
        boolean allComplete = true;
        String failedChildren = "";

        for (int childId : nodeRun.getThreadRun().getChildThreadIds()) {
            ThreadRunModel child = getWfRun().getThreadRun(childId);
            if (!(child.isTerminated() || child.isHalted())) {
                // Can't exit yet.
                return false;
            }
            if (child.status != LHStatus.COMPLETED && !child.isHalted()) {
                if (!nodeRun.getThreadRun().getHandledFailedChildren().contains(childId)) {
                    allComplete = false;

                    // lolz this is silly but it works:
                    failedChildren += " " + child.number;
                }
            }
        }

        if (allComplete) {
            maybeStoreOutputIntoThreadRun(processorContext);
            return true;
        } else {
            throw new NodeFailureException(
                    new FailureModel("Child thread (or threads) failed:" + failedChildren, LHConstants.CHILD_FAILURE));
        }
    }

    private void maybeStoreOutputIntoThreadRun(CoreProcessorContext ctx) throws NodeFailureException {
        ThreadRunModel thread = getNodeRun().getThreadRun();
        ExitNodeModel exitNode = nodeRun.getNode().getExitNode();
        if (exitNode.getReturnContent() == null) return;

        try {
            VariableValueModel result = thread.assignVariable(exitNode.getReturnContent());
            thread.setOutput(result);
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel(
                    "Failed calculating threadRun output: " + exn.getMessage(), LHErrorType.VAR_ERROR.name());
            throw new NodeFailureException(failure);
        }
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        return Optional.empty();
    }

    public static ExitRunModel fromProto(ExitRun p, ExecutionContext context) {
        ExitRunModel out = new ExitRunModel();
        out.initFrom(p, context);
        return out;
    }
}

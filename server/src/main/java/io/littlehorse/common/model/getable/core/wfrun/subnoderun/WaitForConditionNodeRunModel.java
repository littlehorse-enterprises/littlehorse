package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForConditionNodeModel;
import io.littlehorse.sdk.common.proto.WaitForConditionRun;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;

public class WaitForConditionNodeRunModel extends SubNodeRun<WaitForConditionRun> {

    @Override
    public Class<WaitForConditionRun> getProtoBaseClass() {
        return WaitForConditionRun.class;
    }

    @Override
    public WaitForConditionRun.Builder toProto() {
        return WaitForConditionRun.newBuilder();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        // Nothing to do, as of now this nodeRun is empty.
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext ctx) throws NodeFailureException {
        // First, evaluate the edge conditions
        WaitForConditionNodeModel wfcNode = nodeRun.getNode().getWaitForConditionNode();
        return wfcNode.isSatisfied(nodeRun.getThreadRun());
    }

    @Override
    public void arrive(Date time, CoreProcessorContext ctx) {
        // Nothing to do on arrival.
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext ctx) {
        // There is no output from a `WAIT_FOR_CONDITION` node.
        return Optional.empty();
    }

    @Override
    public boolean maybeHalt(CoreProcessorContext ctx) {
        // There is never an action (eg. TaskAttempt) in progress during a
        // WaitForConditionRun, so it's safe to always halt.
        return true;
    }
}

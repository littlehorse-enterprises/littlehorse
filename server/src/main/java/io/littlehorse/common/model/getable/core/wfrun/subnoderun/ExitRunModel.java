package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.sdk.common.proto.ExitRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class ExitRunModel extends SubNodeRun<ExitRun> {

    //
    private boolean alreadyNoticed;

    public ExitRunModel() {
        alreadyNoticed = false;
    }

    public Class<ExitRun> getProtoBaseClass() {
        return ExitRun.class;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {}

    public ExitRun.Builder toProto() {
        return ExitRun.newBuilder();
    }

    public static ExitRunModel fromProto(ExitRun p, ExecutionContext context) {
        ExitRunModel out = new ExitRunModel();
        out.initFrom(p, context);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        boolean out;
        // nothing to do
        if (nodeRun.isInProgress()) {
            arrive(time);
            // Return true if the status changed
            out = !(nodeRun.getThreadRun().isRunning());
        } else {
            if (alreadyNoticed) {
                out = false;
            } else {
                alreadyNoticed = true;
                out = true;
            }
        }
        return out;
    }

    public void arrive(Date time) {
        // First check all children.
        boolean allComplete = true;
        String failedChildren = "";

        for (int childId : nodeRun.getThreadRun().getChildThreadIds()) {
            ThreadRunModel child = getWfRun().getThreadRun(childId);
            if (!child.isTerminated()) {
                // Can't exit yet.
                return;
            }
            if (child.status != LHStatus.COMPLETED) {
                if (!nodeRun.getThreadRun().getHandledFailedChildren().contains(childId)) {
                    allComplete = false;

                    // lolz this is silly but it works:
                    failedChildren += " " + child.number;
                }
            }
        }

        if (allComplete) {
            if (getNode().exitNode.failureDef == null) {
                // Then this is just a regular "yay we're done!" node.
                nodeRun.getThreadRun().complete(time);
                nodeRun.complete(null, time);
            } else {
                // then this is a "yikes Throw Exception" node.

                nodeRun.fail(getNode().exitNode.failureDef.getFailure(nodeRun.getThreadRun()), time);
            }
        } else {
            nodeRun.getThreadRun()
                    .fail(
                            new FailureModel(
                                    "Child thread (or threads) failed:" + failedChildren, LHConstants.CHILD_FAILURE),
                            time);
        }
    }
}

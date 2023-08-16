package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.ExitRunPb;
import io.littlehorse.sdk.common.proto.LHStatus;
import java.util.Date;

public class ExitRun extends SubNodeRun<ExitRunPb> {

    //
    private boolean alreadyNoticed;

    public ExitRun() {
        alreadyNoticed = false;
    }

    public Class<ExitRunPb> getProtoBaseClass() {
        return ExitRunPb.class;
    }

    public void initFrom(Message p) {}

    public ExitRunPb.Builder toProto() {
        return ExitRunPb.newBuilder();
    }

    public static ExitRun fromProto(ExitRunPb p) {
        ExitRun out = new ExitRun();
        out.initFrom(p);
        return out;
    }

    public boolean advanceIfPossible(Date time) {
        boolean out;
        // nothing to do
        if (nodeRunModel.isInProgress()) {
            arrive(time);
            // Return true if the status changed
            out = !(nodeRunModel.getThreadRun().isRunning());
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

        for (int childId : nodeRunModel.getThreadRun().getChildThreadIds()) {
            ThreadRunModel child = getWfRun().getThreadRunModels().get(childId);
            if (!child.isTerminated()) {
                // Can't exit yet.
                return;
            }
            if (child.status != LHStatus.COMPLETED) {
                if (
                    !nodeRunModel
                        .getThreadRun()
                        .getHandledFailedChildren()
                        .contains(childId)
                ) {
                    allComplete = false;

                    // lolz this is silly but it works:
                    failedChildren += " " + child.number;
                }
            }
        }

        if (allComplete) {
            if (getNode().exitNode.failureDef == null) {
                // Then this is just a regular "yay we're done!" node.
                nodeRunModel.getThreadRun().complete(time);
                nodeRunModel.complete(null, time);
            } else {
                // then this is a "yikes Throw Exception" node.

                nodeRunModel.fail(
                    getNode()
                        .exitNode.failureDef.getFailure(nodeRunModel.getThreadRun()),
                    time
                );
            }
        } else {
            nodeRunModel
                .getThreadRun()
                .fail(
                    new Failure(
                        "Child thread (or threads) failed:" + failedChildren,
                        LHConstants.CHILD_FAILURE
                    ),
                    time
                );
        }
    }
}

package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.proto.ExitRunPb;
import io.littlehorse.common.proto.ExitRunPbOrBuilder;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import java.util.Date;

public class ExitRun extends SubNodeRun<ExitRunPb> {

    public Class<ExitRunPb> getProtoBaseClass() {
        return ExitRunPb.class;
    }

    public void initFrom(MessageOrBuilder p) {}

    public ExitRunPb.Builder toProto() {
        return ExitRunPb.newBuilder();
    }

    public static ExitRun fromProto(ExitRunPbOrBuilder p) {
        ExitRun out = new ExitRun();
        out.initFrom(p);
        return out;
    }

    public void processEvent(WfRunEvent event) {
        // I don't believe there's anything to do here.
    }

    public void advanceIfPossible(Date time) {
        // nothing to do
    }

    public void arrive(Date time) {
        // First check all children.
        boolean allComplete = true;
        String failedChildren = "";

        for (int childId : nodeRun.threadRun.childThreadIds) {
            ThreadRun child = getWfRun().threadRuns.get(childId);
            if (!child.isTerminated()) {
                // Can't exit yet.
                return;
            }
            if (child.status != LHStatusPb.COMPLETED) {
                allComplete = false;

                // lolz this is silly but it works:
                failedChildren += " " + child.number;
            }
        }

        if (allComplete) {
            nodeRun.threadRun.complete(time);
        } else {
            nodeRun.threadRun.fail(
                TaskResultCodePb.CHILD_FALIED,
                "Child thread (or threads) failed:" + failedChildren,
                time
            );
        }
    }
}
/*
 * Things to test:
 * - Verify that the parent thread actually waits for the children.
 * - Verify that when the child thread isn't started properly, the StartThreadNode
 *   halts.
 * - Verify that when we call nodeRun.complete() from the arrive(), things don't
 *   break.
 */

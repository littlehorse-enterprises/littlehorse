package io.littlehorse.common.model.wfrun.subnoderun;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // @JsonIgnore
    private boolean alreadyNoticed;

    public ExitRun() {
        alreadyNoticed = false;
    }

    @JsonIgnore
    public Class<ExitRunPb> getProtoBaseClass() {
        return ExitRunPb.class;
    }

    public void initFrom(MessageOrBuilder p) {}

    @JsonIgnore
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

    public boolean advanceIfPossible(Date time) {
        boolean out;
        // nothing to do
        if (nodeRun.isInProgress()) {
            arrive(time);
            // Return true if the status changed
            out = !(nodeRun.threadRun.isRunning());
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

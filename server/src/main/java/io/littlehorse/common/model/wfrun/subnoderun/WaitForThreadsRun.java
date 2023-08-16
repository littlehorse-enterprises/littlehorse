package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.ThreadToWaitForModel;
import io.littlehorse.common.model.meta.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.VariableValueModel;
import io.littlehorse.common.model.wfrun.WaitForThread;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRunPb;
import io.littlehorse.sdk.common.proto.WaitForThreadsRunPb.WaitForThreadPb;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class WaitForThreadsRun extends SubNodeRun<WaitForThreadsRunPb> {

    private List<WaitForThread> threads;

    public WaitForThreadsRun() {
        this.threads = new ArrayList<>();
    }

    public Class<WaitForThreadsRunPb> getProtoBaseClass() {
        return WaitForThreadsRunPb.class;
    }

    public void initFrom(Message proto) {
        WaitForThreadsRunPb p = (WaitForThreadsRunPb) proto;
        for (WaitForThreadPb wft : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(wft, WaitForThread.class));
        }
    }

    public WaitForThreadsRunPb.Builder toProto() {
        WaitForThreadsRunPb.Builder out = WaitForThreadsRunPb.newBuilder();

        for (WaitForThread wft : threads) {
            out.addThreads(wft.toProto());
        }
        return out;
    }

    public static WaitForThreadsRun fromProto(WaitForThreadsRunPb p) {
        WaitForThreadsRun out = new WaitForThreadsRun();
        out.initFrom(p);
        return out;
    }

    // First order of business is to get the status of all threads.
    public boolean advanceIfPossible(Date time) {
        for (WaitForThread wft : threads) {
            ThreadRunModel thread = getWfRun().getThreadRun(wft.getThreadRunNumber());
            wft.setThreadStatus(thread.getStatus());
            if (thread.getEndTime() != null) {
                wft.setThreadEndTime(thread.getEndTime());
            }
        }

        boolean allTerminated = true;
        List<Integer> failedThreads = new ArrayList<>();

        for (WaitForThread wft : threads) {
            if (!isTerminated(wft)) {
                allTerminated = false;
                break;
            }

            if (wft.getThreadStatus() == LHStatus.ERROR) {
                failedThreads.add(wft.getThreadRunNumber());
            }
        }

        if (allTerminated && failedThreads.isEmpty()) {
            VariableValueModel out = new VariableValueModel();
            out.type = VariableType.NULL;
            nodeRunModel.complete(out, time);
            return true;
        } else if (allTerminated && !failedThreads.isEmpty()) {
            String message = "Some child threads failed";
            for (Integer threadRunNumber : failedThreads) {
                message += ", " + threadRunNumber;
            }
            nodeRunModel.fail(new Failure(message, LHConstants.CHILD_FAILURE), time);
            return true;
        } else {
            // nothing to do.
            log.debug("Still waiting for threads");
            return false;
        }
    }

    private boolean isTerminated(WaitForThread wft) {
        return (
            (wft.getThreadStatus() == LHStatus.COMPLETED) ||
            (wft.getThreadStatus() == LHStatus.ERROR)
        );
    }

    public void arrive(Date time) {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel wftn = getNode().getWaitForThreadsNode();
        nodeRunModel.setStatus(LHStatus.RUNNING);

        try {
            for (ThreadToWaitForModel ttwf : wftn.getThreads()) {
                threads.add(new WaitForThread(nodeRunModel, ttwf));
            }
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                new Failure(
                    "Failed determining thread run number to wait for: " +
                    exn.getMessage(),
                    LHConstants.VAR_SUB_ERROR
                ),
                time
            );
        }
    }
}

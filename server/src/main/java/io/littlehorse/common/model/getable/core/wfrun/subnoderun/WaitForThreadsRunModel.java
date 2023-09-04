package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.common.model.getable.global.wfspec.node.ThreadToWaitForModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class WaitForThreadsRunModel extends SubNodeRun<WaitForThreadsRun> {

    private List<WaitForThreadModel> threads;

    public WaitForThreadsRunModel() {
        this.threads = new ArrayList<>();
    }

    public Class<WaitForThreadsRun> getProtoBaseClass() {
        return WaitForThreadsRun.class;
    }

    public void initFrom(Message proto) {
        WaitForThreadsRun p = (WaitForThreadsRun) proto;
        for (WaitForThreadsRun.WaitForThread wft : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(wft, WaitForThreadModel.class));
        }
    }

    public WaitForThreadsRun.Builder toProto() {
        WaitForThreadsRun.Builder out = WaitForThreadsRun.newBuilder();

        for (WaitForThreadModel wft : threads) {
            out.addThreads(wft.toProto());
        }
        return out;
    }

    public static WaitForThreadsRunModel fromProto(WaitForThreadsRun p) {
        WaitForThreadsRunModel out = new WaitForThreadsRunModel();
        out.initFrom(p);
        return out;
    }

    @Override
    public boolean canBeInterrupted() {
        return true;
    }

    // First order of business is to get the status of all threads.
    /*public boolean advanceIfPossible(Date time) {
        for (WaitForThreadModel wft : threads) {
            if (wft.isAlreadyHandled()) {
                continue;
            }
            ThreadRunModel childThread = getWfRun().getThreadRun(wft.getThreadRunNumber());
            wft.setThreadStatus(childThread.getStatus());
            if (childThread.getStatus() == LHStatus.EXCEPTION) {
                NodeRunModel nodeRun = childThread.getNodeRun(childThread.getCurrentNodePosition());
                FailureModel latestFailure = nodeRun.getLatestFailure();
                nodeRunModel.fail(latestFailure, time);
            }
            if (childThread.getEndTime() != null) {
                wft.setAlreadyHandled(true);
                wft.setThreadEndTime(childThread.getEndTime());
            }
        }

        // boolean allTerminated = true;
        for (WaitForThreadModel thread : threads) {
            if (!isTerminated(thread)) {
                log.debug("Still waiting for threads");
                return false;
            }
        }

        Predicate<WaitForThreadModel> isFailedWaitForThread =
                waitForThreadModel -> waitForThreadModel.getThreadStatus() == LHStatus.ERROR
                        || waitForThreadModel.getThreadStatus() == LHStatus.EXCEPTION;

        List<Integer> failedThreads = threads.stream()
                .filter(isFailedWaitForThread)
                .map(WaitForThreadModel::getThreadRunNumber)
                .toList();

        if (failedThreads.isEmpty()) {
            VariableValueModel out = new VariableValueModel();
            out.type = VariableType.NULL;
            nodeRunModel.complete(out, time);
            return true;
        } else {
            String message = "Some child threads failed";
            for (Integer threadRunNumber : failedThreads) {
                message += ", " + threadRunNumber;
            }
            nodeRunModel.fail(new FailureModel(message, LHConstants.CHILD_FAILURE), time);
            return true;
        }
    }*/

    @Override
    public boolean advanceIfPossible(Date time) {
        WfRunModel wfRun = getWfRun();
        boolean allThreadsCompleted = true;
        for (WaitForThreadModel waitingThread : threads) {
            ThreadRunModel threadRun = wfRun.getThreadRun(waitingThread.getThreadRunNumber());
            waitingThread.setThreadStatus(threadRun.getStatus());
            if (waitingThread.getThreadStatus() == LHStatus.EXCEPTION && !waitingThread.isAlreadyHandled()) {
                FailureModel latestFailure =
                        threadRun.getNodeRun(threadRun.getCurrentNodePosition()).getLatestFailure();
                nodeRunModel.fail(latestFailure, time);
            }
            if (threadRun.isTerminated()) {
                waitingThread.setAlreadyHandled(true);
                waitingThread.setThreadEndTime(time);
            } else {
                allThreadsCompleted = false;
            }
        }
        if (allThreadsCompleted) {
            nodeRunModel.complete(new VariableValueModel(), time);
        }
        return allThreadsCompleted;
    }

    private boolean isTerminated(WaitForThreadModel wft) {
        return wft.getThreadStatus() == LHStatus.COMPLETED
                || wft.getThreadStatus() == LHStatus.ERROR
                || wft.getThreadStatus() == LHStatus.EXCEPTION;
    }

    public void arrive(Date time) {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel wftn = getNode().getWaitForThreadsNode();
        nodeRunModel.setStatus(LHStatus.RUNNING);

        try {
            for (ThreadToWaitForModel ttwf : wftn.getThreads()) {
                threads.add(new WaitForThreadModel(nodeRunModel, ttwf));
            }
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                    new FailureModel(
                            "Failed determining thread run number to wait for: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
        }
    }
}

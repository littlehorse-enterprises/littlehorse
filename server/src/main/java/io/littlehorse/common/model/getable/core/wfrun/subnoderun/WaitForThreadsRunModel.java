package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitForThreadsRunModel extends SubNodeRun<WaitForThreadsRun> {

    private List<WaitForThreadModel> threads;
    private WaitForThreadsPolicy policy;

    public WaitForThreadsRunModel() {
        this.threads = new ArrayList<>();
    }

    public Class<WaitForThreadsRun> getProtoBaseClass() {
        return WaitForThreadsRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WaitForThreadsRun p = (WaitForThreadsRun) proto;
        for (WaitForThreadsRun.WaitForThread wft : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(wft, WaitForThreadModel.class, context));
        }
        policy = p.getPolicy();
    }

    public WaitForThreadsRun.Builder toProto() {
        WaitForThreadsRun.Builder out = WaitForThreadsRun.newBuilder();

        for (WaitForThreadModel wft : threads) {
            out.addThreads(wft.toProto());
        }
        out.setPolicy(policy);
        return out;
    }

    public static WaitForThreadsRunModel fromProto(WaitForThreadsRun p, ExecutionContext context) {
        WaitForThreadsRunModel out = new WaitForThreadsRunModel();
        out.initFrom(p, context);
        return out;
    }

    @Override
    public boolean canBeInterrupted() {
        return true;
    }

    @Override
    public boolean advanceIfPossible(Date time) {
        NodeTerminationManager nodeTerminationManager = new NodeTerminationManager(threads, nodeRunModel);
        return nodeTerminationManager.completeIfPossible(time);
    }

    public void arrive(Date time) {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel wftn = getNode().getWaitForThreadsNode();
        nodeRunModel.setStatus(LHStatus.RUNNING);
        try {
            threads.addAll(wftn.getThreadsToWaitFor(nodeRunModel));
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                    new FailureModel(
                            "Failed determining thread run number to wait for: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
        }
    }

    static final class NodeTerminationManager {
        private final List<WaitForThreadModel> waitingThreads;
        private final NodeRunModel waitForThreadsNodeRun;
        private final WfRunModel wfRun;

        NodeTerminationManager(List<WaitForThreadModel> waitingThreads, NodeRunModel waitForThreadsNodeRun) {
            this.waitingThreads = waitingThreads;
            this.waitForThreadsNodeRun = waitForThreadsNodeRun;
            this.wfRun = waitForThreadsNodeRun.getThreadRun().getWfRun();
        }

        /**
         * Verify if WaitForThreads Node should complete or fail.
         * @param time WfRun execution time
         * @return true if node should advance
         */
        public boolean completeIfPossible(Date time) {
            WaitForThreadModel failedWaitingThread = getFailedWaitingThread(time);
            if (failedWaitingThread != null) {
                handleWaitingThreadFailure(failedWaitingThread, time);
                // node fails, so advance
                return true;
            }
            if (areWaitingThreadsTerminated()) {
                waitForThreadsNodeRun.complete(new VariableValueModel(), time);
            }
            return false;
        }

        private void handleWaitingThreadFailure(WaitForThreadModel failedWaitingThread, Date time) {
            ThreadRunModel threadRun = wfRun.getThreadRun(failedWaitingThread.getThreadRunNumber());
            FailureModel latestFailure =
                    threadRun.getNodeRun(threadRun.getCurrentNodePosition()).getLatestFailure();
            if (latestFailure.isUserDefinedFailure()) {
                propagateFailure(latestFailure, time);
            } else {
                String failureMessage =
                        "Some child threads failed = [%s]".formatted(failedWaitingThread.getThreadRunNumber());
                FailureModel childFailure = new FailureModel(failureMessage, LHConstants.CHILD_FAILURE);
                propagateFailure(childFailure, time);
            }
            updateWaitingThreadStatuses(failedWaitingThread);
        }

        private WaitForThreadModel getFailedWaitingThread(Date time) {
            for (WaitForThreadModel waitingThread : waitingThreads) {
                ThreadRunModel threadRun = wfRun.getThreadRun(waitingThread.getThreadRunNumber());
                waitingThread.setThreadStatus(threadRun.getStatus());
                if (waitingThread.isFailed() && !waitingThread.isAlreadyHandled()) {
                    waitingThread.setAlreadyHandled(true);
                    waitingThread.setThreadEndTime(time);
                    return waitingThread;
                }
            }
            return null;
        }

        private boolean areWaitingThreadsTerminated() {
            return waitingThreads.stream()
                    .map(WaitForThreadModel::getThreadStatus)
                    .allMatch(this::isTerminated);
        }

        private boolean isTerminated(LHStatus threadStatus) {
            return threadStatus == LHStatus.COMPLETED
                    || threadStatus == LHStatus.ERROR
                    || threadStatus == LHStatus.EXCEPTION;
        }

        private void propagateFailure(FailureModel failureToPropagate, Date time) {
            waitForThreadsNodeRun.fail(failureToPropagate, time);
        }

        private void updateWaitingThreadStatuses(WaitForThreadModel failedWaitingThread) {
            for (WaitForThreadModel waitingThread : waitingThreads) {
                if (!Objects.equals(failedWaitingThread, waitingThread)) {
                    ThreadRunModel waitingThreadRun = wfRun.getThreadRun(waitingThread.getThreadRunNumber());
                    waitingThread.setThreadStatus(waitingThreadRun.getStatus());
                }
            }
        }
    }
}

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
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class WaitForThreadsRunModel extends SubNodeRun<WaitForThreadsRun> {

    private List<WaitForThreadModel> threads;
    private WaitForThreadsPolicy policy;
    private VariableAssignmentModel threadList;

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

    public static WaitForThreadsRunModel fromProto(WaitForThreadsRun p) {
        WaitForThreadsRunModel out = new WaitForThreadsRunModel();
        out.initFrom(p);
        return out;
    }

    @Override
    public boolean canBeInterrupted() {
        return true;
    }

    @Override
    public boolean advanceIfPossible(Date time) {
        WfRunModel wfRun = getWfRun();
        boolean allThreadsCompleted = true;

        for (WaitForThreadModel waitingThread : threads) {
            ThreadRunModel threadRun = wfRun.getThreadRun(waitingThread.getThreadRunNumber());
            waitingThread.setThreadStatus(threadRun.getStatus());

            if (waitingThread.isFailed() && !waitingThread.isAlreadyHandled()) {
                log.trace("Detected new failure on ThreadRun {} {}", threadRun.getWfRunId(), threadRun.getNumber());

                FailureModel latestFailure =
                        threadRun.getNodeRun(threadRun.getCurrentNodePosition()).getLatestFailure();
                if (isFailFast()) {
                    log.trace("Doing fast failure!");

                    if (latestFailure.isUserDefinedFailure()) {
                        doFailFast(waitingThread, latestFailure, time);
                    } else {
                        String failureMessage =
                                "Some child threads failed = [%s]".formatted(waitingThread.getThreadRunNumber());
                        FailureModel failure = new FailureModel(failureMessage, LHConstants.CHILD_FAILURE);
                        doFailFast(waitingThread, failure, time);
                    }
                    return true;
                } else if (latestFailure.isUserDefinedFailure()) {

                    log.debug("There is an uncaught user failure, we are causing the NodeRun to fail.");
                    nodeRunModel.fail(latestFailure, time);
                }
            }
            if (threadRun.isTerminated()) {
                waitingThread.setAlreadyHandled(true);
                waitingThread.setThreadEndTime(time);
            } else {
                allThreadsCompleted = false;
            }
        }
        if (allThreadsCompleted) {
            String failedThreadRunNumbers = threads.stream()
                    .filter(waitForThreadModel -> waitForThreadModel.getThreadStatus() == LHStatus.ERROR)
                    .map(WaitForThreadModel::getThreadRunNumber)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            if (failedThreadRunNumbers.isEmpty()) {
                nodeRunModel.complete(new VariableValueModel(), time);
            } else {
                String failureMessage = "Some child threads failed = [%s]".formatted(failedThreadRunNumbers);
                nodeRunModel.fail(new FailureModel(failureMessage, LHConstants.CHILD_FAILURE), time);
            }
        }
        return allThreadsCompleted;
    }

    private void maybeInitializeDynamicThreads(Date time) {
        if (threadList != null) {
            try {
                VariableValueModel variableValueModel =
                        nodeRunModel.getThreadRun().assignVariable(threadList);

                List<WaitForThreadModel> waitForThreads = variableValueModel.getJsonArrVal().stream()
                        .map(Object::toString)
                        .map(Integer::valueOf)
                        .map(threadRunNumber -> this.createWaitForThreadModel(threadRunNumber, time))
                        .filter(Objects::nonNull)
                        .toList();
                threads.addAll(waitForThreads);
            } catch (LHVarSubError exn) {
                nodeRunModel.fail(
                        new FailureModel(
                                "Failed determining thread run number to wait for: " + exn.getMessage(),
                                LHConstants.VAR_SUB_ERROR),
                        time);
            }
        }
    }

    private WaitForThreadModel createWaitForThreadModel(int threadRunNumber, Date time) {
        try {
            return new WaitForThreadModel(nodeRunModel, threadRunNumber);
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                    new FailureModel(
                            "Failed determining thread run number to wait for: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
        }
        return null;
    }

    private void doFailFast(WaitForThreadModel failedWaitingThread, FailureModel failure, Date time) {
        nodeRunModel.fail(failure, time);
        WfRunModel wfRun = getWfRun();
        for (WaitForThreadModel waitingThreads : threads) {
            if (!Objects.equals(failedWaitingThread, waitingThreads)) {
                ThreadRunModel threadRun = wfRun.getThreadRun(waitingThreads.getThreadRunNumber());
                waitingThreads.setThreadStatus(threadRun.getStatus());
            }
        }
    }

    private boolean isFailFast() {
        return policy == WaitForThreadsPolicy.STOP_ON_FAILURE;
    }

    public void arrive(Date time) {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel wftn = getNode().getWaitForThreadsNode();
        nodeRunModel.setStatus(LHStatus.RUNNING);
        ThreadRunModel threadRun = nodeRunModel.getThreadRun();
        try {
            for (ThreadToWaitForModel ttwf : wftn.getThreads()) {
                int threadRunNumber = threadRun
                        .assignVariable(ttwf.getThreadRunNumber())
                        .asInt()
                        .intVal
                        .intValue();
                threads.add(new WaitForThreadModel(nodeRunModel, threadRunNumber));
            }
            maybeInitializeDynamicThreads(time);
        } catch (LHVarSubError exn) {
            nodeRunModel.fail(
                    new FailureModel(
                            "Failed determining thread run number to wait for: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
        }
    }
}

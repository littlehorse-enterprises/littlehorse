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
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitingThreadStatus;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class WaitForThreadsRunModel extends SubNodeRun<WaitForThreadsRun> {

    private List<WaitForThreadModel> threads = new ArrayList<>();
    private ProcessorExecutionContext context;

    public WaitForThreadsRunModel() {}

    public WaitForThreadsRunModel(ProcessorExecutionContext context) {
        this.context = context;
    }

    public Class<WaitForThreadsRun> getProtoBaseClass() {
        return WaitForThreadsRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        this.context = context.castOnSupport(ProcessorExecutionContext.class);
        WaitForThreadsRun p = (WaitForThreadsRun) proto;
        for (WaitForThreadsRun.WaitForThread wft : p.getThreadsList()) {
            threads.add(LHSerializable.fromProto(wft, WaitForThreadModel.class, context));
        }
    }

    public WaitForThreadsRun.Builder toProto() {
        WaitForThreadsRun.Builder out = WaitForThreadsRun.newBuilder();

        for (WaitForThreadModel wft : threads) {
            out.addThreads(wft.toProto());
        }
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
        NodeTerminationManager nodeTerminationManager = new NodeTerminationManager(threads, nodeRun, context);
        return nodeTerminationManager.advanceIfPossible(time);
    }

    @Override
    public void arrive(Date time) {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel wftn = getNode().getWaitForThreadsNode();
        nodeRun.setStatus(LHStatus.RUNNING);
        try {
            threads.addAll(wftn.getThreadsToWaitFor(nodeRun, time));
        } catch (LHVarSubError exn) {
            nodeRun.fail(
                    new FailureModel(
                            "Failed determining thread run number to wait for: " + exn.getMessage(),
                            LHConstants.VAR_SUB_ERROR),
                    time);
        }
    }

    static final class NodeTerminationManager {
        private final List<WaitForThreadModel> childrenToWaitFor;
        private final NodeRunModel waitForThreadsNodeRun;
        private final WfRunModel wfRun;
        private final ProcessorExecutionContext context;

        NodeTerminationManager(
                List<WaitForThreadModel> waitingThreads,
                NodeRunModel waitForThreadsNodeRun,
                ProcessorExecutionContext context) {
            this.childrenToWaitFor = waitingThreads;
            this.waitForThreadsNodeRun = waitForThreadsNodeRun;
            this.wfRun = waitForThreadsNodeRun.getThreadRun().getWfRun();
            this.context = context;
        }

        /**
         * Verify if WaitForThreads Node should complete or fail.
         * @param time WfRun execution time
         * @return true if something changed (only God knows how WfRunModel#advance() works)
         */
        public boolean advanceIfPossible(Date time) {
            WaitForThreadModel failedChildThread = getFailedChildThreadIfExists(time);
            if (failedChildThread != null) {
                log.trace("Detected child thread failure {}", failedChildThread);
                handleChildThreadFailure(failedChildThread, time);
                return true;
            }
            if (areChildThreadsTerminated()) {
                waitForThreadsNodeRun.complete(new VariableValueModel(), time);
            }
            return false;
        }

        private void handleChildThreadFailure(WaitForThreadModel failedWaitingThread, Date time) {
            ThreadRunModel threadRun = wfRun.getThreadRun(failedWaitingThread.getThreadRunNumber());
            FailureModel latestFailure = threadRun.getCurrentNodeRun().getLatestFailure();

            FailureHandlerDefModel handler = getHandlerFor(latestFailure);
            if (handler != null) {
                startFailureHandlerFor(handler, latestFailure, threadRun, failedWaitingThread);
            } else if (latestFailure.isUserDefinedFailure()) {
                propagateFailure(failedWaitingThread, latestFailure, time);
            } else {
                String failureMessage =
                        "Some child threads failed = [%s]".formatted(failedWaitingThread.getThreadRunNumber());
                FailureModel childFailure = new FailureModel(failureMessage, LHConstants.CHILD_FAILURE);
                propagateFailure(failedWaitingThread, childFailure, time);
            }
            updateWaitingThreadStatuses();
        }

        /*
         * If the associated WaitForThreadsRun for this WaitForThreadsRunModel is waiting for a ThreadRun
         * that has *already* failed, it returns the first one that is failed.
         */
        private WaitForThreadModel getFailedChildThreadIfExists(Date time) {
            for (WaitForThreadModel childThread : childrenToWaitFor) {
                ThreadRunModel childThreadRun = wfRun.getThreadRun(childThread.getThreadRunNumber());
                childThread.setThreadStatus(childThreadRun.getStatus());

                // The Waiting Thread starts out in the THREAD_IN_PROGRESS status.
                if (childThread.isFailed()
                        && (childThread.getWaitingStatus() == WaitingThreadStatus.THREAD_IN_PROGRESS)) {
                    childThread.setThreadEndTime(time);
                    return childThread;
                }
            }
            return null;
        }

        private void startFailureHandlerFor(
                FailureHandlerDefModel failureHandlerDef,
                FailureModel failure,
                ThreadRunModel failedChildThreadRun,
                WaitForThreadModel failedChildThread) {
            /*
             * The logic for the ThreadRunModel starting a FailureHandler is more complex
             * than the logic for the WaitForThreadsRunModel starting a FailureHandler.
             *
             * This is because of two things:
             * 1. In this class, we know the failed ThreadRun is already terminated, so its
             *    children are also terminated.
             * 2. We don't need to resurrect the failed ThreadRun after the Failure Handler
             *    completes; so we don't need to play with the ThreadHaltReasons.
             */

            log.trace("Starting failure handler for failed child threadrun {}", failedChildThread);

            failedChildThread.setWaitingStatus(WaitingThreadStatus.THREAD_HANDLING_FAILURE);

            WfSpecModel wfSpec = context.service().getWfSpec(waitForThreadsNodeRun.getWfSpecId());
            ThreadSpecModel handlerSpec = wfSpec.threadSpecs.get(failureHandlerDef.getHandlerSpecName());
            Map<String, VariableValueModel> vars = new HashMap<>();
            if (handlerSpec.variableDefs.size() > 0) {
                vars.put(WorkflowThread.HANDLER_INPUT_VAR, failure.getContent());
            }
            ThreadRunModel failureHandler = wfRun.startThread(
                    failureHandlerDef.getHandlerSpecName(),
                    new Date(),
                    failedChildThreadRun.getNumber(),
                    vars,
                    ThreadType.FAILURE_HANDLER);
            failedChildThread.setFailureHandlerThreadRunId(failureHandler.getNumber());
        }

        /*
         * The `WaitForThreadsNode` has a `repeated FailureHandlerDef per_thread_failure_handlers` field,
         * which defines `FailureHandler`s to run in case a `ThreadRun` that we are waiting for is failed.
         *
         * This function returns the first matching FailureHandlerDef if any exists; else it returns null.
         */
        private FailureHandlerDefModel getHandlerFor(FailureModel failure) {
            for (FailureHandlerDefModel handler :
                    waitForThreadsNodeRun.getNode().getWaitForThreadsNode().getPerThreadFailureHandlers()) {
                if (handler.doesHandle(failure.getFailureName())) {
                    return handler;
                }
            }
            return null;
        }

        private boolean areChildThreadsTerminated() {
            updateWaitingThreadStatuses();
            return childrenToWaitFor.stream()
                    .allMatch(childThread ->
                            childThread.getWaitingStatus() == WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);
        }

        private void propagateFailure(
                WaitForThreadModel failedWaitingThread, FailureModel failureToPropagate, Date time) {
            failedWaitingThread.setWaitingStatus(WaitingThreadStatus.THREAD_UNSUCCESSFUL);
            waitForThreadsNodeRun.fail(failureToPropagate, time);
        }

        private void updateWaitingThreadStatuses() {
            for (WaitForThreadModel childThread : childrenToWaitFor) {
                ThreadRunModel childThreadRun = wfRun.getThreadRun(childThread.getThreadRunNumber());
                childThread.setThreadStatus(childThreadRun.getStatus());

                if (childThreadRun.getStatus() == LHStatus.COMPLETED) {
                    childThread.setWaitingStatus(WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);

                } else if (childThread.getWaitingStatus() == WaitingThreadStatus.THREAD_HANDLING_FAILURE) {
                    // Get the failure handler.
                    Integer failureHandlerId = childThread.getFailureHandlerThreadRunId();

                    if (failureHandlerId == null) {
                        log.trace("still waiting for failure handler thread to start on waitforthreadsnode");
                        continue;
                    }

                    ThreadRunModel failureHandlerThread = wfRun.getThreadRun(failureHandlerId);
                    if (failureHandlerThread.getStatus() == LHStatus.COMPLETED) {
                        childThread.setWaitingStatus(WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);
                        // TODO: this is duplicate logic from ThreadRunModel. We need to note that the
                        // child ThreadRun has already been handled.
                        waitForThreadsNodeRun
                                .getThreadRun()
                                .getHandledFailedChildren()
                                .add(childThread.getThreadRunNumber());

                    } else if (failureHandlerThread.getStatus() == LHStatus.EXCEPTION) {
                        propagateFailure(
                                childThread,
                                failureHandlerThread.getCurrentNodeRun().getLatestFailure(),
                                new Date());
                    } else if (failureHandlerThread.getStatus() == LHStatus.ERROR) {
                        String failureMessage =
                                "Failure handler thread failed: [%s]".formatted(failureHandlerThread.getNumber());
                        FailureModel childFailure = new FailureModel(failureMessage, LHConstants.CHILD_FAILURE);
                        propagateFailure(childThread, childFailure, new Date());
                    }
                }
            }
        }
    }
}

package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.WaitForThreadsNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitingThreadStatus;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class WaitForThreadsRunModel extends SubNodeRun<WaitForThreadsRun> {

    private List<WaitForThreadModel> threads = new ArrayList<>();

    public WaitForThreadsRunModel() {}

    @Override
    public Class<WaitForThreadsRun> getProtoBaseClass() {
        return WaitForThreadsRun.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WaitForThreadsRun p = (WaitForThreadsRun) proto;
        for (WaitForThreadsRun.WaitForThread wft : p.getThreadsList()) {
            WaitForThreadModel wftm = LHSerializable.fromProto(wft, WaitForThreadModel.class, context);
            wftm.setNodeRun(getNodeRun());
            threads.add(wftm);
        }
    }

    @Override
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
    public boolean maybeHalt(CoreProcessorContext processorContext) {
        return true;
    }

    @Override
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        boolean allCompleteOrHandled = true;

        // This is stupid but it's required to make things work.
        threads.stream().forEach(child -> child.setNodeRun(nodeRun));

        for (WaitForThreadModel childThread : threads) {
            updateStatusOfAndMaybeThrow(childThread, processorContext);

            allCompleteOrHandled = allCompleteOrHandled
                    && childThread.getWaitingStatus() == WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED;
        }

        return allCompleteOrHandled;
    }

    @Override
    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel node = getNode().getWaitForThreadsNode();
        nodeRun.setStatus(LHStatus.RUNNING);
        try {
            threads.addAll(node.getThreadsToWaitFor(nodeRun, time, processorContext));
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed determining thread run number to wait for: " + exn.getMessage(),
                    LHErrorType.VAR_SUB_ERROR.toString()));
        }
    }

    @Override
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        // Currently, the WaitForThreads node does not have output. This is because we haven't yet implemented
        // ThreadRun outputs.
        return Optional.empty();
    }

    /**
     * Updates the metadata in the WaitForThreadModel for a specific child thread that we're waiting on.
     * @param child is the child we're looking at.
     * @throws NodeFailureException if the child failed with an unrecoverable error.
     */
    private void updateStatusOfAndMaybeThrow(WaitForThreadModel child, CoreProcessorContext processorContext)
            throws NodeFailureException {
        if (child.getWaitingStatus() == WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED) {
            return;
        }

        WfRunModel wfRun = getWfRun();
        ThreadRunModel childThreadRun = wfRun.getThreadRun(child.getThreadRunNumber());
        child.setThreadStatus(childThreadRun.getStatus());

        if (child.getWaitingStatus() == WaitingThreadStatus.THREAD_IN_PROGRESS) {

            // need to look at the waiting thread.
            if (childThreadRun.getStatus() == LHStatus.COMPLETED) {
                child.setWaitingStatus(WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);

            } else if (childThreadRun.getStatus() == LHStatus.EXCEPTION
                    || childThreadRun.getStatus() == LHStatus.ERROR) {
                FailureModel failureFromInterrupt = null;
                if (!childThreadRun.getHaltReasons().isEmpty()) {
                    for (ThreadHaltReasonModel haltReason : childThreadRun.getHaltReasons()) {
                        if (haltReason.getInterrupted() != null) {
                            ThreadRunModel threadRun =
                                    wfRun.getThreadRun(haltReason.getInterrupted().interruptThreadId);
                            failureFromInterrupt = threadRun
                                    .getCurrentNodeRun()
                                    .getLatestFailure()
                                    .get();
                            break;
                        }
                    }
                }
                FailureModel latestFailure = failureFromInterrupt != null
                        ? failureFromInterrupt
                        : childThreadRun.getCurrentNodeRun().getLatestFailure().get();

                FailureHandlerDefModel handler = getHandlerFor(latestFailure);
                if (handler != null) {
                    child.setWaitingStatus(WaitingThreadStatus.THREAD_HANDLING_FAILURE);
                    startFailureHandlerFor(handler, latestFailure, childThreadRun, child, processorContext);
                } else {
                    child.setWaitingStatus(WaitingThreadStatus.THREAD_UNSUCCESSFUL);
                    propagateFailure(latestFailure, child);
                }
            }
        } else if (child.getWaitingStatus() == WaitingThreadStatus.THREAD_HANDLING_FAILURE) {
            ThreadRunModel failureHandler = wfRun.getThreadRun(child.getFailureHandlerThreadRunId());

            if (failureHandler.getStatus() == LHStatus.COMPLETED) {
                // The failure handler succeeded, so we count this thread as "properly handled"
                child.setWaitingStatus(WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED);
                nodeRun.getThreadRun().getHandledFailedChildren().add(child.getThreadRunNumber());

            } else if (failureHandler.getStatus() == LHStatus.EXCEPTION
                    || failureHandler.getStatus() == LHStatus.ERROR) {

                child.setWaitingStatus(WaitingThreadStatus.THREAD_UNSUCCESSFUL);
                // Here, the failure handler failed, so we propagate the failure anyways.
                FailureModel lastFailure =
                        failureHandler.getCurrentNodeRun().getLatestFailure().get();
                propagateFailure(lastFailure, child);
            }
        }
    }

    private void propagateFailure(FailureModel failure, WaitForThreadModel failedChild) throws NodeFailureException {
        if (failure.isUserDefinedFailure()) {
            throw new NodeFailureException(failure);
        } else {
            FailureModel toThrow = new FailureModel(
                    "Child thread %d failed with %s: %s"
                            .formatted(
                                    failedChild.getThreadRunNumber(), failure.getFailureName(), failure.getMessage()),
                    LHErrorType.CHILD_FAILURE.toString());
            throw new NodeFailureException(toThrow);
        }
    }

    private FailureHandlerDefModel getHandlerFor(FailureModel failure) {
        for (FailureHandlerDefModel handler :
                nodeRun.getNode().getWaitForThreadsNode().getPerThreadFailureHandlers()) {
            if (handler.doesHandle(failure.getFailureName())) {
                return handler;
            }
        }
        return null;
    }

    private void startFailureHandlerFor(
            FailureHandlerDefModel failureHandlerDef,
            FailureModel failure,
            ThreadRunModel failedChildThreadRun,
            WaitForThreadModel failedChildThread,
            CoreProcessorContext processorContext)
            throws NodeFailureException {
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

        WfSpecModel wfSpec = processorContext.service().getWfSpec(nodeRun.getWfSpecId());
        ThreadSpecModel handlerSpec = wfSpec.threadSpecs.get(failureHandlerDef.getHandlerSpecName());
        Map<String, VariableValueModel> vars = new HashMap<>();
        if (handlerSpec.variableDefs.size() > 0) {
            vars.put(WorkflowThread.HANDLER_INPUT_VAR, failure.getContent());
        }

        // If this fails, then it means that we have an invalid WfSpec.
        try {
            handlerSpec.validateStartVariables(vars, processorContext.metadataManager());
        } catch (LHValidationException exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failure Handler spec %s for thread %d rejected input variables: %s"
                            .formatted(handlerSpec.getName(), failedChildThread.getThreadRunNumber(), exn.getMessage()),
                    LHErrorType.CHILD_FAILURE.toString()));
        }

        // At this point, we can just start the ThreadRun.
        ThreadRunModel failureHandler = getWfRun()
                .startThread(
                        failureHandlerDef.getHandlerSpecName(),
                        new Date(),
                        failedChildThreadRun.getNumber(),
                        vars,
                        ThreadType.FAILURE_HANDLER);
        failedChildThread.setFailureHandlerThreadRunId(failureHandler.getNumber());
    }
}

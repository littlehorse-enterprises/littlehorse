package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
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
import io.littlehorse.sdk.common.proto.LHErrorType;
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
import java.util.Optional;
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

    @Override
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
    public boolean maybeHalt() {
        return true;
    }

    @Override
    public boolean checkIfProcessingCompleted() throws NodeFailureException {
        boolean allCompleteOrHandled = true;
        for (WaitForThreadModel childThread : threads) {
            updateStatusOfAndMaybeThrow(childThread);

            allCompleteOrHandled = allCompleteOrHandled
                    && childThread.getWaitingStatus() == WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED;
        }

        return allCompleteOrHandled;
    }

    @Override
    public void arrive(Date time) throws NodeFailureException {
        // Need to initialize all of the threads.
        WaitForThreadsNodeModel node = getNode().getWaitForThreadsNode();
        nodeRun.setStatus(LHStatus.RUNNING);
        try {
            threads.addAll(node.getThreadsToWaitFor(nodeRun, time, context));
        } catch (LHVarSubError exn) {
            throw new NodeFailureException(new FailureModel(
                    "Failed determining thread run number to wait for: " + exn.getMessage(),
                    LHErrorType.VAR_SUB_ERROR.toString()));
        }
    }

    @Override
    public Optional<VariableValueModel> getOutput() {
        // Currently, the WaitForThreads node does not have output. This is because we haven't yet implemented
        // ThreadRun outputs.
        return Optional.empty();
    }

    /**
     * Updates the metadata in the WaitForThreadModel for a specific child thread that we're waiting on.
     * @param child is the child we're looking at.
     * @throws NodeFailureException if the child failed with an unrecoverable error.
     */
    private void updateStatusOfAndMaybeThrow(WaitForThreadModel child) throws NodeFailureException {
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

                FailureModel latestFailure =
                        childThreadRun.getCurrentNodeRun().getLatestFailure().get();

                FailureHandlerDefModel handler = getHandlerFor(latestFailure);
                if (handler != null) {
                    child.setWaitingStatus(WaitingThreadStatus.THREAD_HANDLING_FAILURE);
                    startFailureHandlerFor(handler, latestFailure, childThreadRun, child);
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
            WaitForThreadModel failedChildThread)
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

        WfSpecModel wfSpec = context.service().getWfSpec(nodeRun.getWfSpecId());
        ThreadSpecModel handlerSpec = wfSpec.threadSpecs.get(failureHandlerDef.getHandlerSpecName());
        Map<String, VariableValueModel> vars = new HashMap<>();
        if (handlerSpec.variableDefs.size() > 0) {
            vars.put(WorkflowThread.HANDLER_INPUT_VAR, failure.getContent());
        }

        // If this fails, then it means that we have an invalid WfSpec.
        try {
            handlerSpec.validateStartVariables(vars);
        } catch (LHValidationError exn) {
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

/*
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

       public boolean advanceIfPossible() {
           WaitForThreadModel failedChildThread = getFailedChildThreadIfExists(time);
           if (failedChildThread != null) {
               log.trace("Detected child thread failure {}", failedChildThread);
               handleChildThreadFailure(failedChildThread, time);
               return true;
           }

           return areChildThreadsTerminated();
           if (areChildThreadsTerminated()) {
               waitForThreadsNodeRun.complete(new VariableValueModel(), time);
           }
           return false;
       }

       private void handleChildThreadFailure(WaitForThreadModel failedWaitingThread, Date time) {
           ThreadRunModel threadRun = wfRun.getThreadRun(failedWaitingThread.getThreadRunNumber());
           Optional<FailureModel> latestFailureOption =
                   threadRun.getCurrentNodeRun().getLatestFailure();
           if (latestFailureOption.isEmpty()) {
               throw new IllegalStateException(
                       "called handleChildThreadFailure on non-failed ThreadRun, or it failed without saving failure!");
           }
           FailureModel latestFailure = latestFailureOption.get();

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

*/

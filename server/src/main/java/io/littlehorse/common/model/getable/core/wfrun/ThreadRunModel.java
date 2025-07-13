package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.ThreadRunRescueFailedException;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureBeingHandledModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.PendingFailureHandlerModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.HandlingFailureHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.InterruptedModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.ParentHaltedModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.PendingFailureHandlerHaltReasonModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.PendingInterruptHaltReasonModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.InterruptDefModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.expression.ExpressionModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class ThreadRunModel extends LHSerializable<ThreadRun> {

    private WfSpecIdModel wfSpecId;
    public int number;

    public LHStatus status;
    public String threadSpecName;
    public int currentNodePosition;

    public Date startTime;
    public Date endTime;

    public String errorMessage;

    public List<Integer> childThreadIds = new ArrayList<>();
    public Integer parentThreadId;

    public List<ThreadHaltReasonModel> haltReasons = new ArrayList<>();
    public ExternalEventIdModel interruptTriggerId;
    public FailureBeingHandledModel failureBeingHandled;
    public List<Integer> handledFailedChildren = new ArrayList<>();

    public ThreadType type;

    private ExecutionContext executionContext;
    // Only contains value in Processor execution context.
    private CoreProcessorContext processorContext;

    public ThreadRunModel() {}

    public ThreadRunModel(CoreProcessorContext processorContext) {
        this.executionContext = processorContext;
        this.processorContext = processorContext;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        ThreadRun proto = (ThreadRun) p;
        number = proto.getNumber();
        status = proto.getStatus();
        threadSpecName = proto.getThreadSpecName();
        currentNodePosition = proto.getCurrentNodePosition();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());
        wfSpecId = LHSerializable.fromProto(proto.getWfSpecId(), WfSpecIdModel.class, executionContext);
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
        if (proto.hasErrorMessage()) {
            errorMessage = proto.getErrorMessage();
        }
        if (proto.hasParentThreadId()) parentThreadId = proto.getParentThreadId();
        for (Integer childId : proto.getChildThreadIdsList()) {
            childThreadIds.add(childId);
        }

        if (proto.hasInterruptTriggerId()) {
            interruptTriggerId =
                    LHSerializable.fromProto(proto.getInterruptTriggerId(), ExternalEventIdModel.class, context);
        }

        for (ThreadHaltReason thrpb : proto.getHaltReasonsList()) {
            ThreadHaltReasonModel thr = ThreadHaltReasonModel.fromProto(thrpb, context);
            thr.threadRun = this;
            haltReasons.add(thr);
        }

        if (proto.hasFailureBeingHandled()) {
            failureBeingHandled = FailureBeingHandledModel.fromProto(proto.getFailureBeingHandled(), context);
        }

        for (int handledFailedChildId : proto.getHandledFailedChildrenList()) {
            handledFailedChildren.add(handledFailedChildId);
        }
        executionContext = context;
        processorContext = context.castOnSupport(CoreProcessorContext.class);
        type = proto.getType();
    }

    public ThreadRun.Builder toProto() {
        ThreadRun.Builder out = ThreadRun.newBuilder()
                .setNumber(number)
                .setStatus(status)
                .setThreadSpecName(threadSpecName)
                .setCurrentNodePosition(currentNodePosition)
                .setStartTime(LHUtil.fromDate(startTime))
                .setType(type)
                .setWfSpecId(wfSpecId.toProto());

        if (errorMessage != null) {
            out.setErrorMessage(errorMessage);
        }

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }
        if (parentThreadId != null) {
            out.setParentThreadId(parentThreadId);
        }
        out.addAllChildThreadIds(childThreadIds);

        for (ThreadHaltReasonModel thr : haltReasons) {
            out.addHaltReasons(thr.toProto());
        }
        if (interruptTriggerId != null) {
            out.setInterruptTriggerId(interruptTriggerId.toProto());
        }
        if (failureBeingHandled != null) {
            out.setFailureBeingHandled(failureBeingHandled.toProto());
        }
        for (Integer handledFailedChildId : handledFailedChildren) {
            out.addHandledFailedChildren(handledFailedChildId);
        }
        return out;
    }

    public static ThreadRunModel fromProto(Message p, ExecutionContext context) {
        ThreadRunModel out = new ThreadRunModel();
        out.initFrom(p, context);
        return out;
    }

    @Override
    public Class<ThreadRun> getProtoBaseClass() {
        return ThreadRun.class;
    }

    // For Scheduler

    public WfRunModel wfRun;

    private ThreadSpecModel threadSpecModel;

    public ThreadSpecModel getThreadSpec() {
        if (threadSpecModel == null) {
            threadSpecModel = wfRun.getWfSpec().threadSpecs.get(threadSpecName);
        }
        return threadSpecModel;
    }

    public NodeModel getCurrentNode() {
        NodeRunModel currRun = getCurrentNodeRun();

        // TODO (#465): Determine which version of WfSpec we should get the ThreadSpec from.
        ThreadSpecModel threadSpec = getThreadSpec();
        if (currRun == null) {
            return threadSpec.nodes.get(threadSpec.getEntrypointNodeName());
        }

        return threadSpec.nodes.get(currRun.getNodeName());
    }

    public NodeRunModel getCurrentNodeRun() {
        return getNodeRun(currentNodePosition);
    }

    /**
     * Starts the ThreadRun and advances it past the entrypoint node. Note that it is
     * the responsibility of the caller to validate start variables before calling this
     * method. For example:
     * - The RunWfRequestModel validates variables before creating the WfRun
     * - A START_THREAD NodeRun validates the variables before creating the ThreadRun
     *
     * @param variables are the pre-validated input variables to this ThreadRun.
     */
    public void createVariablesAndStart(Map<String, VariableValueModel> variables) {
        if (currentNodePosition != -1) {
            throw new IllegalStateException("Should only be called on creation");
        }

        currentNodePosition = 0;

        Date now = new Date();
        ThreadSpecModel threadSpec = getThreadSpec();
        setStatus(LHStatus.RUNNING);
        setStartTime(now);

        NodeModel entrypointNode = threadSpec.getNodes().get(threadSpec.getEntrypointNodeName());

        NodeRunModel entrypointRun = new NodeRunModel(processorContext);
        entrypointRun.setThreadRun(this);
        entrypointRun.setNodeName(entrypointNode.getName());
        entrypointRun.setStatus(LHStatus.STARTING);
        entrypointRun.setId(new NodeRunIdModel(wfRun.getId(), this.number, 0));
        entrypointRun.setWfSpecId(wfSpecId);
        entrypointRun.setThreadSpecName(threadSpecName);
        entrypointRun.setArrivalTime(now);
        entrypointRun.setSubNodeRun(entrypointNode.getSubNode().createSubNodeRun(now, processorContext));
        putNodeRun(entrypointRun);

        for (ThreadVarDefModel threadVarDef : threadSpec.getVariableDefs()) {
            VariableDefModel varDef = threadVarDef.getVarDef();
            String varName = varDef.getName();
            VariableValueModel val = null;

            if (threadVarDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) {
                // We do NOT create a variable since we want to use the one from the parent.
                continue;
            }

            if (variables.containsKey(varName)) {
                val = variables.get(varName);
                if (val.isNull()) {
                    val = varDef.getDefaultValue();
                }
            } else if (varDef.getDefaultValue() != null) {
                val = varDef.getDefaultValue();
            }

            if (val == null) {
                val = new VariableValueModel();
            }
            VariableModel variable = new VariableModel(
                    varName, val, wfRun.getId(), this.number, threadSpec.getWfSpec(), varDef.isMaskedValue());
            processorContext.getableManager().put(variable);
        }

        entrypointRun.setStatus(LHStatus.RUNNING);
    }

    /*
     * Note on how ExternalEvents are handled:
     * 1. First, the ExternalEvent is saved to the data store. This is handled
     * in the SchedulerProcessor::processHelper() function.
     * 2. If the ExternalEvent isn't an Interrupt trigger, then if any nodes
     * in any ThreadRuns need to react to it, they will look it up in the store
     * and react appropriately if it's present. That is done by the methods
     * SubNodeRun::advanceIfPossible() and SubNodeRun::arrive().
     * 3. If it's an Interrupt trigger, then we need to trigger the interrupt here.
     */
    public void processExternalEvent(ExternalEventModel e) {
        if (e.getThreadRunNumber() != null && e.getThreadRunNumber() != number) {
            return;
        }
        ExternalEventDefIdModel extEvtId = e.getId().getExternalEventDefId();
        InterruptDefModel idef = getThreadSpec().getInterruptDefFor(extEvtId.getName());
        if (idef != null) {
            // trigger interrupt
            initializeInterrupt(e, idef);
        }
    }

    public void processExtEvtTimeout(ExternalEventTimeoutModel timeout) {
        NodeRunModel nr = getNodeRun(timeout.getNodeRunId().getPosition());
        if (nr.getType() != NodeTypeCase.EXTERNAL_EVENT) {
            log.error("Impossible: got a misconfigured external event timeout: {}", nr.toJson());
            return;
        }
        nr.getExternalEventRun().processExternalEventTimeout(timeout);
    }

    public void processSleepNodeMatured(SleepNodeMaturedModel e) {
        NodeRunModel nr = getNodeRun(e.getNodeRunId().getPosition());
        if (nr.getType() != NodeTypeCase.SLEEP) {
            log.warn("Tried to mature on non-sleep node");
            // TODO: how do we wanna handle exceptions?
            return;
        }

        nr.getSleepNodeRun().processSleepNodeMatured(e);
    }

    public void acknowledgeInterruptStarted(PendingInterruptModel pi, int handlerThreadId) {
        boolean foundIt = false;
        for (int i = haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReasonModel hr = haltReasons.get(i);
            if (hr.type != ReasonCase.PENDING_INTERRUPT) {
                continue;
            }
            if (hr.pendingInterrupt.externalEventId.equals(pi.externalEventId)) {
                foundIt = true;
                haltReasons.remove(i);
            }
        }
        if (!foundIt) {
            throw new RuntimeException("Not possible");
        }

        ThreadHaltReasonModel thr = new ThreadHaltReasonModel();
        thr.threadRun = this;
        thr.type = ReasonCase.INTERRUPTED;
        thr.interrupted = new InterruptedModel();
        thr.interrupted.interruptThreadId = handlerThreadId;

        haltReasons.add(thr);
    }

    private void initializeInterrupt(ExternalEventModel trigger, InterruptDefModel idef) {
        trigger.setClaimed(true);
        // First, stop all child threads.
        ThreadHaltReasonModel haltReason = new ThreadHaltReasonModel();
        haltReason.type = ReasonCase.PENDING_INTERRUPT;
        haltReason.pendingInterrupt = new PendingInterruptHaltReasonModel();
        haltReason.pendingInterrupt.externalEventId = trigger.getObjectId();

        // This also stops the children
        halt(haltReason);

        // Now make sure that the parent WfRun has the info necessary to launch the
        // interrupt on the next call to advance
        PendingInterruptModel pi = new PendingInterruptModel();
        pi.externalEventId = trigger.getObjectId();
        pi.interruptedThreadId = number;
        pi.handlerSpecName = idef.handlerSpecName;

        wfRun.pendingInterrupts.add(pi);
    }

    /**
     * Attempts to "rescue" the ThreadRun. If not possible, returns a `Status` that can
     * be thrown to the client explaining why the ThreadRun could not be rescued.
     * @param skipCurrentNode whether to skip past the current node. If set to `false`, then
     * we attempt to execute the same Node again; else, we move to the next outgoing edge.
     * @param ctx is a CoreProcessorContext.
     * @return Optional.empty() if we can successfully rescue the ThreadRun; else, a Status
     * explaining why not.
     */
    public void rescue(boolean skipCurrentNode, CoreProcessorContext ctx) throws ThreadRunRescueFailedException {
        // First, Optional<Status> refers to the grpc status which can be thrown as an error
        // to the client.

        // Note that any child ThreadRuns that were HALTED with the reason PARENT_HALTED
        // will be automatically un-halted when the status of this ThreadRun moves from
        // ERROR to RUNNING
        if (this.status != LHStatus.ERROR) {
            throw new IllegalStateException("This is a bug: ThreadRun %s on WfRun %s tried to be rescued from status %s"
                    .formatted(number, wfRun.getId(), status));
        }

        try {
            NodeRunModel currentNR = getCurrentNodeRun();
            NodeModel toActivate;
            if (skipCurrentNode) {
                toActivate = currentNR.evaluateOutgoingEdgesAndMaybeMutateVariables(ctx);
            } else {
                toActivate = currentNR.getNode();
            }
            setStatus(LHStatus.RUNNING);
            activateNode(toActivate);
        } catch (NodeFailureException exn) {
            setStatus(LHStatus.ERROR);
            throw new ThreadRunRescueFailedException("Could not rescue threadRun: " + exn.getMessage());
        }

        this.setEndTime(null); // no longer terminated.
        if (getNumber() == 0) {
            // WfRun status needs to reflect the threadRun status.
            wfRun.setStatus(LHStatus.RUNNING);
        } else {
            ThreadRunModel parent = getParent();
            if (parent.getStatus() == LHStatus.ERROR) {
                NodeRunModel parentCurrentNR = parent.getCurrentNodeRun();
                if (parentCurrentNR.getType() == NodeTypeCase.WAIT_THREADS
                        || parentCurrentNR.getType() == NodeTypeCase.EXIT) {

                    FailureModel parentFailure =
                            parent.getCurrentNodeRun().getLatestFailure().get();
                    if (parentFailure.getFailureName().equals(LHErrorType.CHILD_FAILURE.toString())) {
                        parent.rescue(false, ctx);
                    }
                }
            }
        }
    }

    public void halt(ThreadHaltReasonModel reason) {
        reason.setThreadRun(this);
        if (isTerminated()) return;

        // if we got this far, then we know that we are still running. Add the
        // halt reason.
        haltReasons.add(reason);

        if (status != LHStatus.HALTED) setStatus(LHStatus.HALTING);

        // Now need to halt all the children.
        ThreadHaltReasonModel childHaltReason = new ThreadHaltReasonModel();
        childHaltReason.type = ReasonCase.PARENT_HALTED;
        childHaltReason.parentHalted = new ParentHaltedModel();
        childHaltReason.parentHalted.parentThreadId = number;

        for (int childId : childThreadIds) {
            ThreadRunModel child = wfRun.getThreadRun(childId);

            // In almost all cases, we want to stop all children.
            // However, if the child is an interrupt thread, and the parent got
            // interrupted again, we let the two interrupts continue side-by-side.
            if (child.interruptTriggerId != null) {
                if (reason.type != ReasonCase.PENDING_INTERRUPT && reason.type != ReasonCase.INTERRUPTED) {
                    child.halt(childHaltReason);
                } else {
                    log.trace("Not halting sibling interrupt thread! This will change, in future release.");
                }
            } else {
                child.halt(childHaltReason);
            }
        }

        getCurrentNodeRun().maybeHalt(processorContext);
        maybeFinishHaltingProcess();
    }

    public void setStatus(LHStatus status) {
        this.status = status;
    }

    /**
     * Tries to halt this ThreadRun, returns true if successful. As a side effect, the status of
     * this ThreadRun and also its children may transition from HALTING to HALTED.
     * @precondition this ThreadRun is in the HALTING state.
     * @return true if halting this ThreadRun was successful.
     */
    public boolean maybeFinishHaltingProcess() {
        if (isTerminated() || status == LHStatus.HALTED) return true;

        if (status != LHStatus.HALTING) {
            throw new IllegalStateException("Cant finish halting if not halting");
        }
        boolean allChildrenHalted = true;
        for (int childId : childThreadIds) {
            if (!wfRun.getThreadRun(childId).maybeFinishHaltingProcess()) {
                allChildrenHalted = false;
            }
        }

        if (getCurrentNodeRun().maybeHalt(processorContext) && allChildrenHalted) {
            setStatus(LHStatus.HALTED);
            return true;
        }
        return false;
    }

    /**
     * Returns true if this thread is in a dynamic (running) state.
     */
    public boolean isRunning() {
        return (status == LHStatus.RUNNING || status == LHStatus.STARTING || status == LHStatus.HALTING);
    }

    /**
     * Tries to advance the ThreadRun and returns true if a new Node is activated, if a child thread
     * of any type is started, or the status of the ThreadRun changes.
     * @param eventTime is the time of the Command that causes the ThreadRun to advance.
     * @return true if a new node is activated or sub-thread is started.
     */
    public boolean advance(Date eventTime) {
        if (isTerminated()) return false;

        if (status == LHStatus.HALTED) {
            return maybeUnHaltIfAllHaltReasonsResolved();
        }

        if (status == LHStatus.HALTING) {
            return maybeFinishHaltingProcess();
        }

        NodeRunModel currentNR = getCurrentNodeRun();
        try {
            // At this point, we know it's a RUNNING or STARTING thread, so we advance it.
            if (currentNR.getLatestFailure().isPresent()) {
                return maybeAdvanceFromFailedNodeRun();
            }

            boolean canAdvance = currentNR.checkIfProcessingCompleted(processorContext);

            if (!canAdvance) {
                // then we're still waiting on the NodeRun, nothing happened.
                return false;
            }

            if (currentNR.getType() == NodeTypeCase.EXIT) {
                // Then we're done!
                setStatus(LHStatus.COMPLETED);
                endTime = eventTime;
                wfRun.handleThreadStatus(number, eventTime, status);
            } else {
                NodeModel nextNode = currentNR.evaluateOutgoingEdgesAndMaybeMutateVariables(processorContext);
                activateNode(nextNode);
            }
        } catch (NodeFailureException exn) {
            respondToNodeFailure(exn);
        }

        return true;
    }

    /**
     *
     * @return true if we advanced from the failed NodeRun, else false.
     */
    public boolean maybeAdvanceFromFailedNodeRun() {
        NodeRunModel nodeRun = getCurrentNodeRun();
        FailureModel failure = nodeRun.getLatestFailure().get();
        nodeRun.setStatus(failure.getStatus());
        if (!getCurrentNode().getHandlerFor(failure).isPresent()) {
            throw new IllegalStateException("The Failure should be handleable, otherwise we fail earlier");
        }

        if (failure.getFailureHandlerThreadRunId() == null) {

            return false;
        }

        boolean handled =
                wfRun.getThreadRun(failure.getFailureHandlerThreadRunId()).getStatus() == LHStatus.COMPLETED;
        if (handled) {
            try {
                NodeModel nextNode = nodeRun.evaluateOutgoingEdgesAndMaybeMutateVariables(processorContext);
                activateNode(nextNode);
            } catch (NodeFailureException exn) {
                failWithoutGrace(exn.getFailure(), new Date());
                return true;
            }
        }
        return handled;
    }

    /**
     * Handles a node failure. Starts a failure handler, or fails the ThreadRun.
     * @param exn
     */
    private void respondToNodeFailure(NodeFailureException exn) {
        NodeModel node = getCurrentNode();
        FailureModel failure = exn.getFailure();

        Optional<FailureHandlerDefModel> handlerOption = node.getHandlerFor(failure);
        if (handlerOption.isEmpty()) {
            for (int childId : childThreadIds) {
                ThreadRunModel child = wfRun.getThreadRun(childId);
                ThreadHaltReasonModel hr = new ThreadHaltReasonModel();
                hr.type = ReasonCase.PARENT_HALTED;
                hr.parentHalted = new ParentHaltedModel();
                hr.parentHalted.parentThreadId = number;
                child.halt(hr);
                if (child.getCurrentNodeRun().isInProgress()) {
                    child.getCurrentNodeRun().maybeHalt(processorContext);
                }
            }
            getCurrentNodeRun().setStatus(failure.getStatus());
            failWithoutGrace(failure, endTime);
        } else {
            handleFailure(failure, handlerOption.get());
        }
    }

    /**
     * Tries to un-halt the thread by checking if all of the HaltReasons are resolved. Has a side-effect:
     * resolved HaltReasons are removed, and status changed to RUNNING.
     * @return true if the ThreadRun moved to RUNNING.
     */
    private boolean maybeUnHaltIfAllHaltReasonsResolved() {
        haltReasons.removeIf(ThreadHaltReasonModel::isResolved);

        if (haltReasons.isEmpty()) {
            log.debug("Thread {} is alive again!", number);
            if (getCurrentNodeRun().getLatestFailure().isEmpty()) {
                setStatus(LHStatus.RUNNING);
                getCurrentNodeRun().unHalt();
            } else if (getCurrentNodeRun().getLatestFailure().get().isProperlyHandled()) {
                setStatus(LHStatus.RUNNING);
            } else {
                setStatus(getCurrentNodeRun().getLatestFailure().get().getStatus());
            }
            return true;
        }
        return false;
    }

    /**
     * In the case that a Failure is thrown and there is a FailureHandler defined for that failure,
     * we start a "Failure Handler ThreadRun" which handles that failure.
     * @param failure is the failure being handled
     * @param handler is the FailureHandlerDef that defines the ThreadSpec to handle the failure.
     */
    private void handleFailure(FailureModel failure, FailureHandlerDefModel handler) {
        PendingFailureHandlerModel pfh = new PendingFailureHandlerModel();
        pfh.failedThreadRun = this.number;
        pfh.handlerSpecName = handler.handlerSpecName;

        /*
         * It should be noted that the current implementation of Failure Handling is as follows:
         * - We HALT the ThreadRun that threw the Failure
         * - Once that ThreadRun is HALTED, we start the FailureHandler ThreadRun.
         *
         * Note that for a ThreadRun to be HALTED, we need to wait for all of its children to be
         * HALTED as well. That is why we add the "pending failure" to the WfRun, which means that
         * the next time we call advance(), we check to see if the failed ThreadRun is HALTED, and
         * only the do we start the FailureHandler.
         */
        wfRun.pendingFailures.add(pfh);

        ThreadHaltReasonModel haltReason = new ThreadHaltReasonModel();
        haltReason.type = ReasonCase.PENDING_FAILURE;
        haltReason.pendingFailure = new PendingFailureHandlerHaltReasonModel();
        haltReason.pendingFailure.nodeRunPosition = currentNodePosition;

        // This also stops the children
        halt(haltReason);
    }

    /**
     * See the note inside handleFailure(). The WfRunModel is in charge of starting the FailureHandler
     * ThreadRun once the ThreadRun that threw the Failure has reached the HALTED state.
     *
     * When the WfRunModel starts the FailureHandler ThreadRun, then it must also tell the failed
     * ThreadRunModel to "update" its state to reflect the fact that the failed ThreadRun is no longer
     * waiting for the FailureHandler to start; rather, the FailureHandler has already started.
     */
    public void acknowledgeXnHandlerStarted(PendingFailureHandlerModel pfh, int handlerThreadNumber) {
        boolean foundIt = false;
        for (int i = haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReasonModel hr = haltReasons.get(i);
            if (hr.type != ReasonCase.PENDING_FAILURE) {
                continue;
            }
            foundIt = true;
            haltReasons.remove(i);
        }
        if (!foundIt) {
            throw new RuntimeException("Not possible");
        }

        ThreadHaltReasonModel thr = new ThreadHaltReasonModel();
        thr.threadRun = this;
        thr.type = ReasonCase.HANDLING_FAILURE;
        thr.handlingFailure = new HandlingFailureHaltReasonModel(handlerThreadNumber);

        childThreadIds.add(handlerThreadNumber);
        haltReasons.add(thr);
    }

    /**
     * Bypasses failure handlers and causes the ThreadRun to fail without possibility for handling
     * the Failure. This is used for exmaple if the FailureHandler ThreadRun throws a failure.
     *
     * When a ThreadRun fails, the current behavior of the LH Server is that all child ThreadRun's
     * are moved to the HALTED state.
     *
     * If the failing ThreadRun is an Interrupt Handler ThreadRun, then the parent ThreadRun is marked
     * as failed as well.
     */
    private void failWithoutGrace(FailureModel failure, Date time) {
        for (int childId : childThreadIds) {
            ThreadRunModel child = wfRun.getThreadRun(childId);
            if (child == null) {
                // already gc'ed
                continue;
            }
            if (child.isRunning()) {
                log.trace("Not failing threadRun yet; child is halting still");
                if (child.getStatus() != LHStatus.HALTING) {
                    throw new IllegalStateException("Should be HALTING! Bug in LittleHorse.");
                }
            }
        }
        this.errorMessage = failure.message;
        this.status = failure.getStatus();
        this.endTime = time;

        if (interruptTriggerId != null) {
            // then we're an interrupt thread and need to fail the parent. Parent is guaranteed to
            // to be not-null in this case
            getParent()
                    .failWithoutGrace(
                            new FailureModel(
                                    "Interrupt thread with id " + number + " failed!",
                                    failure.getFailureName(),
                                    failure.getContent()), // propagate failure content
                            time);
        } else if (failureBeingHandled != null) {
            // Then it's a FailureHandler thread, so we want the parent ThreadRun to fail without
            // grace.
            getParent().failWithoutGrace(failure, time);
        }

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void activateNode(NodeModel node) throws NodeFailureException {
        Date arrivalTime = new Date();

        currentNodePosition++;

        NodeRunModel cnr = new NodeRunModel(processorContext);
        cnr.setThreadRun(this);
        cnr.setNodeName(node.name);
        cnr.setStatus(LHStatus.STARTING);
        cnr.setId(new NodeRunIdModel(wfRun.getId(), number, currentNodePosition));
        cnr.setWfSpecId(wfSpecId);
        cnr.setThreadSpecName(threadSpecName);
        cnr.setArrivalTime(arrivalTime);
        cnr.setSubNodeRun(node.getSubNode().createSubNodeRun(arrivalTime, processorContext));

        putNodeRun(cnr);

        cnr.arrive(arrivalTime, processorContext);
    }

    public ThreadRunModel getParent() {
        if (parentThreadId == null) return null;
        return wfRun.getThreadRun(parentThreadId);
    }

    public boolean isTerminated() {
        return status == LHStatus.COMPLETED || status == LHStatus.ERROR || status == LHStatus.EXCEPTION;
    }

    public VariableValueModel assignVariable(VariableAssignmentModel assn) throws LHVarSubError {
        return assignVariable(assn, new HashMap<>());
    }

    public VariableValueModel assignVariable(VariableAssignmentModel assn, Map<String, VariableValueModel> txnCache)
            throws LHVarSubError {
        VariableValueModel val = null;
        switch (assn.getRhsSourceType()) {
            case LITERAL_VALUE:
                val = assn.getRhsLiteralValue();
                break;
            case VARIABLE_NAME:
                if (txnCache.containsKey(assn.getVariableName())) {
                    val = txnCache.get(assn.getVariableName());
                } else {
                    val = getVariable(assn.getVariableName()).getValue();
                }

                if (val == null) {
                    throw new LHVarSubError(null, "Variable " + assn.getVariableName() + " not in scope!");
                }

                break;
            case FORMAT_STRING:
                // first, assign the format string
                VariableValueModel formatStringVarVal =
                        assignVariable(assn.getFormatString().getFormat(), txnCache);
                if (formatStringVarVal.getType() != VariableType.STR) {
                    throw new LHVarSubError(
                            null, "Format String template isn't a STR; it's a " + formatStringVarVal.getType());
                }

                List<Object> formatArgs = new ArrayList<>();

                // second, assign the vars
                for (VariableAssignmentModel argAssn : assn.getFormatString().getArgs()) {
                    VariableValueModel variableValue = assignVariable(argAssn, txnCache);
                    formatArgs.add(variableValue.getVal());
                }

                // Finally, format the String.
                try {
                    val = new VariableValueModel(
                            MessageFormat.format(formatStringVarVal.getStrVal(), formatArgs.toArray(new Object[0])));
                } catch (RuntimeException e) {
                    throw new LHVarSubError(e, "Error formatting variable");
                }
                break;
            case NODE_OUTPUT:
                String nodeReferenceName = assn.getNodeOutputReference().getNodeName();
                NodeRunModel referencedNodeRun = getMostRecentNodeRun(nodeReferenceName);
                Optional<VariableValueModel> output = referencedNodeRun.getOutput(processorContext);
                if (output.isEmpty()) {
                    throw new LHVarSubError(
                            null,
                            "Specified node " + nodeReferenceName + " of type " + referencedNodeRun.getType()
                                    + ", number " + referencedNodeRun.getId().getPosition() + " has no output.");
                }
                val = output.get();
                break;
            case EXPRESSION:
                ExpressionModel expression = assn.getExpression();
                val = expression.evaluate(varAssn -> assignVariable(varAssn, txnCache));
                break;
            case SOURCE_NOT_SET:
                // This should have been caught by the WfSpecModel#validate()
                throw new IllegalStateException("Invalid WfSpec with un-set VariableAssignment.");
        }

        if (assn.getJsonPath() != null) {
            val = val.jsonPath(assn.getJsonPath());
        }

        return val;
    }

    public void putNodeRun(NodeRunModel nr) {
        processorContext.getableManager().put(nr);
    }

    public NodeRunModel getNodeRun(int position) {
        NodeRunModel out = processorContext.getableManager().get(new NodeRunIdModel(wfRun.getId(), number, position));
        if (out != null) {
            out.setThreadRun(this);
        }
        return out;
    }

    public NodeRunModel getMostRecentNodeRun(String nodeName) throws LHVarSubError {
        // The only way to find a previous NodeRun is to walk backwards from the current position
        // until we either:
        // 1) Find a NodeRun that matches the nodeName
        // 2) Reach the Entrypoint Node
        //
        // If we reach the entrypoint node, it means that there is no NodeRun that satisfies
        // the requirement; therefore we fail the Variable Assignment with LHVarSubError.
        //
        // TODO: We can find a way to optimize this. Range scans are expensive, and also
        // doing it in this way means that the NodeRun's will be re-put into the GetableManager's
        // buffer, which could get _very_ expensive.
        for (int backwardPosition = currentNodePosition; backwardPosition > 0; backwardPosition--) {
            NodeRunModel nodeRun = this.getNodeRun(backwardPosition);
            if (nodeRun.getNodeName().equals(nodeName)) {
                return nodeRun;
            } else {
                continue;
            }
        }
        throw new LHVarSubError(null, "Specified node " + nodeName + " does not have any previous runs.");
    }

    /**
     * Traverse the current and parent ThreadRun to run the variable modification on
     * the appropriate
     * ThreadRun
     *
     * @param varName  name of the variable
     * @param function function that executes the variable modification (e.g.
     *                 creation, mutation,
     *                 etc.)
     * @throws LHVarSubError when the varName is not found either on the current
     *                       ThreadRun
     *                       definition or its parents definition
     */
    private void applyVarMutationOnAppropriateThread(String varName, VariableMutator function) throws LHVarSubError {
        if (getThreadSpec().localGetVarDef(varName) != null) {
            function.apply(wfRun.getId(), this.number, wfRun);
        } else {
            if (getParent() != null) {
                getParent().applyVarMutationOnAppropriateThread(varName, function);
            } else {
                throw new LHVarSubError(null, "Tried to save out-of-scope var " + varName);
            }
        }
    }

    /**
     * Mutates an existing variable on the current ThreadRun or any of its parents
     * depending on who has the variable on its definition.
     *
     * @param varName name of the variable
     * @param var     value of the variable
     * @throws LHVarSubError when the varName is not found either on the current
     *                       ThreadRun
     *                       definition or its parents definition
     */
    public void mutateVariable(String varName, VariableValueModel var) throws LHVarSubError {
        VariableMutator mutateVariable = (wfRunId, threadRunNumber, wfRun) -> {
            VariableModel variable = getVariable(varName);
            variable.setValue(var);
            processorContext.getableManager().put(variable);
        };
        applyVarMutationOnAppropriateThread(varName, mutateVariable);
    }

    public VariableModel getVariable(String varName) {
        VariableModel out =
                processorContext.getableManager().get(new VariableIdModel(wfRun.getId(), this.number, varName));
        if (out != null) {
            return out;
        }
        if (getParent() != null) {
            return getParent().getVariable(varName);
        }

        // Last thing to check is whether the variable is inherited.
        ThreadVarDefModel threadVarDef = processorContext
                .service()
                .getWfSpec(getWfSpecId())
                .getAllVariables()
                .get(varName);
        if (threadVarDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) {
            // If we validate the WfSpec properly, it should be impossible for parentWfRunId to be null.
            WfRunIdModel parentWfRunId = getWfRun().getId().getParentWfRunId();
            WfRunModel parentWfRun = processorContext.getableManager().get(parentWfRunId);
            ThreadVarDefModel parentVarDef =
                    parentWfRun.getWfSpec().getAllVariables().get(varName);
            if (!(parentVarDef.getAccessLevel() == WfRunVariableAccessLevel.PRIVATE_VAR)) {
                return parentWfRun.getThreadRun(0).getVariable(varName);
            }
        }

        return null;
    }

    /**
     * Allows to apply variable modifications within the context of the ThreadRun
     * that owns it
     */
    @FunctionalInterface
    private interface VariableMutator {
        /**
         * Apply a variable modification within the context of the ThreadRun that owns
         * the variable
         *
         * @param wfRunId         the wfRunId of the ThreadRun that owns the variable
         * @param threadRunNumber the threadRunNumber of the ThreadRun that owns the
         *                        variable
         * @param wfRunModel      the wfRun of the ThreadRun that owns the variable
         */
        void apply(WfRunIdModel wfRunId, int threadRunNumber, WfRunModel wfRunModel);
    }
}

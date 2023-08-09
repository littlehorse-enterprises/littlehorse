package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.VariableModification;
import io.littlehorse.common.model.command.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.command.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.FailureHandlerDef;
import io.littlehorse.common.model.meta.InterruptDef;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.VariableMutation;
import io.littlehorse.common.model.meta.subnode.ExitNode;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.objectId.ExternalEventId;
import io.littlehorse.common.model.wfrun.haltreason.HandlingFailureHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.Interrupted;
import io.littlehorse.common.model.wfrun.haltreason.ParentHalted;
import io.littlehorse.common.model.wfrun.haltreason.PendingFailureHandlerHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.PendingInterruptHaltReason;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb.NodeTypeCase;
import io.littlehorse.sdk.common.proto.ThreadHaltReasonPb;
import io.littlehorse.sdk.common.proto.ThreadHaltReasonPb.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRunPb;
import io.littlehorse.sdk.common.proto.ThreadTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class ThreadRun extends LHSerializable<ThreadRunPb> {

    public String wfRunId;
    public int number;

    public LHStatusPb status;
    public String wfSpecName;
    public int wfSpecVersion;
    public String threadSpecName;
    public int currentNodePosition;

    public Date startTime;
    public Date endTime;

    public String errorMessage;

    public List<Integer> childThreadIds;
    public Integer parentThreadId;

    public List<ThreadHaltReason> haltReasons;
    public ExternalEventId interruptTriggerId;
    public FailureBeingHandled failureBeingHandled;
    public List<Integer> handledFailedChildren;

    public ThreadTypePb type;

    public ThreadRun() {
        childThreadIds = new ArrayList<>();
        haltReasons = new ArrayList<>();
        handledFailedChildren = new ArrayList<>();
    }

    public void initFrom(Message p) {
        ThreadRunPb proto = (ThreadRunPb) p;
        wfRunId = proto.getWfRunId();
        number = proto.getNumber();
        status = proto.getStatus();
        wfSpecName = proto.getWfSpecName();
        wfSpecVersion = proto.getWfSpecVersion();
        threadSpecName = proto.getThreadSpecName();
        currentNodePosition = proto.getCurrentNodePosition();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());
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
                LHSerializable.fromProto(
                    proto.getInterruptTriggerId(),
                    ExternalEventId.class
                );
        }

        for (ThreadHaltReasonPb thrpb : proto.getHaltReasonsList()) {
            ThreadHaltReason thr = ThreadHaltReason.fromProto(thrpb);
            thr.threadRun = this;
            haltReasons.add(thr);
        }

        if (proto.hasFailureBeingHandled()) {
            failureBeingHandled =
                FailureBeingHandled.fromProto(proto.getFailureBeingHandled());
        }

        for (int handledFailedChildId : proto.getHandledFailedChildrenList()) {
            handledFailedChildren.add(handledFailedChildId);
        }

        type = proto.getType();
    }

    public ThreadRunPb.Builder toProto() {
        ThreadRunPb.Builder out = ThreadRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setNumber(number)
            .setStatus(status)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion)
            .setThreadSpecName(threadSpecName)
            .setCurrentNodePosition(currentNodePosition)
            .setStartTime(LHUtil.fromDate(startTime))
            .setType(type);

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

        for (ThreadHaltReason thr : haltReasons) {
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

    public static ThreadRun fromProto(Message p) {
        ThreadRun out = new ThreadRun();
        out.initFrom(p);
        return out;
    }

    public Class<ThreadRunPb> getProtoBaseClass() {
        return ThreadRunPb.class;
    }

    // For Scheduler

    public WfRun wfRun;

    private ThreadSpec threadSpec;

    public ThreadSpec getThreadSpec() {
        if (threadSpec == null) {
            threadSpec = wfRun.wfSpec.threadSpecs.get(threadSpecName);
        }
        return threadSpec;
    }

    public Node getCurrentNode() {
        NodeRun currRun = getCurrentNodeRun();
        ThreadSpec t = getThreadSpec();
        if (currRun == null) {
            return t.nodes.get(t.entrypointNodeName);
        }

        return t.nodes.get(currRun.nodeName);
    }

    public NodeRun getCurrentNodeRun() {
        return getNodeRun(currentNodePosition);
    }

    /*
     * Note on how ExternalEvents are handled:
     * 1. First, the ExternalEvent is saved to the data store. This is handled
     *    in the SchedulerProcessor::processHelper() function.
     * 2. If the ExternalEvent isn't an Interrupt trigger, then if any nodes
     *    in any ThreadRuns need to react to it, they will look it up in the store
     *    and react appropriately if it's present. That is done by the methods
     *    SubNodeRun::advanceIfPossible() and SubNodeRun::arrive().
     * 3. If it's an Interrupt trigger, then we need to trigger the interrupt here.
     */
    public void processExternalEvent(ExternalEvent e) {
        String extEvtName = e.externalEventDefName;
        InterruptDef idef = getThreadSpec().getInterruptDefFor(extEvtName);
        if (idef != null) {
            // trigger interrupt
            initializeInterrupt(e, idef);
        }
    }

    public void processExtEvtTimeout(ExternalEventTimeout timeout) {
        NodeRun nr = getNodeRun(timeout.nodeRunPosition);
        if (nr.type != NodeTypeCase.EXTERNAL_EVENT) {
            log.error(
                "Impossible: got a misconfigured external event timeout: {}",
                nr.toJson()
            );
            return;
        }
        nr.externalEventRun.processExternalEventTimeout(timeout);
    }

    public void processSleepNodeMatured(SleepNodeMatured e) {
        NodeRun nr = getNodeRun(e.nodeRunPosition);
        if (nr.type != NodeTypeCase.SLEEP) {
            log.warn("Tried to mature on non-sleep node");
            // TODO: how do we wanna handle exceptions?
            return;
        }

        nr.sleepNodeRun.processSleepNodeMatured(e);
    }

    public void acknowledgeInterruptStarted(
        PendingInterrupt pi,
        int handlerThreadId
    ) {
        boolean foundIt = false;
        for (int i = haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReason hr = haltReasons.get(i);
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

        ThreadHaltReason thr = new ThreadHaltReason();
        thr.threadRun = this;
        thr.type = ReasonCase.INTERRUPTED;
        thr.interrupted = new Interrupted();
        thr.interrupted.interruptThreadId = handlerThreadId;

        childThreadIds.add((Integer) handlerThreadId);

        haltReasons.add(thr);
    }

    private void initializeInterrupt(ExternalEvent trigger, InterruptDef idef) {
        // First, stop all child threads.
        ThreadHaltReason haltReason = new ThreadHaltReason();
        haltReason.type = ReasonCase.PENDING_INTERRUPT;
        haltReason.pendingInterrupt = new PendingInterruptHaltReason();
        haltReason.pendingInterrupt.externalEventId = trigger.getObjectId();

        // This also stops the children
        halt(haltReason);

        // Now make sure that the parent WfRun has the info necessary to launch the
        // interrupt on the next call to advance
        PendingInterrupt pi = new PendingInterrupt();
        pi.externalEventId = trigger.getObjectId();
        pi.interruptedThreadId = number;
        pi.handlerSpecName = idef.handlerSpecName;

        wfRun.pendingInterrupts.add(pi);
    }

    public void halt(ThreadHaltReason reason) {
        reason.threadRun = this;
        switch (status) {
            case COMPLETED:
            case ERROR:
                // Already terminated, ignoring halt
                return;
            case STARTING:
            case RUNNING:
            case HALTING:
                if (canBeInterrupted()) {
                    setStatus(LHStatusPb.HALTED);
                } else {
                    setStatus(LHStatusPb.HALTING);
                }
                break;
            case HALTED:
                setStatus(LHStatusPb.HALTED);
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }

        // if we got this far, then we know that we are still running. Add the
        // halt reason.
        haltReasons.add(reason);

        // Now need to halt all the children.
        ThreadHaltReason childHaltReason = new ThreadHaltReason();
        childHaltReason.type = ReasonCase.PARENT_HALTED;
        childHaltReason.parentHalted = new ParentHalted();
        childHaltReason.parentHalted.parentThreadId = number;

        for (int childId : childThreadIds) {
            ThreadRun child = wfRun.threadRuns.get(childId);

            // In almost all cases, we want to stop all children.
            // However, if the child is an interrupt thread, and the parent got
            // interrupted again, we let the two interrupts continue side-by-side.
            if (child.interruptTriggerId != null) {
                if (
                    reason.type != ReasonCase.PENDING_INTERRUPT &&
                    reason.type != ReasonCase.INTERRUPTED
                ) {
                    child.halt(childHaltReason);
                } else {
                    log.debug(
                        "Not halting sibling interrupt thread! This will change, in future release."
                    );
                }
            } else {
                child.halt(childHaltReason);
            }
        }
    }

    /*
     * Checks if the status can be changed. Returns true if status did change.
     */
    public boolean updateStatus() {
        if (status == LHStatusPb.COMPLETED || status == LHStatusPb.ERROR) {
            return false;
        } else if (status == LHStatusPb.RUNNING) {
            return false;
        } else if (status == LHStatusPb.HALTED) {
            // determine if halt reasons are resolved or not.

            // This is where ThreadRun's wake up for example when an exception handler
            // completes.
            for (int i = haltReasons.size() - 1; i >= 0; i--) {
                ThreadHaltReason hr = haltReasons.get(i);
                if (hr.isResolved()) {
                    haltReasons.remove(i);
                    log.debug(
                        "Removed haltReason {} on thread {} {}, leaving: {}",
                        hr,
                        wfRunId,
                        number,
                        haltReasons
                    );
                }
            }
            if (haltReasons.isEmpty()) {
                log.debug("Thread {} is alive again!", number);
                setStatus(LHStatusPb.RUNNING);
                return true;
            } else {
                return false;
            }
        } else if (status == LHStatusPb.HALTING) {
            if (getCurrentNodeRun().canBeInterrupted()) {
                setStatus(LHStatusPb.HALTED);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void setStatus(LHStatusPb status) {
        this.status = status;
    }

    /*
     * Returns true if we can move this Thread from HALTING to HALTED status.
     */

    public boolean canBeInterrupted() {
        if (getCurrentNodeRun().canBeInterrupted()) return true;

        for (int childId : childThreadIds) {
            if (wfRun.threadRuns.get(childId).isRunning()) {
                return false;
            }
        }
        return true;
    }

    /*
     * Returns true if this thread is in a dynamic (running) state.
     */

    public boolean isRunning() {
        return (
            status == LHStatusPb.RUNNING ||
            status == LHStatusPb.STARTING ||
            status == LHStatusPb.HALTING
        );
    }

    public boolean advance(Date eventTime) {
        NodeRun currentNodeRun = getCurrentNodeRun();

        if (status == LHStatusPb.RUNNING) {
            // Just advance the node. Not fancy.
            return currentNodeRun.advanceIfPossible(eventTime);
        } else if (status == LHStatusPb.HALTED) {
            // This means we just need to wait until advance() is called again
            // after Thread Resumption

            log.info("Tried to advance HALTED thread. Doing nothing.");
            return false;
        } else if (status == LHStatusPb.HALTING) {
            log.info("Tried to advance HALTING thread, checking if halted yet.");

            if (currentNodeRun.canBeInterrupted()) {
                setStatus(LHStatusPb.HALTED);
                log.info("Moving thread to HALTED");
                return true;
            } else {
                return false;
            }
        } else if (status == LHStatusPb.COMPLETED) {
            // Nothing to do, this is likely an innocuous event.
            return false;
        } else if (status == LHStatusPb.ERROR) {
            // This is innocuous. Occurs when a timeout event comes in after
            // a thread fails or completes. Nothing to do.

            return false;
        } else if (status == LHStatusPb.STARTING) {
            setStatus(LHStatusPb.RUNNING);
            return currentNodeRun.advanceIfPossible(eventTime);
        } else {
            throw new RuntimeException("Unrecognized status: " + status);
        }
    }

    public void fail(Failure failure, Date time) {
        // First determine if the node that was failed has a relevant exception
        // handler attached.

        Node curNode = getCurrentNode();
        FailureHandlerDef handler = null;

        for (FailureHandlerDef candidate : curNode.failureHandlers) {
            if (candidate.doesHandle(failure.failureName)) {
                handler = candidate;
                break;
            }
        }

        if (handler == null) {
            dieForReal(failure, time);
        } else {
            handleFailure(failure, handler);
        }
    }

    private void handleFailure(Failure failure, FailureHandlerDef handler) {
        PendingFailureHandler pfh = new PendingFailureHandler();
        pfh.failedThreadRun = this.number;
        pfh.handlerSpecName = handler.handlerSpecName;

        wfRun.pendingFailures.add(pfh);

        ThreadHaltReason haltReason = new ThreadHaltReason();
        haltReason.type = ReasonCase.PENDING_FAILURE;
        haltReason.pendingFailure = new PendingFailureHandlerHaltReason();
        haltReason.pendingFailure.nodeRunPosition = currentNodePosition;

        // This also stops the children
        halt(haltReason);
        getWfRun().advance(getWfRun().getDao().getEventTime());
    }

    public void acknowledgeXnHandlerStarted(
        PendingFailureHandler pfh,
        int handlerThreadNumber
    ) {
        boolean foundIt = false;
        for (int i = haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReason hr = haltReasons.get(i);
            if (hr.type != ReasonCase.PENDING_FAILURE) {
                continue;
            }
            foundIt = true;
            haltReasons.remove(i);
        }
        if (!foundIt) {
            throw new RuntimeException("Not possible");
        }

        ThreadHaltReason thr = new ThreadHaltReason();
        thr.threadRun = this;
        thr.type = ReasonCase.HANDLING_FAILURE;
        thr.handlingFailure = new HandlingFailureHaltReason();
        thr.handlingFailure.handlerThreadId = handlerThreadNumber;

        childThreadIds.add((Integer) handlerThreadNumber);
        haltReasons.add(thr);
    }

    public void dieForReal(Failure failure, Date time) {
        this.errorMessage = failure.message;
        setStatus(LHStatusPb.ERROR);
        this.endTime = time;

        for (int childId : childThreadIds) {
            ThreadRun child = wfRun.threadRuns.get(childId);
            ThreadHaltReason hr = new ThreadHaltReason();
            hr.type = ReasonCase.PARENT_HALTED;
            hr.parentHalted = new ParentHalted();
            hr.parentHalted.parentThreadId = number;
            child.halt(hr);
        }

        if (interruptTriggerId != null) {
            // then we're an interrupt thread and need to fail the parent.

            getParent() // guaranteed not to be null in this case
                .failWithoutGrace(
                    new Failure(
                        "Interrupt thread with id " + number + " failed!",
                        failure.failureName
                    ),
                    time
                );
        } else if (failureBeingHandled != null) {
            getParent()
                .failWithoutGrace(
                    new Failure(
                        "Interrupt thread with id " + number + " failed!",
                        failure.failureName
                    ),
                    time
                );
        }

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void failWithoutGrace(Failure failure, Date time) {
        dieForReal(failure, time);
    }

    public void complete(Date time) {
        this.errorMessage = null;
        setStatus(LHStatusPb.COMPLETED);
        endTime = time;

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void completeCurrentNode(VariableValue output, Date eventTime) {
        NodeRun crn = getCurrentNodeRun();
        crn.status = LHStatusPb.COMPLETED;

        try {
            mutateVariables(output);
        } catch (LHVarSubError exn) {
            fail(
                new Failure(
                    "Failed mutating variables: " + exn.getMessage(),
                    LHConstants.VAR_MUTATION_ERROR
                ),
                eventTime
            );
            return;
        }

        if (status == LHStatusPb.RUNNING) {
            // If we got here, then we're good.
            advanceFrom(getCurrentNode());
        }
        getWfRun().advance(eventTime);
    }

    public void advanceFrom(Node curNode) {
        if (curNode.getSubNode().getClass().equals(ExitNode.class)) {
            return;
        }
        Node nextNode = null;
        for (Edge e : curNode.outgoingEdges) {
            try {
                if (evaluateEdge(e)) {
                    nextNode = e.getSinkNode();
                    break;
                }
            } catch (LHVarSubError exn) {
                log.error(
                    "Failing threadrun due to VarSubError {} {}",
                    wfRun.id,
                    currentNodePosition,
                    exn
                );
                fail(
                    new Failure(
                        "Failed evaluating outgoing edge: " + exn.getMessage(),
                        LHConstants.VAR_MUTATION_ERROR
                    ),
                    new Date()
                );
                return;
            }
        }
        if (nextNode == null) {
            // TODO: Later versions should validate wfSpec's so that this is not
            // possible
            fail(
                new Failure(
                    "WfSpec was invalid. There were no activated outgoing edges" +
                    " from a non-exit node.",
                    LHConstants.INTERNAL_ERROR
                ),
                new Date()
            );
        } else {
            activateNode(nextNode);
        }
    }

    public void activateNode(Node node) {
        Date arrivalTime = new Date();

        currentNodePosition++;

        NodeRun cnr = new NodeRun();
        cnr.setDao(wfRun.getDao());
        cnr.setThreadRun(this);
        cnr.nodeName = node.name;
        cnr.status = LHStatusPb.STARTING;

        cnr.wfRunId = wfRunId;
        cnr.threadRunNumber = number;
        cnr.position = currentNodePosition;
        cnr.threadSpecName = threadSpecName;

        cnr.arrivalTime = arrivalTime;
        cnr.wfSpecId = wfRun.getWfSpec().getObjectId();
        cnr.nodeName = node.name;

        cnr.position = currentNodePosition;

        cnr.setSubNodeRun(node.getSubNode().createSubNodeRun(arrivalTime));

        putNodeRun(cnr);

        cnr.getSubNodeRun().arrive(arrivalTime);
        cnr.getSubNodeRun().advanceIfPossible(arrivalTime);
    }

    private boolean evaluateEdge(Edge e) throws LHVarSubError {
        if (e.condition == null) {
            return true;
        }

        VariableValue lhs = assignVariable(e.condition.left);
        VariableValue rhs = assignVariable(e.condition.right);

        switch (e.condition.comparator) {
            case LESS_THAN:
                return Comparer.compare(lhs, rhs) < 0;
            case LESS_THAN_EQ:
                return Comparer.compare(lhs, rhs) <= 0;
            case GREATER_THAN:
                return Comparer.compare(lhs, rhs) > 0;
            case GREATER_THAN_EQ:
                return Comparer.compare(lhs, rhs) >= 0;
            case EQUALS:
                return lhs != null && Comparer.compare(lhs, rhs) == 0;
            case NOT_EQUALS:
                return lhs != null && Comparer.compare(lhs, rhs) != 0;
            case IN:
                return Comparer.contains(rhs, lhs);
            case NOT_IN:
                return !Comparer.contains(rhs, lhs);
            case UNRECOGNIZED:
        }

        // TODO: Refactor this line
        throw new RuntimeException(
            "Unhandled comparison enum " + e.condition.comparator
        );
    }

    public ThreadRun getParent() {
        if (parentThreadId == null) return null;
        return wfRun.threadRuns.get(parentThreadId);
    }

    public List<VarNameAndVal> assignVarsForNode(TaskNode node) throws LHVarSubError {
        List<VarNameAndVal> out = new ArrayList<>();
        TaskDef taskDef = node.getTaskDef();

        if (taskDef.inputVars.size() != node.variables.size()) {
            throw new LHVarSubError(
                null,
                "Impossible: got different number of taskdef vars and node input vars"
            );
        }

        for (int i = 0; i < taskDef.inputVars.size(); i++) {
            VariableDef requiredVarDef = taskDef.inputVars.get(i);
            VariableAssignment assn = node.variables.get(i);
            String varName = requiredVarDef.name;
            VariableValue val;

            if (assn != null) {
                val = assignVariable(assn);
            } else {
                throw new LHVarSubError(
                    null,
                    "Variable " + varName + " is unassigned."
                );
            }
            if (val.type != requiredVarDef.type && val.type != VariableTypePb.NULL) {
                throw new LHVarSubError(
                    null,
                    "Variable " +
                    varName +
                    " should be " +
                    requiredVarDef.type +
                    " but is of type " +
                    val.type
                );
            }
            out.add(new VarNameAndVal(varName, val));
        }
        return out;
    }

    private void mutateVariables(VariableValue nodeOutput) throws LHVarSubError {
        Node node = getCurrentNode();

        // Need to do this atomically in a transaction, so that if one of the
        // mutations fail then none of them occur.
        // That's why we write to an in-memory Map. If all mutations succeed,
        // then we flush the contents of the Map to the Variables.
        Map<String, VariableValue> varCache = new HashMap<>();
        for (VariableMutation mut : node.variableMutations) {
            try {
                mut.execute(this, varCache, nodeOutput);
            } catch (LHVarSubError exn) {
                log.error(exn.getMessage(), exn);
                exn.addPrefix(
                    "Mutating variable " +
                    mut.lhsName +
                    " with operation " +
                    mut.operation
                );
                throw exn;
            }
        }

        // If we got this far without a LHVarSubError, then we can safely save all
        // of the variables.
        for (Map.Entry<String, VariableValue> entry : varCache.entrySet()) {
            // this method saves the variable into the appropriate ThreadRun,
            // respecting the fact that child ThreadRun's can access their
            // parents' variables.
            mutateVariable(entry.getKey(), entry.getValue());
        }
    }

    public boolean isTerminated() {
        return status == LHStatusPb.COMPLETED || status == LHStatusPb.ERROR;
    }

    public VariableValue assignVariable(VariableAssignment assn)
        throws LHVarSubError {
        return assignVariable(assn, new HashMap<>());
    }

    public VariableValue assignVariable(
        VariableAssignment assn,
        Map<String, VariableValue> txnCache
    ) throws LHVarSubError {
        VariableValue val = null;
        switch (assn.getRhsSourceType()) {
            case LITERAL_VALUE:
                val = assn.getRhsLiteralValue();
                break;
            case VARIABLE_NAME:
                if (txnCache.containsKey(assn.getVariableName())) {
                    val = txnCache.get(assn.getVariableName());
                } else {
                    val = getVariable(assn.getVariableName()).value;
                }

                if (val == null) {
                    throw new LHVarSubError(
                        null,
                        "Variable " + assn.getVariableName() + " not in scope!"
                    );
                }

                break;
            case FORMAT_STRING:
                // first, assign the format string
                VariableValue formatStringVarVal = assignVariable(
                    assn.getFormatString().getFormat(),
                    txnCache
                );
                if (formatStringVarVal.getType() != VariableTypePb.STR) {
                    throw new LHVarSubError(
                        null,
                        "Format String template isn't a STR; it's a " +
                        formatStringVarVal.getType()
                    );
                }

                List<Object> formatArgs = new ArrayList<>();

                // second, assign the vars
                for (VariableAssignment argAssn : assn.getFormatString().getArgs()) {
                    VariableValue variableValue = assignVariable(argAssn, txnCache);
                    formatArgs.add(variableValue.getVal());
                }

                // Finally, format the String.
                val = new VariableValue();
                val.type = VariableTypePb.STR;
                try {
                    val.strVal =
                        MessageFormat.format(
                            formatStringVarVal.strVal,
                            formatArgs.toArray(new Object[0])
                        );
                } catch (RuntimeException e) {
                    throw new LHVarSubError(e, "Error formatting variable");
                }
                break;
            case SOURCE_NOT_SET:
            // Not possible
        }

        // TODO: Refactor this line
        if (val == null) throw new RuntimeException("Not possible");

        if (assn.getJsonPath() != null) {
            val = val.jsonPath(assn.getJsonPath());
        }

        return val;
    }

    public void putNodeRun(NodeRun task) {
        wfRun.getDao().putNodeRun(task);
    }

    public NodeRun getNodeRun(int position) {
        NodeRun out = wfRun.getDao().getNodeRun(wfRun.id, number, position);
        if (out != null) {
            out.setThreadRun(this);
        }
        return out;
    }

    /**
     * Traverse the current and parent ThreadRun to run the variable modification on the appropriate ThreadRun
     * @param varName name of the variable
     * @param function function that executes the variable modification (e.g. creation, mutation, etc.)
     * @throws LHVarSubError when the varName is not found either on the current ThreadRun definition
     * or its parents definition
     */
    private void applyOnAppropriateThread(String varName, VariableModification function) throws LHVarSubError {
        if (getThreadSpec().localGetVarDef(varName) != null) {
            function.apply(wfRunId, this.number, wfRun);
        } else  {
            if (getParent() != null) {
                getParent().applyOnAppropriateThread(varName, function);
            } else {
                throw new LHVarSubError(null, "Tried to save out-of-scope var " + varName);
            }
        }
    }

    /**
     * Creates a new variable on the current ThreadRun or any of its parents depending on
     * who has the variable on its definition
     * @param varName name of the variable
     * @param var value of the variable
     * @throws LHVarSubError when the varName is not found either on the current ThreadRun definition
     * or its parents definition
     */
    public void createVariable(String varName, VariableValue var) throws LHVarSubError {
        VariableModification createVariable = (wfRunId, threadRunNumber, wfRun) -> {
            Variable variable = new Variable(
                    varName,
                    var,
                    wfRunId,
                    threadRunNumber,
                    wfRun.getWfSpec()
            );
            wfRun.getDao().putVariable(variable);
        };
        applyOnAppropriateThread(varName, createVariable);
    }

    /**
     * Mutates an existing variable on the current ThreadRun or any of its parents depending on
     * who has the variable on its definition
     * @param varName name of the variable
     * @param var value of the variable
     * @throws LHVarSubError when the varName is not found either on the current ThreadRun definition
     * or its parents definition
     */
    public void mutateVariable(String varName, VariableValue var) throws LHVarSubError {
        VariableModification mutateVariable = (wfRunId, threadRunNumber, wfRun) -> {
            Variable variable = wfRun.getDao().getVariable(wfRunId, varName, threadRunNumber);
            variable.setValue(var);
            wfRun.getDao().putVariable(variable);
        };
        applyOnAppropriateThread(varName, mutateVariable);
    }

    public Variable getVariable(String varName) {
        // For now, just do the local one
        // Once we have threads, this will do a backtrack up the thread tree.
        Variable out = wfRun.getDao().getVariable(wfRunId, varName, this.number);
        if (out != null) {
            return out;
        }
        if (getParent() != null) {
            return getParent().getVariable(varName);
        }

        return null;
    }
}

// TODO: Shouldn't we to move this class to its own file?
@Slf4j
class Comparer {

    @SuppressWarnings("all") // lol
    public static int compare(VariableValue left, VariableValue right)
        throws LHVarSubError {
        try {
            int result =
                ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());
            return result;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new LHVarSubError(exn, "Failed comparing the provided values.");
        }
    }

    public static boolean contains(VariableValue left, VariableValue right)
        throws LHVarSubError {
        // Can only do for Str, Arr, and Obj

        if (left.type == VariableTypePb.STR) {
            String rStr = right.asStr().strVal;

            return left.asStr().strVal.contains(rStr);
        } else if (left.type == VariableTypePb.JSON_ARR) {
            Object rObj = right.getVal();
            List<Object> lhs = left.asArr().jsonArrVal;

            for (Object o : lhs) {
                if (LHUtil.deepEquals(o, rObj)) {
                    return true;
                }
            }
            return false;
        } else if (left.type == VariableTypePb.JSON_OBJ) {
            return left.asObj().jsonObjVal.containsKey(right.asStr().strVal);
        } else {
            throw new LHVarSubError(null, "Can't do CONTAINS on " + left.type);
        }
    }
}

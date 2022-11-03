package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Edge;
import io.littlehorse.common.model.meta.FailureHandlerDef;
import io.littlehorse.common.model.meta.InterruptDef;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.VariableMutation;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.wfrun.haltreason.HandlingFailureHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.Interrupted;
import io.littlehorse.common.model.wfrun.haltreason.ParentHalted;
import io.littlehorse.common.model.wfrun.haltreason.PendingFailureHandlerHaltReason;
import io.littlehorse.common.model.wfrun.haltreason.PendingInterruptHaltReason;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.ThreadHaltReasonPb;
import io.littlehorse.common.proto.ThreadHaltReasonPb.ReasonCase;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThreadRun extends LHSerializable<ThreadRunPb> {

    public String wfRunId;
    public int number;

    public LHStatusPb status;
    public String wfSpecId;
    public String threadSpecName;
    public int currentNodePosition;

    public Date startTime;
    public Date endTime;

    public String errorMessage;
    public TaskResultCodePb resultCode;

    public List<Integer> childThreadIds;
    public Integer parentThreadId;

    public List<ThreadHaltReason> haltReasons;
    public String interruptTriggerId;
    public FailureBeingHandled failureBeingHandled;

    public ThreadRun() {
        variables = new HashMap<>();
        childThreadIds = new ArrayList<>();
        haltReasons = new ArrayList<>();
    }

    public void initFrom(MessageOrBuilder p) {
        ThreadRunPb proto = (ThreadRunPb) p;
        wfRunId = proto.getWfRunId();
        number = proto.getNumber();
        status = proto.getStatus();
        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        currentNodePosition = proto.getCurrentNodePosition();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
        if (proto.hasErrorMessage()) {
            errorMessage = proto.getErrorMessage();
        }
        if (proto.hasResultCode()) {
            resultCode = proto.getResultCode();
        }
        if (proto.hasParentThreadId()) parentThreadId = proto.getParentThreadId();
        for (Integer childId : proto.getChildThreadIdsList()) {
            childThreadIds.add(childId);
        }

        if (proto.hasInterruptTriggerId()) {
            interruptTriggerId = proto.getInterruptTriggerId();
        }

        for (ThreadHaltReasonPb thrpb : proto.getHaltReasonsList()) {
            ThreadHaltReason thr = ThreadHaltReason.fromProto(thrpb);
            thr.threadRun = this;
            haltReasons.add(thr);
        }

        if (proto.hasFailureBeingHandled()) {
            failureBeingHandled =
                FailureBeingHandled.fromProto(
                    proto.getFailureBeingHandledOrBuilder()
                );
        }
    }

    public ThreadRunPb.Builder toProto() {
        ThreadRunPb.Builder out = ThreadRunPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setNumber(number)
            .setStatus(status)
            .setWfSpecId(wfSpecId)
            .setThreadSpecName(threadSpecName)
            .setCurrentNodePosition(currentNodePosition)
            .setStartTime(LHUtil.fromDate(startTime));

        if (resultCode != null) {
            out.setResultCode(resultCode);
        }

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
            out.setInterruptTriggerId(interruptTriggerId);
        }
        if (failureBeingHandled != null) {
            out.setFailureBeingHandled(failureBeingHandled.toProto());
        }
        return out;
    }

    public static ThreadRun fromProto(MessageOrBuilder p) {
        ThreadRun out = new ThreadRun();
        out.initFrom(p);
        return out;
    }

    public Class<ThreadRunPb> getProtoBaseClass() {
        return ThreadRunPb.class;
    }

    // For Scheduler
    @JsonIgnore
    public WfRun wfRun;

    @JsonIgnore
    public Map<String, Variable> variables;

    @JsonIgnore
    private ThreadSpec threadSpec;

    @JsonIgnore
    public ThreadSpec getThreadSpec() {
        if (threadSpec == null) {
            threadSpec = wfRun.wfSpec.threadSpecs.get(threadSpecName);
        }
        return threadSpec;
    }

    @JsonIgnore
    public Node getCurrentNode() {
        NodeRun currRun = getCurrentNodeRun();
        ThreadSpec t = getThreadSpec();
        if (currRun == null) {
            return t.nodes.get(t.entrypointNodeName);
        }

        return t.nodes.get(currRun.nodeName);
    }

    @JsonIgnore
    public NodeRun getCurrentNodeRun() {
        return getNodeRun(currentNodePosition);
    }

    public void processEvent(WfRunEvent e) {
        // External Events get a little bit of special handling since they may not
        // already have information about the node...and they can have impact at the
        // ThreadRun level (eg. interrupts) rather than just the node level.
        if (e.type == EventCase.EXTERNAL_EVENT) {
            registerExternalEvent(e);
            return;
        }

        if (e.getThreadRunNumber() == null || e.getThreadRunNumber() != number) {
            return;
        }

        Integer nrpos = e.getNodeRunPosition();
        if (nrpos != null) {
            if (nrpos > currentNodePosition) {
                // Got event for unknown future node. Skipping.
                return;
            }
            getNodeRun(nrpos).processEvent(e);
        } else {
            // In the future, we may have things like Interrupts or STOP_REQUEST
            // in which it's assigned to a Thread but not a Node.

            // Got an event without assigned to node. Skipping.
        }
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
    private void registerExternalEvent(WfRunEvent e) {
        if (e.type != EventCase.EXTERNAL_EVENT) {
            throw new RuntimeException("Not possible");
        }

        String extEvtName = e.externalEvent.externalEventDefName;
        InterruptDef idef = getThreadSpec().getInterruptDefFor(extEvtName);
        if (idef != null) {
            // trigger interrupt
            initializeInterrupt(e.externalEvent, idef);
        }
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
                    status = LHStatusPb.HALTED;
                } else {
                    status = LHStatusPb.HALTING;
                }
                break;
            case HALTED:
                status = LHStatusPb.HALTED;
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
            wfRun.threadRuns.get(childId).halt(childHaltReason);
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
            for (int i = haltReasons.size() - 1; i >= 0; i--) {
                ThreadHaltReason hr = haltReasons.get(i);
                if (hr.isResolved()) {
                    haltReasons.remove(i);
                }
            }
            if (haltReasons.isEmpty()) {
                status = LHStatusPb.RUNNING;
                return true;
            } else {
                return false;
            }
        } else if (status == LHStatusPb.HALTING) {
            if (getCurrentNodeRun().canBeInterrupted()) {
                status = LHStatusPb.HALTED;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /*
     * Returns true if we can move this Thread from HALTING to HALTED status.
     */
    @JsonIgnore
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
    @JsonIgnore
    public boolean isRunning() {
        return (
            status == LHStatusPb.RUNNING ||
            status == LHStatusPb.STARTING ||
            status == LHStatusPb.HALTING
        );
    }

    @JsonIgnore
    public boolean advance(Date eventTime) {
        NodeRun currentNodeRun = getCurrentNodeRun();

        if (status == LHStatusPb.RUNNING) {
            // Just advance the node. Not fancy.
            return currentNodeRun.advanceIfPossible(eventTime);
        } else if (status == LHStatusPb.HALTED) {
            // This means we just need to wait until advance() is called again
            // after Thread Resumption

            LHUtil.log("Tried to advance HALTED thread. Doing nothing.");
            return false;
        } else if (status == LHStatusPb.HALTING) {
            // TODO: Decide whether we want to pull this out into a method
            // like `ThreadRun::updateStatus()` or keep it here

            LHUtil.log("Tried to advance HALTING thread, checking if halted yet.");

            if (currentNodeRun.canBeInterrupted()) {
                status = LHStatusPb.HALTED;
                LHUtil.log("Moving thread to HALTED");
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
            status = LHStatusPb.RUNNING;
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
        this.resultCode = failure.failureCode;
        this.errorMessage = failure.message;
        status = LHStatusPb.ERROR;
        this.endTime = time;

        for (int childId : childThreadIds) {
            ThreadRun child = wfRun.threadRuns.get(childId);
            ThreadHaltReason hr = new ThreadHaltReason();
            hr.type = ReasonCase.PARENT_HALTED;
            hr.parentHalted = new ParentHalted();
            hr.parentHalted.parentThreadId = number;
            child.halt(hr);
        }

        wfRun.handleThreadStatus(number, new Date(), status);

        if (interruptTriggerId != null) {
            // then we're an interrupt thread and need to fail the parent.

            getParent() // guaranteed not to be null in this case
                .failWithoutGrace(
                    new Failure(
                        TaskResultCodePb.INTERRUPT_HANDLER_FAILED,
                        "Interrupt thread with id " + number + " failed!",
                        failure.failureName
                    ),
                    time
                );
        } else if (failureBeingHandled != null) {
            getParent()
                .failWithoutGrace(
                    new Failure(
                        TaskResultCodePb.EXCEPTION_HANDLER_FAILED,
                        "Interrupt thread with id " + number + " failed!",
                        failure.failureName
                    ),
                    time
                );
        }
    }

    public void failWithoutGrace(Failure failure, Date time) {
        dieForReal(failure, time);
    }

    public void complete(Date time) {
        this.resultCode = TaskResultCodePb.SUCCESS;
        this.errorMessage = null;
        status = LHStatusPb.COMPLETED;
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
                    TaskResultCodePb.VAR_MUTATION_ERROR,
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
    }

    public void advanceFrom(Node curNode) {
        Node nextNode = null;
        for (Edge e : curNode.outgoingEdges) {
            try {
                if (evaluateEdge(e)) {
                    nextNode = e.getSinkNode();
                    break;
                }
            } catch (LHVarSubError exn) {
                LHUtil.log(
                    "Failing threadrun due to VarSubError",
                    wfRun.id,
                    currentNodePosition
                );
                fail(
                    new Failure(
                        TaskResultCodePb.VAR_MUTATION_ERROR,
                        "Failed evaluating outgoing edge: " + exn.getMessage(),
                        LHConstants.VAR_MUTATION_ERROR
                    ),
                    new Date()
                );
                return;
            }
        }
        if (nextNode == null) {
            throw new RuntimeException(
                "Not possible to have a node with zero activated edges"
            );
        }

        activateNode(nextNode);
    }

    public void activateNode(Node node) {
        Date arrivalTime = new Date();

        currentNodePosition++;
        NodeRun old = getCurrentNodeRun();
        Node oldNode = old == null ? null : old.getNode();

        NodeRun cnr = new NodeRun();
        cnr.threadRun = this;
        cnr.nodeName = node.name;
        cnr.status = LHStatusPb.STARTING;

        cnr.wfRunId = wfRunId;
        cnr.threadRunNumber = number;
        cnr.position = currentNodePosition;
        cnr.threadSpecName = threadSpecName;

        cnr.arrivalTime = arrivalTime;
        cnr.wfSpecId = wfSpecId;
        cnr.nodeName = node.name;

        cnr.position = currentNodePosition;

        if (oldNode == null || old == null) {
            // then it's a start node
            cnr.number = 0;
            cnr.attemptNumber = 0;
        } else if (oldNode.name.equals(node.name)) {
            // Then it's a retry, since we don't allow edges pointing to the same
            // node.
            cnr.number = old.number;
            cnr.attemptNumber = old.attemptNumber + 1;
        } else {
            // Not a retry, so it's a new "number" and attemptNumber is 0.
            cnr.number = old.number + 1;
            cnr.attemptNumber = 0;
        }
        cnr.setSubNodeRun(node.getSubNode().createRun(arrivalTime));

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
                return lhs != null && lhs.equals(rhs);
            case NOT_EQUALS:
                return lhs != null && !lhs.equals(rhs);
            case IN:
                return Comparer.contains(rhs, lhs);
            case NOT_IN:
                return !Comparer.contains(rhs, lhs);
            default:
                throw new RuntimeException(
                    "Unhandled comparison enum " + e.condition.comparator
                );
        }
    }

    @JsonIgnore
    public ThreadRun getParent() {
        if (parentThreadId == null) return null;
        return wfRun.threadRuns.get(parentThreadId);
    }

    // TODO: variable locking may have to consider this.
    public Map<String, VariableValue> assignVarsForNode(TaskNode node)
        throws LHVarSubError {
        Map<String, VariableValue> out = new HashMap<>();
        TaskDef taskDef = node.taskDef;

        for (Map.Entry<String, VariableDef> entry : taskDef.requiredVars.entrySet()) {
            String varName = entry.getKey();
            VariableDef requiredVarDef = entry.getValue();
            VariableAssignment assn = node.variables.get(varName);
            VariableValue val;

            if (assn != null) {
                val = assignVariable(assn);
            } else {
                if (requiredVarDef.defaultValue == null) {
                    throw new LHVarSubError(
                        null,
                        "Variable " +
                        varName +
                        " neither has default value nor assignment."
                    );
                }
                val = requiredVarDef.defaultValue;
            }
            if (val.type != requiredVarDef.type) {
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
            out.put(varName, val);
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
            mut.execute(this, varCache, nodeOutput);
        }

        // If we got this far without a LHVarSubError, then we can safely save all
        // of the variables.
        for (Map.Entry<String, VariableValue> entry : varCache.entrySet()) {
            // TODO: This needs to be extended once we add Threads.
            putVariable(entry.getKey(), entry.getValue());
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
        switch (assn.rhsSourceType) {
            case LITERAL_VALUE:
                val = assn.rhsLiteralValue;
                break;
            case VARIABLE_NAME:
                val = getVariable(assn.rhsVariableName).value;

                if (val == null) {
                    throw new LHVarSubError(
                        null,
                        "Variable " + assn.rhsVariableName + " not in scope!"
                    );
                }

                break;
            case SOURCE_NOT_SET:
            default:
                throw new RuntimeException("Not possible");
        }

        if (assn.jsonPath != null) {
            val = val.jsonPath(assn.jsonPath);
        }

        return val;
    }

    public void putNodeRun(NodeRun task) {
        wfRun.stores.putNodeRun(task);
    }

    public NodeRun getNodeRun(int position) {
        NodeRun out = wfRun.stores.getNodeRun(number, position);
        if (out != null) {
            out.threadRun = this;
        }
        return out;
    }

    @JsonIgnore
    public Set<String> getLocallyDefinedVarNames() {
        return getThreadSpec().variableDefs.keySet();
    }

    public void putVariable(String varName, VariableValue var) throws LHVarSubError {
        if (getLocallyDefinedVarNames().contains(varName)) {
            wfRun.stores.putVariable(varName, var, number);
        } else {
            if (getParent() != null) {
                getParent().putVariable(varName, var);
            } else {
                throw new LHVarSubError(
                    null,
                    "Tried to save out-of-scope var " + varName
                );
            }
        }
    }

    public Variable getVariable(String varName) {
        // For now, just do the local one
        // Once we have threads, this will do a backtrack up the thread tree.
        Variable out = wfRun.stores.getVariable(varName, this.number);
        if (out != null) {
            return out;
        }
        if (getParent() != null) {
            return getParent().getVariable(varName);
        }

        return null;
    }
}

class Comparer {

    @SuppressWarnings("all") // lol
    public static int compare(VariableValue left, VariableValue right)
        throws LHVarSubError {
        try {
            int result =
                ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());
            return result;
        } catch (Exception exn) {
            LHUtil.log(exn.getMessage());
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

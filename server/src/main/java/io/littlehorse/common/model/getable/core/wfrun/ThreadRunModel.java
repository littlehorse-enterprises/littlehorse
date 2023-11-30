package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
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
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.EdgeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureHandlerDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.ExitNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.InterruptDefModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableMutationModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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
    private ProcessorExecutionContext processorContext;

    public ThreadRunModel() {}

    public ThreadRunModel(ProcessorExecutionContext processorContext) {
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
            thr.threadRunModel = this;
            haltReasons.add(thr);
        }

        if (proto.hasFailureBeingHandled()) {
            failureBeingHandled = FailureBeingHandledModel.fromProto(proto.getFailureBeingHandled(), context);
        }

        for (int handledFailedChildId : proto.getHandledFailedChildrenList()) {
            handledFailedChildren.add(handledFailedChildId);
        }
        executionContext = context;
        processorContext = context.castOnSupport(ProcessorExecutionContext.class);
        type = proto.getType();
    }

    public ThreadRun.Builder toProto() {
        ThreadRun.Builder out = ThreadRun.newBuilder()
                .setNumber(number)
                .setStatus(status)
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

    public Class<ThreadRun> getProtoBaseClass() {
        return ThreadRun.class;
    }

    // For Scheduler

    public WfRunModel wfRun;

    private ThreadSpecModel threadSpecModel;

    public ThreadSpecModel getThreadSpecModel() {
        if (threadSpecModel == null) {
            threadSpecModel = wfRun.getWfSpec().threadSpecs.get(threadSpecName);
        }
        return threadSpecModel;
    }

    public NodeModel getCurrentNode() {
        NodeRunModel currRun = getCurrentNodeRun();
        ThreadSpecModel t = getThreadSpecModel();
        if (currRun == null) {
            return t.nodes.get(t.entrypointNodeName);
        }

        return t.nodes.get(currRun.nodeName);
    }

    public NodeRunModel getCurrentNodeRun() {
        return getNodeRun(currentNodePosition);
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
        ExternalEventDefIdModel extEvtId = e.getId().getExternalEventDefId();
        InterruptDefModel idef = getThreadSpecModel().getInterruptDefFor(extEvtId.getName());
        if (idef != null) {
            // trigger interrupt
            initializeInterrupt(e, idef);
        }
    }

    public void processExtEvtTimeout(ExternalEventTimeoutModel timeout) {
        NodeRunModel nr = getNodeRun(timeout.getNodeRunId().getPosition());
        if (nr.type != NodeTypeCase.EXTERNAL_EVENT) {
            log.error("Impossible: got a misconfigured external event timeout: {}", nr.toJson());
            return;
        }
        nr.externalEventRun.processExternalEventTimeout(timeout);
    }

    public void processSleepNodeMatured(SleepNodeMaturedModel e) {
        NodeRunModel nr = getNodeRun(e.getNodeRunId().getPosition());
        if (nr.type != NodeTypeCase.SLEEP) {
            log.warn("Tried to mature on non-sleep node");
            // TODO: how do we wanna handle exceptions?
            return;
        }

        nr.sleepNodeRun.processSleepNodeMatured(e);
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
        thr.threadRunModel = this;
        thr.type = ReasonCase.INTERRUPTED;
        thr.interrupted = new InterruptedModel();
        thr.interrupted.interruptThreadId = handlerThreadId;

        childThreadIds.add(handlerThreadId);

        haltReasons.add(thr);
    }

    private void initializeInterrupt(ExternalEventModel trigger, InterruptDefModel idef) {
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

    public void halt(ThreadHaltReasonModel reason) {
        reason.threadRunModel = this;
        switch (status) {
            case COMPLETED:
            case EXCEPTION:
            case ERROR:
                // Already terminated, ignoring halt
                return;
            case STARTING:
            case RUNNING:
            case HALTING:
                if (canBeInterrupted()) {
                    setStatus(LHStatus.HALTED);
                } else {
                    setStatus(LHStatus.HALTING);
                }
                break;
            case HALTED:
                setStatus(LHStatus.HALTED);
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }

        // if we got this far, then we know that we are still running. Add the
        // halt reason.
        haltReasons.add(reason);

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
                    log.debug("Not halting sibling interrupt thread! This will change, in future" + " release.");
                }
            } else {
                child.halt(childHaltReason);
            }
        }

        getCurrentNodeRun().halt();
    }

    /*
     * Checks if the status can be changed. Returns true if status did change.
     */
    public boolean updateStatus() {
        if (status == LHStatus.COMPLETED || status == LHStatus.ERROR) {
            return false;
        } else if (status == LHStatus.RUNNING) {
            return false;
        } else if (status == LHStatus.HALTED) {
            // determine if halt reasons are resolved or not.

            // This is where ThreadRun's wake up for example when an exception handler
            // completes.
            for (int i = haltReasons.size() - 1; i >= 0; i--) {
                ThreadHaltReasonModel hr = haltReasons.get(i);
                if (hr.isResolved()) {
                    haltReasons.remove(i);
                    log.debug(
                            "Removed haltReason {} on thread {} {}, leaving: {}",
                            hr,
                            wfRun.getId(),
                            number,
                            haltReasons);
                }
            }
            if (haltReasons.isEmpty()) {
                log.debug("Thread {} is alive again!", number);
                if (getCurrentNodeRun().getLatestFailure() == null
                        || getCurrentNodeRun().getLatestFailure().isProperlyHandled()) {
                    setStatus(LHStatus.RUNNING);
                } else {
                    setStatus(getCurrentNodeRun().getLatestFailure().getStatus());
                }
                return true;
            } else {
                return false;
            }
        } else if (status == LHStatus.HALTING) {
            if (getCurrentNodeRun().canBeInterrupted()) {
                setStatus(LHStatus.HALTED);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void setStatus(LHStatus status) {
        this.status = status;
    }

    /*
     * Returns true if we can move this Thread from HALTING to HALTED status.
     */

    public boolean canBeInterrupted() {
        if (getCurrentNodeRun().canBeInterrupted()) return true;

        for (int childId : childThreadIds) {
            if (wfRun.getThreadRun(childId).isRunning()) {
                return false;
            }
        }
        return true;
    }

    /*
     * Returns true if this thread is in a dynamic (running) state.
     */

    public boolean isRunning() {
        return (status == LHStatus.RUNNING || status == LHStatus.STARTING || status == LHStatus.HALTING);
    }

    public boolean advance(Date eventTime) {
        NodeRunModel currentNodeRunModel = getCurrentNodeRun();

        if (status == LHStatus.RUNNING) {
            // Just advance the node. Not fancy.
            return currentNodeRunModel.advanceIfPossible(eventTime);
        } else if (status == LHStatus.HALTED) {
            // This means we just need to wait until advance() is called again
            // after Thread Resumption

            log.info("Tried to advance HALTED thread. Doing nothing.");
            return false;
        } else if (status == LHStatus.HALTING) {
            log.info("Tried to advance HALTING thread, checking if halted yet.");

            if (currentNodeRunModel.canBeInterrupted()) {
                setStatus(LHStatus.HALTED);
                log.info("Moving thread to HALTED");
                return true;
            } else {
                return false;
            }
        } else if (status == LHStatus.COMPLETED) {
            // Nothing to do, this is likely an innocuous event.
            return false;
        } else if (status == LHStatus.ERROR || status == LHStatus.EXCEPTION) {
            // This is innocuous. Occurs when a timeout event comes in after
            // a thread fails or completes. Nothing to do.

            return false;
        } else if (status == LHStatus.STARTING) {
            setStatus(LHStatus.RUNNING);
            return currentNodeRunModel.advanceIfPossible(eventTime);
        } else {
            throw new RuntimeException("Unrecognized status: " + status);
        }
    }

    public void fail(FailureModel failure, Date time) {
        // First determine if the node that was failed has a relevant exception
        // handler attached.

        NodeModel curNode = getCurrentNode();
        FailureHandlerDefModel handler = null;

        for (FailureHandlerDefModel candidate : curNode.failureHandlers) {
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

    private void handleFailure(FailureModel failure, FailureHandlerDefModel handler) {
        PendingFailureHandlerModel pfh = new PendingFailureHandlerModel();
        pfh.failedThreadRun = this.number;
        pfh.handlerSpecName = handler.handlerSpecName;

        wfRun.pendingFailures.add(pfh);

        ThreadHaltReasonModel haltReason = new ThreadHaltReasonModel();
        haltReason.type = ReasonCase.PENDING_FAILURE;
        haltReason.pendingFailure = new PendingFailureHandlerHaltReasonModel();
        haltReason.pendingFailure.nodeRunPosition = currentNodePosition;

        // This also stops the children
        halt(haltReason);
        getWfRun().advance(processorContext.currentCommand().getTime());
    }

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
        thr.threadRunModel = this;
        thr.type = ReasonCase.HANDLING_FAILURE;
        thr.handlingFailure = new HandlingFailureHaltReasonModel();
        thr.handlingFailure.handlerThreadId = handlerThreadNumber;

        childThreadIds.add(handlerThreadNumber);
        haltReasons.add(thr);
    }

    public void dieForReal(FailureModel failure, Date time) {
        this.errorMessage = failure.message;
        this.status = failure.getStatus();
        this.endTime = time;

        for (int childId : childThreadIds) {
            ThreadRunModel child = wfRun.getThreadRun(childId);
            ThreadHaltReasonModel hr = new ThreadHaltReasonModel();
            hr.type = ReasonCase.PARENT_HALTED;
            hr.parentHalted = new ParentHaltedModel();
            hr.parentHalted.parentThreadId = number;
            child.halt(hr);
            if (child.getCurrentNodeRun().isInProgress()) {
                child.getCurrentNodeRun().halt();
            }
        }

        if (interruptTriggerId != null) {
            // then we're an interrupt thread and need to fail the parent.

            getParent() // guaranteed not to be null in this case
                    .failWithoutGrace(
                            new FailureModel("Interrupt thread with id " + number + " failed!", failure.failureName),
                            time);
        } else if (failureBeingHandled != null) {
            getParent().failWithoutGrace(failure, time);
        }

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void failWithoutGrace(FailureModel failure, Date time) {
        dieForReal(failure, time);
    }

    public void complete(Date time) {
        this.errorMessage = null;
        setStatus(LHStatus.COMPLETED);
        endTime = time;

        wfRun.handleThreadStatus(number, new Date(), status);
    }

    public void completeCurrentNode(VariableValueModel output, Date eventTime) {
        NodeRunModel crn = getCurrentNodeRun();
        crn.status = LHStatus.COMPLETED;
        try {
            mutateVariables(output);
        } catch (LHVarSubError exn) {
            fail(
                    new FailureModel("Failed mutating variables: " + exn.getMessage(), LHConstants.VAR_MUTATION_ERROR),
                    eventTime);
            return;
        }

        if (status == LHStatus.RUNNING) {
            // If we got here, then we're good.
            advanceFrom(getCurrentNode());
        }
        getWfRun().advance(eventTime);
    }

    public void advanceFrom(NodeModel curNode) {
        if (curNode.getSubNode().getClass().equals(ExitNodeModel.class)) {
            return;
        }
        NodeModel nextNode = null;
        for (EdgeModel e : curNode.outgoingEdges) {
            try {
                if (evaluateEdge(e)) {
                    nextNode = e.getSinkNode();
                    break;
                }
            } catch (LHVarSubError exn) {
                log.error("Failing threadrun due to VarSubError {} {}", wfRun.getId(), currentNodePosition, exn);
                fail(
                        new FailureModel(
                                "Failed evaluating outgoing edge: " + exn.getMessage(), LHConstants.VAR_MUTATION_ERROR),
                        new Date());
                return;
            }
        }
        if (nextNode == null) {
            // TODO: Later versions should validate wfSpec's so that this is not
            // possible
            fail(
                    new FailureModel(
                            "WfSpec was invalid. There were no activated outgoing edges" + " from a non-exit node.",
                            LHConstants.INTERNAL_ERROR),
                    new Date());
        } else {
            activateNode(nextNode);
        }
    }

    public void activateNode(NodeModel node) {
        Date arrivalTime = new Date();

        currentNodePosition++;

        NodeRunModel cnr = new NodeRunModel(processorContext);
        cnr.setThreadRun(this);
        cnr.nodeName = node.name;
        cnr.status = LHStatus.STARTING;
        cnr.setId(new NodeRunIdModel(wfRun.getId(), number, currentNodePosition));
        cnr.setWfSpecId(wfSpecId);
        cnr.setThreadSpecName(threadSpecName);

        cnr.arrivalTime = arrivalTime;

        cnr.setSubNodeRun(node.getSubNode().createSubNodeRun(arrivalTime));

        putNodeRun(cnr);

        cnr.getSubNodeRun().arrive(arrivalTime);
        cnr.getSubNodeRun().advanceIfPossible(arrivalTime);
    }

    private boolean evaluateEdge(EdgeModel e) throws LHVarSubError {
        if (e.condition == null) {
            return true;
        }

        VariableValueModel lhs = assignVariable(e.condition.left);
        VariableValueModel rhs = assignVariable(e.condition.right);

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
        throw new RuntimeException("Unhandled comparison enum " + e.condition.comparator);
    }

    public ThreadRunModel getParent() {
        if (parentThreadId == null) return null;
        return wfRun.getThreadRun(parentThreadId);
    }

    public List<VarNameAndValModel> assignVarsForNode(TaskNodeModel node) throws LHVarSubError {
        List<VarNameAndValModel> out = new ArrayList<>();
        TaskDefModel taskDef = node.getTaskDef();

        if (taskDef.inputVars.size() != node.getVariables().size()) {
            throw new LHVarSubError(null, "Impossible: got different number of taskdef vars and node input vars");
        }

        for (int i = 0; i < taskDef.inputVars.size(); i++) {
            VariableDefModel requiredVarDef = taskDef.inputVars.get(i);
            VariableAssignmentModel assn = node.getVariables().get(i);
            String varName = requiredVarDef.getName();
            VariableValueModel val;

            if (assn != null) {
                val = assignVariable(assn);
            } else {
                throw new LHVarSubError(null, "Variable " + varName + " is unassigned.");
            }
            if (val.type != requiredVarDef.getType() && val.type != VariableType.NULL) {
                throw new LHVarSubError(
                        null,
                        "Variable " + varName + " should be " + requiredVarDef.getType() + " but is of type "
                                + val.getType());
            }
            out.add(new VarNameAndValModel(varName, val));
        }
        return out;
    }

    private void mutateVariables(VariableValueModel nodeOutput) throws LHVarSubError {
        NodeModel node = getCurrentNode();

        // Need to do this atomically in a transaction, so that if one of the
        // mutations fail then none of them occur.
        // That's why we write to an in-memory Map. If all mutations succeed,
        // then we flush the contents of the Map to the Variables.
        Map<String, VariableValueModel> varCache = new HashMap<>();
        for (VariableMutationModel mut : node.variableMutations) {
            try {
                mut.execute(this, varCache, nodeOutput);
            } catch (LHVarSubError exn) {
                log.error(exn.getMessage(), exn);
                exn.addPrefix("Mutating variable " + mut.lhsName + " with operation " + mut.operation);
                throw exn;
            }
        }

        // If we got this far without a LHVarSubError, then we can safely save all
        // of the variables.
        for (Map.Entry<String, VariableValueModel> entry : varCache.entrySet()) {
            // this method saves the variable into the appropriate ThreadRun,
            // respecting the fact that child ThreadRun's can access their
            // parents' variables.
            mutateVariable(entry.getKey(), entry.getValue());
        }
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
                val = new VariableValueModel();
                val.type = VariableType.STR;
                try {
                    val.strVal = MessageFormat.format(formatStringVarVal.strVal, formatArgs.toArray(new Object[0]));
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
        if (getThreadSpecModel().localGetVarDef(varName) != null) {
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
     * Creates a new variable on the current ThreadRun or any of its parents
     * depending on who has
     * the variable on its definition
     *
     * @param varName name of the variable
     * @param var     value of the variable
     * @throws LHVarSubError when the varName is not found either on the current
     *                       ThreadRun
     *                       definition or its parents definition
     */
    public void createVariable(String varName, VariableValueModel var) throws LHVarSubError {
        VariableMutator createVariable = (wfRunId, threadRunNumber, wfRun) -> {
            VariableModel variable = new VariableModel(varName, var, wfRunId, threadRunNumber, wfRun.getWfSpec());
            processorContext.getableManager().put(variable);
        };
        applyVarMutationOnAppropriateThread(varName, createVariable);
    }

    /**
     * Mutates an existing variable on the current ThreadRun or any of its parents
     * depending on who
     * has the variable on its definition
     *
     * @param varName name of the variable
     * @param var     value of the variable
     * @throws LHVarSubError when the varName is not found either on the current
     *                       ThreadRun
     *                       definition or its parents definition
     */
    public void mutateVariable(String varName, VariableValueModel var) throws LHVarSubError {
        VariableMutator mutateVariable = (wfRunId, threadRunNumber, wfRun) -> {
            VariableModel variable =
                    processorContext.getableManager().get(new VariableIdModel(wfRunId, threadRunNumber, varName));
            variable.setValue(var);
            processorContext.getableManager().put(variable);
        };
        applyVarMutationOnAppropriateThread(varName, mutateVariable);
    }

    public VariableModel getVariable(String varName) {
        // For now, just do the local one
        // Once we have threads, this will do a backtrack up the thread tree.
        VariableModel out =
                processorContext.getableManager().get(new VariableIdModel(wfRun.getId(), this.number, varName));
        if (out != null) {
            return out;
        }
        if (getParent() != null) {
            return getParent().getVariable(varName);
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

// TODO: Shouldn't we to move this class to its own file?
@Slf4j
class Comparer {

    @SuppressWarnings("all") // lol
    public static int compare(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        try {
            if (left.getVal() == null && right.getVal() != null) return -1;
            if (right.getVal() == null && left.getVal() != null) return 1;
            if (right.getVal() == null && left.getVal() == null) return 0;

            int result = ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());
            return result;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new LHVarSubError(exn, "Failed comparing the provided values.");
        }
    }

    public static boolean contains(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        // Can only do for Str, Arr, and Obj

        if (left.type == VariableType.STR) {
            String rStr = right.asStr().strVal;

            return left.asStr().strVal.contains(rStr);
        } else if (left.type == VariableType.JSON_ARR) {
            Object rObj = right.getVal();
            List<Object> lhs = left.asArr().jsonArrVal;

            for (Object o : lhs) {
                if (LHUtil.deepEquals(o, rObj)) {
                    return true;
                }
            }
            return false;
        } else if (left.type == VariableType.JSON_OBJ) {
            return left.asObj().jsonObjVal.containsKey(right.asStr().strVal);
        } else {
            throw new LHVarSubError(null, "Can't do CONTAINS on " + left.type);
        }
    }
}

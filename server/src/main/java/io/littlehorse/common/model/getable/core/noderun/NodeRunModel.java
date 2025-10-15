package io.littlehorse.common.model.getable.core.noderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExitRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.RunChildWfNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.SleepNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartMultipleThreadsRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.StartThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.TaskNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ThrowEventNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.UserTaskNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForChildWfNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForConditionNodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.WaitForThreadsRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.EdgeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Node.NodeCase;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class NodeRunModel extends CoreGetable<NodeRun> {

    private NodeRunIdModel id;
    private WfSpecIdModel wfSpecId;
    private String threadSpecName;
    private LHStatus status;
    private Date arrivalTime;
    private Date endTime;
    private String nodeName;
    private String errorMessage;
    private List<FailureModel> failures = new ArrayList<>();
    private List<Integer> failureHandlerIds = new ArrayList<>();

    private NodeTypeCase type;
    private ExternalEventNodeRunModel externalEventRun;
    private TaskNodeRunModel taskRun;
    private ExitRunModel exitRun;
    private EntrypointRunModel entrypointRun;
    private StartThreadRunModel startThreadRun;
    private StartMultipleThreadsRunModel startMultipleThreadsRun;
    private WaitForThreadsRunModel waitForThreadsRun;
    private SleepNodeRunModel sleepNodeRun;
    private UserTaskNodeRunModel userTaskRun;
    private ThrowEventNodeRunModel throwEventNodeRun;
    private WaitForConditionNodeRunModel waitForConditionNodeRun;
    private RunChildWfNodeRunModel runChildWfNodeRun;
    private WaitForChildWfNodeRunModel waitForChildWfNodeRun;

    private ExecutionContext executionContext;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    // Use `NodeRunModel#getThreadRun()`, as this field is lazy-loaded.
    private ThreadRunModel threadRunDoNotUseMe;

    public NodeRunModel() {}

    public NodeRunModel(CoreProcessorContext processorContext) {
        this.executionContext = processorContext;
    }

    @Override
    public Class<NodeRun> getProtoBaseClass() {
        return NodeRun.class;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        NodeRun proto = (NodeRun) p;
        id = LHSerializable.fromProto(proto.getId(), NodeRunIdModel.class, context);

        arrivalTime = LHUtil.fromProtoTs(proto.getArrivalTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        wfSpecId = LHSerializable.fromProto(proto.getWfSpecId(), WfSpecIdModel.class, context);
        threadSpecName = proto.getThreadSpecName();
        nodeName = proto.getNodeName();
        status = proto.getStatus();

        if (proto.hasErrorMessage()) errorMessage = proto.getErrorMessage();

        type = proto.getNodeTypeCase();
        switch (type) {
            case TASK:
                taskRun = LHSerializable.fromProto(proto.getTask(), TaskNodeRunModel.class, context);
                break;
            case EXTERNAL_EVENT:
                externalEventRun = ExternalEventNodeRunModel.fromProto(proto.getExternalEvent(), context);
                break;
            case EXIT:
                exitRun = ExitRunModel.fromProto(proto.getExit(), context);
                break;
            case ENTRYPOINT:
                entrypointRun = EntrypointRunModel.fromProto(proto.getEntrypoint(), context);
                break;
            case START_THREAD:
                startThreadRun = StartThreadRunModel.fromProto(proto.getStartThread(), context);
                break;
            case WAIT_FOR_THREADS:
                waitForThreadsRun = WaitForThreadsRunModel.fromProto(proto.getWaitForThreads(), context);
                break;
            case SLEEP:
                sleepNodeRun = SleepNodeRunModel.fromProto(proto.getSleep(), context);
                break;
            case USER_TASK:
                userTaskRun = LHSerializable.fromProto(proto.getUserTask(), UserTaskNodeRunModel.class, context);
                break;
            case START_MULTIPLE_THREADS:
                startMultipleThreadsRun = LHSerializable.fromProto(
                        proto.getStartMultipleThreads(), StartMultipleThreadsRunModel.class, context);
                break;
            case THROW_EVENT:
                throwEventNodeRun =
                        LHSerializable.fromProto(proto.getThrowEvent(), ThrowEventNodeRunModel.class, context);
                break;
            case WAIT_FOR_CONDITION:
                waitForConditionNodeRun = LHSerializable.fromProto(
                        proto.getWaitForCondition(), WaitForConditionNodeRunModel.class, context);
                break;
            case RUN_CHILD_WF:
                this.runChildWfNodeRun =
                        LHSerializable.fromProto(proto.getRunChildWf(), RunChildWfNodeRunModel.class, context);
                break;
            case WAIT_FOR_CHILD_WF:
                this.waitForChildWfNodeRun =
                        LHSerializable.fromProto(proto.getWaitForChildWf(), WaitForChildWfNodeRunModel.class, context);
                break;
            case NODETYPE_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        for (Failure failure : proto.getFailuresList()) {
            failures.add(FailureModel.fromProto(failure, context));
        }
        for (int handlerId : proto.getFailureHandlerIdsList()) {
            failureHandlerIds.add(handlerId);
        }
        this.executionContext = context;
        getSubNodeRun().setNodeRun(this);
    }

    @Override
    public Date getCreatedAt() {
        return arrivalTime;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        if (externalEventRun != null && externalEventRun.getExternalEventDefId() != null) {
            GetableIndex<? extends AbstractGetable<?>> externalEventIndex = new GetableIndex<>(
                    List.of(
                            Pair.of("status", GetableIndex.ValueType.SINGLE),
                            Pair.of("extEvtDefName", GetableIndex.ValueType.SINGLE)),
                    Optional.of(TagStorageType.LOCAL));
            return List.of(externalEventIndex);
        }
        return List.of();
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "status" -> {
                return List.of(new IndexedField(key, this.getStatus().toString(), TagStorageType.LOCAL));
            }
            case "extEvtDefName" -> {
                if (externalEventRun != null && externalEventRun.getExternalEventDefId() != null) {
                    String externalEventName =
                            externalEventRun.getExternalEventDefId().toString();
                    return List.of(new IndexedField(key, externalEventName, TagStorageType.LOCAL));
                }
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }

    @Override
    public NodeRun.Builder toProto() {
        NodeRun.Builder out = NodeRun.newBuilder()
                .setId(id.toProto())
                .setStatus(status)
                .setArrivalTime(LHUtil.fromDate(arrivalTime))
                .setWfSpecId(wfSpecId.toProto())
                .setThreadSpecName(threadSpecName)
                .setNodeName(nodeName);

        if (endTime != null) out.setEndTime(LHUtil.fromDate(endTime));

        if (errorMessage != null) out.setErrorMessage(errorMessage);

        switch (type) {
            case TASK:
                out.setTask(taskRun.toProto());
                break;
            case EXTERNAL_EVENT:
                out.setExternalEvent(externalEventRun.toProto());
                break;
            case ENTRYPOINT:
                out.setEntrypoint(entrypointRun.toProto());
                break;
            case EXIT:
                out.setExit(exitRun.toProto());
                break;
            case START_THREAD:
                out.setStartThread(startThreadRun.toProto());
                break;
            case WAIT_FOR_THREADS:
                out.setWaitForThreads(waitForThreadsRun.toProto());
                break;
            case SLEEP:
                out.setSleep(sleepNodeRun.toProto());
                break;
            case USER_TASK:
                out.setUserTask(userTaskRun.toProto());
                break;
            case START_MULTIPLE_THREADS:
                out.setStartMultipleThreads(startMultipleThreadsRun.toProto());
                break;
            case THROW_EVENT:
                out.setThrowEvent(throwEventNodeRun.toProto());
                break;
            case WAIT_FOR_CONDITION:
                out.setWaitForCondition(waitForConditionNodeRun.toProto());
                break;
            case RUN_CHILD_WF:
                out.setRunChildWf(runChildWfNodeRun.toProto());
                break;
            case WAIT_FOR_CHILD_WF:
                out.setWaitForChildWf(waitForChildWfNodeRun.toProto());
                break;
            case NODETYPE_NOT_SET:
        }

        for (FailureModel failure : failures) {
            out.addFailures(failure.toProto());
        }
        for (Integer id : failureHandlerIds) {
            out.addFailureHandlerIds(id);
        }

        return out;
    }

    @Override
    public NodeRunIdModel getObjectId() {
        return id;
    }

    /**
     * A SubNodeRun is the sub-field of a NodeRun. This method returns the
     * appropriate one
     * from this NodeRun.
     *
     * @return the SubNodeRun for this NodeRun.
     */
    public SubNodeRun<?> getSubNodeRun() {
        switch (type) {
            case TASK:
                return taskRun;
            case EXTERNAL_EVENT:
                return externalEventRun;
            case ENTRYPOINT:
                return entrypointRun;
            case EXIT:
                return exitRun;
            case WAIT_FOR_THREADS:
                return waitForThreadsRun;
            case START_THREAD:
                return startThreadRun;
            case SLEEP:
                return sleepNodeRun;
            case USER_TASK:
                return userTaskRun;
            case START_MULTIPLE_THREADS:
                return startMultipleThreadsRun;
            case THROW_EVENT:
                return throwEventNodeRun;
            case WAIT_FOR_CONDITION:
                return waitForConditionNodeRun;
            case RUN_CHILD_WF:
                return runChildWfNodeRun;
            case WAIT_FOR_CHILD_WF:
                return waitForChildWfNodeRun;
            case NODETYPE_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    /**
     * Sets the SubNodeRun of this NodeRun. This will also set the type of the
     * NodeRun.
     *
     * Called during initialization; eg. when the ThreadRunModel activates a new
     * Node on the ThreadRun.
     *
     * @param subNodeRun is the SubNodeRun to assign for this NodeRunModel.
     */
    public void setSubNodeRun(SubNodeRun<?> subNodeRun) {
        Class<?> cls = subNodeRun.getClass();
        if (cls.equals(TaskNodeRunModel.class)) {
            type = NodeTypeCase.TASK;
            taskRun = (TaskNodeRunModel) subNodeRun;
        } else if (cls.equals(EntrypointRunModel.class)) {
            type = NodeTypeCase.ENTRYPOINT;
            entrypointRun = (EntrypointRunModel) subNodeRun;
        } else if (cls.equals(ExitRunModel.class)) {
            type = NodeTypeCase.EXIT;
            exitRun = (ExitRunModel) subNodeRun;
        } else if (cls.equals(ExternalEventNodeRunModel.class)) {
            type = NodeTypeCase.EXTERNAL_EVENT;
            externalEventRun = (ExternalEventNodeRunModel) subNodeRun;
        } else if (cls.equals(StartThreadRunModel.class)) {
            type = NodeTypeCase.START_THREAD;
            startThreadRun = (StartThreadRunModel) subNodeRun;
        } else if (cls.equals(WaitForThreadsRunModel.class)) {
            type = NodeTypeCase.WAIT_FOR_THREADS;
            waitForThreadsRun = (WaitForThreadsRunModel) subNodeRun;
        } else if (cls.equals(SleepNodeRunModel.class)) {
            type = NodeTypeCase.SLEEP;
            sleepNodeRun = (SleepNodeRunModel) subNodeRun;
        } else if (cls.equals(UserTaskNodeRunModel.class)) {
            type = NodeTypeCase.USER_TASK;
            userTaskRun = (UserTaskNodeRunModel) subNodeRun;
        } else if (cls.equals(StartMultipleThreadsRunModel.class)) {
            type = NodeTypeCase.START_MULTIPLE_THREADS;
            startMultipleThreadsRun = (StartMultipleThreadsRunModel) subNodeRun;
        } else if (cls.equals(ThrowEventNodeRunModel.class)) {
            type = NodeTypeCase.THROW_EVENT;
            throwEventNodeRun = (ThrowEventNodeRunModel) subNodeRun;
        } else if (cls.equals(WaitForConditionNodeRunModel.class)) {
            type = NodeTypeCase.WAIT_FOR_CONDITION;
            waitForConditionNodeRun = (WaitForConditionNodeRunModel) subNodeRun;
        } else if (cls.equals(RunChildWfNodeRunModel.class)) {
            type = NodeTypeCase.RUN_CHILD_WF;
            this.runChildWfNodeRun = (RunChildWfNodeRunModel) subNodeRun;
        } else if (cls.equals(WaitForChildWfNodeRunModel.class)) {
            type = NodeTypeCase.WAIT_FOR_CHILD_WF;
            this.waitForChildWfNodeRun = (WaitForChildWfNodeRunModel) subNodeRun;
        } else {
            throw new RuntimeException("Didn't recognize " + subNodeRun.getClass());
        }

        subNodeRun.nodeRun = this;
    }

    /**
     * Returns the ThreadRunModel representing the ThreadRun that the NodeRun for
     * this NodeRunModel
     * is a part of.
     *
     * Requires a CoreProcessorContext; meaning that this should only be called from
     * within the
     * CommandProcessor execution context.
     *
     * @return the ThreadRunModel for the ThreadRun that this NodeRunModel's NodeRun
     *         belongs to.
     */
    public ThreadRunModel getThreadRun() {
        if (threadRunDoNotUseMe == null) {
            CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
            WfRunModel wfRunModel = processorContext.getableManager().get(id.getWfRunId());
            threadRunDoNotUseMe = wfRunModel.getThreadRun(id.getThreadRunNumber());
        }
        return threadRunDoNotUseMe;
    }

    /**
     * Returns the Id of the ThreadRun that this NodeRunModel's NodeRun belongs to.
     *
     * @return the ID of the ThreadRun that this NodeRunModel's NodeRun belongs to.
     */
    public int getThreadRunNumber() {
        return id.getThreadRunNumber();
    }

    /**
     * Called on initialization/building of a NodeRunModel.
     *
     * @param threadRunModel is the ThreadRunModel.
     */
    public void setThreadRun(ThreadRunModel threadRunModel) {
        threadRunDoNotUseMe = threadRunModel;
    }

    /**
     * Returns the most recent Failure thrown by this NodeRunModel, if there is a
     * Failure.
     *
     * A NodeRun in LittleHorse can have zero or more Failures. For example, a
     * NodeRun can
     * have one Failure, then the Failure Handler completes successfuly, then
     * another Failure
     * may be thrown when evaluating the outgoing edges.
     *
     * @return the most recent Failure thrown by this NodeRunModel.
     */
    public Optional<FailureModel> getLatestFailure() {
        if (failures.size() == 0) return Optional.empty();
        return Optional.of(failures.get(failures.size() - 1));
    }

    /**
     * Returns whether the NodeRun is making progress; i.e. it's
     * starting/running/halting.
     *
     * @return if the NodeRun is in progress.
     */
    public boolean isInProgress() {
        switch (status) {
            case STARTING:
            case RUNNING:
            case HALTING:
                return true;
            case HALTED:
            case COMPLETED:
            case EXCEPTION:
            case ERROR:
            case UNRECOGNIZED:
        }
        return false;
    }

    /*
     * Returns the Node from the ThreadSpec that this NodeRun is running.
     */
    public NodeModel getNode() {
        return getThreadRun().getThreadSpec().nodes.get(nodeName);
    }

    /**
     * Returns the type of the Node.
     *
     * @return the type of the Node.
     */
    public NodeCase getNodeType() {
        return getNode().type;
    }

    /**
     * Checks if the processing performed by this NodeRunModel is completed. If so,
     * then the ThreadRunModel
     * has permission to advance the ThreadRun past this NodeRunModel's NodeRun.
     * Otherwise, the ThreadRunModel
     * must continue waiting at this NodeRunModel.
     *
     * This method may mutate the state of the NodeRun.
     *
     * @return
     */
    public boolean checkIfProcessingCompleted(CoreProcessorContext processorContext) throws NodeFailureException {
        boolean completed;
        try {
            completed = getSubNodeRun().checkIfProcessingCompleted(processorContext);
        } catch (NodeFailureException exn) {
            failures.add(exn.getFailure());
            status = exn.getFailure().getStatus();
            errorMessage = exn.getFailure().getMessage();
            throw exn;
        }

        if (completed) {
            status = LHStatus.COMPLETED;
            endTime = executionContext
                    .castOnSupport(CoreProcessorContext.class)
                    .currentCommand()
                    .getTime();
        }
        return completed;
    }

    public void arrive(Date time, CoreProcessorContext processorContext) throws NodeFailureException {
        try {
            setStatus(LHStatus.RUNNING);
            getSubNodeRun().arrive(time, processorContext);
        } catch (NodeFailureException exn) {
            fail(exn);
            throw exn;
        }
    }

    public void fail(NodeFailureException exn) {
        failures.add(exn.getFailure());
        setStatus(exn.getFailure().getStatus());
    }

    /**
     * In LittleHorse, a NodeRun may return an output. For example, a TASK NodeRun's
     * output is the VariableValue
     * returned by the Task Method invoked during the TaskRun. An EXTERNAL_EVENT
     * NodeRun's output is the content
     * of the ExternalEvent.
     *
     * Not all NodeRun types return an output though; for example, WAIT_FOR_THREADS
     * *currently* does NOT return
     * an output.
     *
     * @precondition the NodeRUnModel should already be completed or recovered from
     *               failure.
     * @return the output from this NodeRunModel's NodeRun, if such output exists.
     */
    public Optional<VariableValueModel> getOutput(CoreProcessorContext processorContext) {
        if (status != LHStatus.COMPLETED) {
            throw new IllegalStateException("Cannot get output from a non-completed NodeRun");
        }

        return getSubNodeRun().getOutput(processorContext);
    }

    /**
     * Halts this NodeRun if possible; otherwise starts the halting process and sets
     * the status
     * to HALTING. Returns true if the NodeRun is successfully HALTED.
     *
     * @return true if the NodeRun is successfully HALTED; else false.
     */
    public boolean maybeHalt(CoreProcessorContext processorContext) {
        if (!isInProgress()) {
            // If the NodeRun is already completed, failed, or halted, then we're done (:
            return true;
        }

        if (getSubNodeRun().maybeHalt(processorContext)) {
            status = LHStatus.HALTED;
            return true;
        } else {
            status = LHStatus.HALTING;
            return false;
        }
    }

    public void unHalt() {
        status = LHStatus.RUNNING;
    }

    /**
     * Returns the WfSpecModel for the WfSpec that this NodeRunModel's NodeRun
     * belongs to. Note
     * that in the case of a WfSpec Version Migration, this might be different than
     * the return
     * value of getWfRun().getWfSpec(). For example, if we call nodeRun.getWfSpec()
     * on an old
     * NodeRun after a WfSpec Version Migration has already occurred, the version of
     * the WfSpec
     * returned could be older than the version of the WfSpec returned by
     * wfRun.getWfSpec().
     *
     * Can only be called in the CommandProcessor execution context.
     *
     * @return the WfSpecModel for the WfSpec of this NodeRunModel's NodeRun
     */
    public WfSpecModel getWfSpec() {
        CoreProcessorContext ctx = executionContext.castOnSupport(CoreProcessorContext.class);
        return ctx.service().getWfSpec(wfSpecId);
    }

    /**
     * Evaluates the outgoing edge, maybe mutates variables, and returns the next
     * Node that the ThreadRun
     * should go to.
     *
     * If the NodeRun had a failure, then VariableMutations do NOT happen (this is
     * part of our public API
     * behavior. See comments in issue #656 on GitHub.).
     *
     * If the evaluation of outgoing edges fails, or the variable mutations fail,
     * then this method adds a
     * Failure to the NodeRun (responsibility of the NodeRunModel) and also throws a
     * NodeFailureException (so
     * that the ThreadRunModel can react appropriately).
     *
     * For several good reasons, Outgoing Edges are a property of the Node proto,
     * not the ThreadSpec proto.
     * Furthermore, Variable Mutations are a property of the Outgoing Edges (and by
     * extension, also the Node).
     * This means that the following are all responsibilities of the NodeRun:
     * - Choosing the next Node to go to (evaluating outgoing edges)
     * - Telling the ThreadRunModel to mutate the variables.
     *
     * If either of those things fail, then the resulting `Failure` is a property of
     * the NodeRun itself.
     *
     * EXIT Node's do NOT have OutgoingEdges, so this method should not be called on
     * the NodeRunModel for
     * an EXIT NodeRun.
     *
     * @precondition the NodeRun succeeded OR its failures were all properly
     *               handled.
     * @postcondition if the NodeRun was successful, variable mutations on the
     *                activated edge are executed.
     * @return the Node that the ThreadSpecModel should advance to next.
     * @throws NodeFailureException if evaluation of outgoing edges fails or if
     *                              variable mutations fail.
     */
    public NodeModel evaluateOutgoingEdgesAndMaybeMutateVariables(CoreProcessorContext processorContext)
            throws NodeFailureException {
        NodeModel currentNode = getNode();
        ThreadRunModel thread = getThreadRun();

        for (EdgeModel edge : currentNode.getOutgoingEdges()) {

            // We can either fail when evaluating the outgoing edge or when mutating
            // the variables. We want to know when the error happens so we can adjust the
            // error message properly.
            try {
                if (edge.isConditionSatisfied(thread)) {
                    // As per GH Issue #656, we do NOT mutate variables if there is a Failure, even
                    // if the Failure is handled. If a user wants to mutate variables anyways, they
                    // should do so in the Failure Handler.
                    if (failures.isEmpty()) {
                        edge.mutateVariables(thread, this.getOutput(processorContext));
                    }

                    // If we get here, we have found an edge that was valid, and the variable
                    // mutations returned successfully. We return to the ThreadRunModel the
                    // WfSpec Node to which this Edge points.
                    return edge.getSinkNode();
                }
            } catch (LHVarSubError exn) {
                FailureModel failure = new FailureModel(
                        "Failed evaluating edge with sink node %s: %s"
                                .formatted(edge.getSinkNodeName(), exn.getMessage()),
                        LHErrorType.VAR_SUB_ERROR.toString());
                failures.add(failure);
                throw new NodeFailureException(failure);
            }
        }

        // If we get this far, it means that none of the Edges had a valid condition.
        // This means that the WfSpec was invalid. This isn't possible if the user uses
        // our SDK's.
        FailureModel invalidWfSpecFailure = new FailureModel(
                "Invalid WfSpec: No outgoing edges found on Node %s with all conditions satisfied".formatted(nodeName),
                LHErrorType.VAR_SUB_ERROR.toString());
        failures.add(invalidWfSpecFailure);
        throw new NodeFailureException(invalidWfSpecFailure);
    }
}

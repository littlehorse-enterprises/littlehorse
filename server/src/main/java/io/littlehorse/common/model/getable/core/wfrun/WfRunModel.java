package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.exceptions.MissingThreadRunException;
import io.littlehorse.common.exceptions.ThreadRunRescueFailedException;
import io.littlehorse.common.exceptions.UnRescuableThreadRunException;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.migration.MigrationVariablesModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.MigrateWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureBeingHandledModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.PendingFailureHandlerModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.ManualHaltModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WorkflowRetentionPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.NodeMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.ThreadMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.migration.WfRunMigrationPlanModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.MigrationPlanIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.sdk.common.proto.PendingFailureHandler;
import io.littlehorse.sdk.common.proto.PendingInterrupt;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.GetableUpdates;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Setter
@Getter
public class WfRunModel extends CoreGetable<WfRun> implements CoreOutputTopicGetable<WfRun> {

    private WfRunIdModel id;
    private WfSpecIdModel wfSpecId;
    private List<WfSpecIdModel> oldWfSpecVersions = new ArrayList<>();
    private int greatestThreadRunNumber;

    public LHStatus status;

    public Date startTime;
    public Date endTime;

    // Using this directly is dangerous; better to use `WfRunModel#getThreadRun()`.
    private List<ThreadRunModel> threadRunsUseMeCarefully = new ArrayList<>();

    public List<PendingInterruptModel> pendingInterrupts = new ArrayList<>();
    public List<PendingFailureHandlerModel> pendingFailures = new ArrayList<>();
    private ExecutionContext executionContext;


    private MigrationPlanIdModel migrationPlanId; 
    // Not in proto
    private int numAdvancesInThisCommand = 0;

    public WfRunModel() {}

    public WfRunModel(CoreProcessorContext processorContext) {
        this.executionContext = processorContext;
    }

    public Date getCreatedAt() {
        return startTime;
    }

    // K -> V
    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        List<GetableIndex<? extends AbstractGetable<?>>> indexes = new ArrayList<>(List.of(
                new GetableIndex<>(
                        List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("wfSpecId", GetableIndex.ValueType.SINGLE)), Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(Pair.of("majorVersion", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("majorVersion", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecId", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL))));

        if (id != null && id.getParentWfRunId() != null) {
            indexes.add(new GetableIndex<>(
                    List.of(Pair.of("parentWfRunId", GetableIndex.ValueType.SINGLE)),
                    Optional.of(TagStorageType.LOCAL)));
            indexes.add(new GetableIndex<>(
                    List.of(
                            Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                            Pair.of("parentWfRunId", GetableIndex.ValueType.SINGLE)),
                    Optional.of(TagStorageType.LOCAL)));
            indexes.add(new GetableIndex<>(
                    List.of(
                            Pair.of("status", GetableIndex.ValueType.SINGLE),
                            Pair.of("parentWfRunId", GetableIndex.ValueType.SINGLE)),
                    Optional.of(TagStorageType.LOCAL)));
            indexes.add(new GetableIndex<>(
                    List.of(
                            Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                            Pair.of("status", GetableIndex.ValueType.SINGLE),
                            Pair.of("parentWfRunId", GetableIndex.ValueType.SINGLE)),
                    Optional.of(TagStorageType.LOCAL)));
        }

        return indexes;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(new IndexedField(key, this.getWfSpecName(), tagStorageType.get()));
            }
            case "status" -> {
                return List.of(new IndexedField(key, this.getStatus().toString(), tagStorageType.get()));
            }
            case "majorVersion" -> {
                return List.of(new IndexedField(
                        key,
                        wfSpecId.getName() + "/" + LHUtil.toLHDbVersionFormat(wfSpecId.getMajorVersion()),
                        TagStorageType.LOCAL));
            }
            case "wfSpecId" -> {
                return List.of(new IndexedField(key, wfSpecId.toString(), TagStorageType.LOCAL));
            }
            case "parentWfRunId" -> {
                if (id.getParentWfRunId() != null) {
                    return List.of(new IndexedField(key, id.getParentWfRunId().toString(), tagStorageType.get()));
                }
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }

    public WfSpecModel getWfSpec() {
        if (wfSpec == null) {
            wfSpec = executionContext.service().getWfSpec(wfSpecId);
        }
        return wfSpec;
    }

    public void setWfSpec(WfSpecModel spec) {
        this.wfSpec = spec;
    }

    public ThreadRunModel getThreadRun(int threadRunNumber) {
        return threadRunsUseMeCarefully.stream()
                .filter(thread -> thread.getNumber() == threadRunNumber)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        WfRun proto = (WfRun) p;
        id = LHSerializable.fromProto(proto.getId(), WfRunIdModel.class, context);
        wfSpecId = LHSerializable.fromProto(proto.getWfSpecId(), WfSpecIdModel.class, context);
        status = proto.getStatus();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());

        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        for (ThreadRun trpb : proto.getThreadRunsList()) {
            ThreadRunModel thr = ThreadRunModel.fromProto(trpb, context);
            thr.wfRun = this;
            threadRunsUseMeCarefully.add(thr);
        }
        for (PendingInterrupt pipb : proto.getPendingInterruptsList()) {
            pendingInterrupts.add(PendingInterruptModel.fromProto(pipb, context));
        }
        for (PendingFailureHandler pfhpb : proto.getPendingFailuresList()) {
            pendingFailures.add(PendingFailureHandlerModel.fromProto(pfhpb, context));
        }

        for (WfSpecId oldWfSpecId : proto.getOldWfSpecVersionsList()) {
            oldWfSpecVersions.add(LHSerializable.fromProto(oldWfSpecId, WfSpecIdModel.class, context));
        }
        this.executionContext = context;
        this.greatestThreadRunNumber = proto.getGreatestThreadrunNumber();
        this.migrationPlanId = LHSerializable.fromProto(proto.getMigrationPlanId(), MigrationPlanIdModel.class, context);
        
    }

    @Override
    public boolean shouldProduceToOutputTopic(
            WfRun previousValue,
            ReadOnlyMetadataManager metadataManager,
            ReadOnlyGetableManager getableManager,
            OutputTopicConfigModel config) {
        if (config.getDefaultRecordingLevel() == OutputTopicRecordingLevel.NO_ENTITY_EVENTS) {
            return false;
        }

        return previousValue == null || previousValue.getStatus() != this.status;
    }

    @Override
    public WfRunIdModel getObjectId() {
        return id;
    }

    /*
     * Returns true if this WfRun is currently running. Due to the inheritance
     * structure of threads, we can determine this by simply checking if the
     * entrypoint thread is running.
     */
    public boolean isRunning() {
        return getThreadRun(0).isRunning();
    }

    @Override
    public WfRun.Builder toProto() {
        WfRun.Builder out = WfRun.newBuilder()
                .setId(id.toProto())
                .setWfSpecId(wfSpecId.toProto())
                .setStatus(status)
                .setStartTime(LHUtil.fromDate(startTime))
                .setGreatestThreadrunNumber(greatestThreadRunNumber);
        if (migrationPlanId != null) {
            out.setMigrationPlanId(migrationPlanId.toProto());
        }

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRunModel threadRunModel : threadRunsUseMeCarefully) {
            out.addThreadRuns(threadRunModel.toProto());
        }

        for (PendingInterruptModel pi : pendingInterrupts) {
            out.addPendingInterrupts(pi.toProto());
        }

        for (PendingFailureHandlerModel pfh : pendingFailures) {
            out.addPendingFailures(pfh.toProto());
        }
        for (WfSpecIdModel oldWfSpec : oldWfSpecVersions) {
            out.addOldWfSpecVersions(oldWfSpec.toProto());
        }

        return out;
    }

    public Class<WfRun> getProtoBaseClass() {
        return WfRun.class;
    }

    // Below is used by scheduler

    private WfSpecModel wfSpec;

    public ThreadRunModel startThread(
            String threadName,
            Date start,
            Integer parentThreadId,
            Map<String, VariableValueModel> variables,
            ThreadType type) {
        CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
        ThreadSpecModel tspec = getWfSpec().getThreadSpecs().get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name, should be impossible");
        }

        ThreadRunModel newThread = new ThreadRunModel(processorContext);
        newThread.parentThreadId = parentThreadId;
        newThread.setWfSpecId(wfSpecId);

        if (parentThreadId == null) {
            // then this is the entrypoint.
            this.greatestThreadRunNumber = 0;
            newThread.number = 0;
        } else {
            this.greatestThreadRunNumber++;
            newThread.number = this.greatestThreadRunNumber;

            ThreadRunModel parent = getThreadRun(parentThreadId);
            parent.getChildThreadIds().add(newThread.getNumber());
        }

        newThread.threadSpecName = threadName;
        newThread.currentNodePosition = -1; // this gets bumped when we start the thread

        newThread.wfRun = this;
        newThread.type = type;
        threadRunsUseMeCarefully.add(newThread);

        newThread.createVariablesAndStart(variables);
        return newThread;
    }

    public String getWfSpecName() {
        return wfSpecId.getName();
    }

    public LHStatus getStatus() {
        return status;
    }

    private boolean startXnHandlersAndInterrupts(Date time) {
        boolean somethingChanged = false;
        somethingChanged = startInterrupts(time) || somethingChanged;
        somethingChanged = startXnHandlers(time) || somethingChanged;
        return somethingChanged;
    }

    private boolean startInterrupts(Date time) {
        CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
        boolean somethingChanged = false;

        // Current server behavior is that only one ThreadRun may be interrupted at a single time. This will be
        // configurable in the `WfSpec` in the future.

        List<PendingInterruptModel> interruptsToLaunchNow = new ArrayList<>();
        Set<Integer> threadsToInterruptNow = new HashSet<>();

        for (int i = pendingInterrupts.size() - 1; i >= 0; i--) {
            PendingInterruptModel pi = pendingInterrupts.get(i);
            ThreadRunModel toInterrupt = getThreadRun(pi.interruptedThreadId);

            if (!threadsToInterruptNow.contains(pi.interruptedThreadId)) {
                if (toInterrupt.maybeFinishHaltingProcess()) {
                    threadsToInterruptNow.add(pi.interruptedThreadId);
                    somethingChanged = true;
                    interruptsToLaunchNow.add(0, pi);
                    pendingInterrupts.remove(i);
                }
            }
        }

        for (PendingInterruptModel pi : interruptsToLaunchNow) {
            ThreadRunModel toInterrupt = getThreadRun(pi.interruptedThreadId);
            Map<String, VariableValueModel> vars;

            ThreadSpecModel iSpec = getWfSpec().getThreadSpecs().get(pi.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                ExternalEventModel event = processorContext.getableManager().get(pi.externalEventId);
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, event.getContent());
            } else {
                vars = new HashMap<>();
            }
            ThreadRunModel interruptor =
                    startThread(pi.handlerSpecName, time, pi.interruptedThreadId, vars, ThreadType.INTERRUPT);
            interruptor.setInterruptTriggerId(pi.externalEventId);

            if (interruptor.status == LHStatus.ERROR) {
                putFailureOnThreadRun(
                        toInterrupt,
                        new FailureModel(
                                "Failed launching interrupt thread with id: " + interruptor.number,
                                LHConstants.CHILD_FAILURE),
                        time,
                        processorContext);
            } else {
                toInterrupt.acknowledgeInterruptStarted(pi, interruptor.number);
            }
        }

        return somethingChanged;
    }

    private boolean startXnHandlers(Date time) {
        boolean somethingChanged = false;

        for (int i = pendingFailures.size() - 1; i >= 0; i--) {
            PendingFailureHandlerModel pfh = pendingFailures.get(i);
            ThreadRunModel failedThr = getThreadRun(pfh.failedThreadRun);

            if (!failedThr.maybeFinishHaltingProcess()) {
                continue;
            }
            somethingChanged = true;
            pendingFailures.remove(i);
            Map<String, VariableValueModel> vars = new HashMap<>();

            ThreadSpecModel iSpec = getWfSpec().getThreadSpecs().get(pfh.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                FailureModel failure =
                        failedThr.getCurrentNodeRun().getLatestFailure().get();
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, failure.content);
            }

            ThreadRunModel fh =
                    startThread(pfh.handlerSpecName, time, pfh.failedThreadRun, vars, ThreadType.FAILURE_HANDLER);

            failedThr.getCurrentNodeRun().getFailureHandlerIds().add(fh.number);

            fh.failureBeingHandled = new FailureBeingHandledModel();
            fh.failureBeingHandled.setFailureNumber(
                    failedThr.getCurrentNodeRun().getFailures().size() - 1);
            fh.failureBeingHandled.setNodeRunPosition(failedThr.currentNodePosition);
            fh.failureBeingHandled.setThreadRunNumber(pfh.failedThreadRun);

            failedThr.getCurrentNodeRun().getLatestFailure().get().setFailureHandlerThreadRunId(fh.getNumber());

            if (fh.status == LHStatus.ERROR) {
                putFailureOnThreadRun(
                        failedThr,
                        new FailureModel(
                                "Failed launching exception handler thread with id: " + fh.number,
                                LHConstants.CHILD_FAILURE),
                        time,
                        executionContext.castOnSupport(CoreProcessorContext.class));
            } else {
                failedThr.acknowledgeXnHandlerStarted(pfh, fh.number);
            }
        }

        return somethingChanged;
    }

    private void putFailureOnThreadRun(
            ThreadRunModel threadToFail, FailureModel failure, Date time, CoreProcessorContext processorContext) {
        NodeRunModel nodeRun = threadToFail.getCurrentNodeRun();
        nodeRun.getFailures().add(failure);
        threadToFail.setStatus(failure.getStatus());
        threadToFail.setErrorMessage(failure.getMessage());
        nodeRun.maybeHalt(processorContext);
    }

    public void advance(Date time) {
        CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
        WfRunMigrationPlanModel migrationPlan = getMigrationPlan(processorContext);
        WfSpecModel migrationTargetSpec = getMigrationTargetSpec(processorContext, migrationPlan);

        boolean statusChanged = true;
        // We repeatedly advance each thread until we have a run wherein the entire
        // WfRun is static, meaning that there are no more advances that can be made
        // without another Command coming in.
        while (statusChanged) {
            if (++numAdvancesInThisCommand > LHConstants.MAX_STACK_FRAMES_PER_COMMAND) {
                putFailureOnThreadRun(
                        getThreadRun(0),
                        new FailureModel(
                                LHErrorType.INTERNAL_ERROR.toString(),
                                "Your WfSpec had a tight loop and exceeded the maximum iterations in one command."),
                        time,
                        null);
                transitionTo(LHStatus.ERROR);
                break;
            }

            statusChanged = startXnHandlersAndInterrupts(time);
            // for (int i = threadRunsUseMeCarefully.size() - 1; i >= 0; i--) {
            for (int i = 0; i < threadRunsUseMeCarefully.size(); i++) {
                ThreadRunModel thread = threadRunsUseMeCarefully.get(i);
                if (migrationPlan != null && migrationTargetSpec != null) {
                    statusChanged = maybeMigrateThread(thread, migrationPlan, migrationTargetSpec, time, processorContext)
                            || statusChanged;
                }
                statusChanged = thread.advance(time) || statusChanged;
            }
        }

        // Now we remove any old threadruns according to the retention policy
        for (int i = threadRunsUseMeCarefully.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRunsUseMeCarefully.get(i);
            ThreadSpecModel spec = thread.getThreadSpec();
            if (spec.getRetentionPolicy() != null) {
                if (spec.getRetentionPolicy().shouldGcThreadRun(thread)) {
                    removeThreadRun(thread);
                }
            }
        }
    }

    private void removeThreadRun(ThreadRunModel thread) {
        if (thread.getNumber() == 0) {
            throw new IllegalStateException("Impossible to cleanup entrypoint threadrun");
        }

        // First, remove the reference to this thread from the parent threadrun.
        ThreadRunModel parent = getThreadRun(thread.getParentThreadId());
        if (parent != null) {
            parent.childThreadIds.removeIf(childId -> childId.equals(thread.getNumber()));
        }

        threadRunsUseMeCarefully.removeIf(candidate -> candidate.getNumber() == thread.getNumber());
    }

    private WfRunMigrationPlanModel getMigrationPlan(CoreProcessorContext context) {
        if (migrationPlanId == null) {
            return null;
        }
        return context.service().getWfRunMigrationPlan(migrationPlanId.getName());
    }

    private WfSpecModel getMigrationTargetSpec(
            CoreProcessorContext context, WfRunMigrationPlanModel migrationPlan) {
        if (migrationPlan == null || migrationPlan.getNewWfSpecId() == null) {
            return null;
        }
        return context.service().getWfSpec(migrationPlan.getNewWfSpecId());
    }

    private boolean maybeMigrateThread(
            ThreadRunModel thread,
            WfRunMigrationPlanModel migrationPlan,
            WfSpecModel newWfSpec,
            Date time,
            CoreProcessorContext processorContext) {
        ThreadMigrationPlanModel threadPlan = migrationPlan.getThreadMigrations().get(thread.getThreadSpecName());
        if (threadPlan == null || threadPlan.getNodeMigrations() == null) {
            return false;
        }
        NodeRunModel currentNodeRun = thread.getCurrentNodeRun();
        if (currentNodeRun == null) {
            return false;
        }
        NodeMigrationPlanModel nodePlan = threadPlan.getNodeMigrations().get(currentNodeRun.getNodeName());
        if (nodePlan == null) {
            return false;
        }

        String newThreadName = threadPlan.getNewThreadName();
        String newNodeName = nodePlan.getNewNode();
        ThreadSpecModel newThreadSpec = newWfSpec.getThreadSpecs().get(newThreadName);
        if (newThreadSpec == null) {
            return false;
        }
        NodeModel newNode = newThreadSpec.getNodes().get(newNodeName);
        if (newNode == null) {
            return false;
        }

//        if (!oldWfSpecVersions.contains(wfSpecId)) {
//           oldWfSpecVersions.add(wfSpecId);
//        }

        thread.setWfSpecId(newWfSpec.getId());
        thread.setThreadSpecName(newThreadName);
        thread.setThreadSpecModel(null);

        currentNodeRun.setWfSpecId(newWfSpec.getId());
        currentNodeRun.setThreadSpecName(newThreadName);
        currentNodeRun.setNodeName(newNodeName);
        currentNodeRun.setSubNodeRun(newNode.getSubNode().createSubNodeRun(time, processorContext));

        return true;
    }

    public void processExtEvtTimeout(ExternalEventTimeoutModel timeout) {
        CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
        ThreadRunModel handler = getThreadRun(timeout.getNodeRunId().getThreadRunNumber());
        handler.processExtEvtTimeout(timeout);
        advance(processorContext.currentCommand().getTime());
    }

    public void processExternalEvent(ExternalEventModel event) {
        // TODO LH-303: maybe if the event has a `threadRunNumber` and
        // `nodeRunPosition` set, it should do some validation here?
        for (ThreadRunModel thread : threadRunsUseMeCarefully) {
            thread.processExternalEvent(event);
        }
        advance(event.getCreatedAt());
    }

    public void processStopRequest(StopWfRunRequestModel req) {
        if (req.threadRunNumber > getGreatestThreadRunNumber() || req.threadRunNumber < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to stop a non-existent thread id.");
        }

        ThreadRunModel thread = getThreadRun(req.threadRunNumber);
        ThreadHaltReasonModel haltReason = new ThreadHaltReasonModel();
        haltReason.type = ReasonCase.MANUAL_HALT;
        haltReason.manualHalt = new ManualHaltModel();
        stop(thread, haltReason);
    }

    public void stop(ThreadRunModel thread, ThreadHaltReasonModel threadHaltReason) {
        // need to see if thread already is halted. If so, don't double halt it.
        for (ThreadHaltReasonModel reason : thread.haltReasons) {
            if (reason.type == ReasonCase.MANUAL_HALT) {
                return;
            }
        }
        thread.halt(threadHaltReason);
        this.advance(new Date()); // Seems like a good idea, why not?
    }

    public void rescueThreadRun(int threadRunNumber, boolean skipCurrentNode, CoreProcessorContext ctx)
            throws MissingThreadRunException, UnRescuableThreadRunException, ThreadRunRescueFailedException {
        validateCanRescueThreadRun(threadRunNumber, ctx);
        ThreadRunModel toRescue = getThreadRun(threadRunNumber);
        toRescue.rescue(skipCurrentNode, ctx);
    }

    private void validateCanRescueThreadRun(int threadRunNumber, CoreProcessorContext ctx)
            throws MissingThreadRunException, UnRescuableThreadRunException {
        // Some validations
        if (threadRunNumber > getGreatestThreadRunNumber() || threadRunNumber < 0) {
            throw new MissingThreadRunException("Tried to rescue a non-existent thread id.");
        }

        ThreadRunModel toRescue = getThreadRun(threadRunNumber);
        if (toRescue == null) {
            throw new UnRescuableThreadRunException("Specified ThreadRun has been garbage-collected.");
        }
        if (toRescue.getStatus() != LHStatus.ERROR) {
            throw new UnRescuableThreadRunException(
                    "Specified ThreadRun has status %s, not ERROR".formatted(toRescue.getStatus()));
        }

        NodeRunModel failedNode = toRescue.getCurrentNodeRun();
        if (failedNode.getFailures().isEmpty()) {
            throw new IllegalStateException(
                    "This is a LH Bug: A ThreadRun can only fail if there is a Failure on its current NodeRun");
        }

        // Now we need to validate that the actual ERROR hasn't been handled by some exception handler somewhere.
        ThreadRunModel child = toRescue;
        while (child.getParent() != null) {
            ThreadRunModel parent = child.getParent();
            if (parent.handledFailedChildren.contains(child.getNumber())) {
                throw new UnRescuableThreadRunException(
                        "Parent of specified ThreadRun has already handled the failure");
            }
            child = parent;
        }
    }

    public void processResumeRequest(ResumeWfRunRequestModel req) {
        if (req.threadRunNumber > getGreatestThreadRunNumber() || req.threadRunNumber < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to resume a non-existent thread id.");
        }

        ThreadRunModel thread = getThreadRun(req.threadRunNumber);

        for (int i = thread.haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReasonModel thr = thread.haltReasons.get(i);
            if (thr.type == ReasonCase.MANUAL_HALT) {
                thread.haltReasons.remove(i);
            }
        }
        this.advance(new Date());
    }

    // This will be cleaned up and seperated into 2 method
    //.validate() and .migrate() with the use of helper methods

     public void processMigration(
             MigrateWfRunRequestModel req,
             Map<String,MigrationVariablesModel> migrationVars,
             CoreProcessorContext context) {

        // STEP 1) ENTER A DORMIT WFRUN STATE WHERE STATUS CAN NOT BE UPDATE 
        // Threads can change state rapidly so thats why we need to pause
        WfRunMigrationPlanModel migrationPlan =
                context.service().getWfRunMigrationPlan(req.getMigrationPlanId().getName());
        if (migrationPlan == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "Couldn't find MigrationPlan: " + req.getMigrationPlanId().getName());
        }
        if (migrationPlan.getNewWfSpecId() == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "Migration plan %s does not specify a new WfSpec".formatted(req.getMigrationPlanId().getName()));
        }
        WfSpecModel newWfSpec = context.service().getWfSpec(migrationPlan.getNewWfSpecId());
        if (newWfSpec == null) {
            throw new LHApiException(
                    Status.NOT_FOUND,
                    "The new version of wfSpec was not found: " + migrationPlan.getNewWfSpecId());
        }

        validateEntrypointThreadMigration(migrationPlan, getWfSpec(), newWfSpec);
        validateRequiredMigrationVars(migrationPlan, migrationVars);
        this.migrationPlanId = req.getMigrationPlanId();
        advance(context.currentCommand().getTime());
    }

    private void validateEntrypointThreadMigration(
            WfRunMigrationPlanModel migrationPlan,
            WfSpecModel oldWfSpec,
            WfSpecModel newWfSpec) {
        if (migrationPlan == null || migrationPlan.getThreadMigrations() == null) {
            return;
        }

        String oldEntrypoint = oldWfSpec.getEntrypointThreadName();
        String newEntrypoint = newWfSpec.getEntrypointThreadName();

        for (Map.Entry<String, ThreadMigrationPlanModel> entry :
                migrationPlan.getThreadMigrations().entrySet()) {
            String oldThreadName = entry.getKey();
            String newThreadName = entry.getValue().getNewThreadName();

            boolean oldIsEntrypoint = oldThreadName.equals(oldEntrypoint);
            boolean newIsEntrypoint = newThreadName.equals(newEntrypoint);

            if (oldIsEntrypoint != newIsEntrypoint) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Entrypoint thread must migrate to entrypoint thread: %s -> %s".formatted(
                                oldThreadName, newThreadName));
            }
        }
    }

    private void validateRequiredMigrationVars(
            WfRunMigrationPlanModel migrationPlan,
            Map<String, MigrationVariablesModel> migrationVars) {
        if (migrationPlan == null || migrationPlan.getThreadMigrations() == null) {
            return;
        }

        for (Map.Entry<String, ThreadMigrationPlanModel> entry :
                migrationPlan.getThreadMigrations().entrySet()) {
            String threadName = entry.getKey();
            ThreadMigrationPlanModel threadPlan = entry.getValue();

            if (threadPlan.getRequiredMigrationVars() == null || threadPlan.getRequiredMigrationVars().isEmpty()) {
                continue;
            }

            MigrationVariablesModel varsForThread =
                    migrationVars == null ? null : migrationVars.get(threadName);
            if (varsForThread == null || varsForThread.getVarValues() == null) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Missing required migration vars for thread %s".formatted(threadName));
            }

            for (String varName : threadPlan.getRequiredMigrationVars()) {
                if (!varsForThread.getVarValues().containsKey(varName)) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT,
                            "Missing required migration var %s for thread %s".formatted(varName, threadName));
                }
            }
        }
    }

    
    public void processSleepNodeMatured(SleepNodeMaturedModel req, Date time) throws LHValidationException {
        int threadRunNumber = req.getNodeRunId().getThreadRunNumber();
        int nodeRunPosition = req.getNodeRunId().getPosition();
        if (threadRunNumber >= threadRunsUseMeCarefully.size() || threadRunNumber < 0) {
            throw new LHValidationException(null, "Reference to nonexistent thread.");
        }

        ThreadRunModel thread = getThreadRun(threadRunNumber);

        if (nodeRunPosition > thread.currentNodePosition) {
            throw new LHValidationException(null, "Reference to nonexistent nodeRun");
        }

        thread.processSleepNodeMatured(req);
        advance(time);
    }

    public void transitionTo(LHStatus status) {
        CoreProcessorContext processorContext = executionContext.castOnSupport(CoreProcessorContext.class);
        GetableUpdates.GetableStatusUpdate statusChanged;
        if (Objects.equals(status, LHStatus.COMPLETED)) {
            statusChanged = GetableUpdates.create(
                    wfSpecId, processorContext.authorization().tenantId(), this.status, status);
        } else {
            statusChanged = GetableUpdates.createEndEvent(
                    wfSpecId, processorContext.authorization().tenantId(), this.status, status, startTime);
        }
        this.status = status;
        processorContext.getableUpdates().dispatch(statusChanged);

        WorkflowRetentionPolicyModel retentionPolicy = getWfSpec().getRetentionPolicy();
        if (retentionPolicy != null && isTerminated()) {
            Date terminationTime = retentionPolicy.scheduleTerminationFor(this);

            if (terminationTime != null) {
                LHTimer timer = new LHTimer();
                timer.partitionKey = id.getPartitionKey().get();
                timer.maturationTime = terminationTime;
                InternalDeleteWfRunRequestModel deleteWfRun = new InternalDeleteWfRunRequestModel();
                deleteWfRun.setWfRunId(id);

                CommandModel deleteWfRunCmd = new CommandModel();
                deleteWfRunCmd.setSubCommand(deleteWfRun);
                deleteWfRunCmd.time = timer.maturationTime;
                timer.payload = deleteWfRunCmd.toProto().build().toByteArray();
                processorContext.getTaskManager().scheduleTimer(timer);
            }
        }
    }

    private boolean isTerminated() {
        switch (status) {
            case COMPLETED:
            case ERROR:
            case EXCEPTION:
                return true;
            case STARTING:
            case RUNNING:
            case HALTED:
            case HALTING:
            case UNRECOGNIZED:
        }
        return false;
    }

    // As a precondition, the status of the calling thread must already be updated
    // to complete.
    public void handleThreadStatus(int threadRunNumber, Date time, LHStatus newStatus) {
        // WfRun Status is determined by the Entrypoint Thread
        if (threadRunNumber == 0) {
            // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
            // killing (or waiting for) any child threads. To be determined based on threading design.
            if (newStatus == LHStatus.COMPLETED) {
                endTime = time;
                transitionTo(LHStatus.COMPLETED);
                log.debug("Completed WfRun {} at {} ", id, new Date());
            } else if (newStatus == LHStatus.ERROR) {
                endTime = time;
                transitionTo(LHStatus.ERROR);
            } else if (newStatus == LHStatus.EXCEPTION) {
                endTime = time;
                transitionTo(LHStatus.EXCEPTION);
            } else {
                // The WfRun's status must always match the entrypoint ThreadRun.
                transitionTo(newStatus);
            }
        }

        if (endTime != null) {
            // wake up parent if parent exists
            if (id.getParentWfRunId() != null) {
                WfRunModel parent = executionContext
                        .castOnSupport(CoreProcessorContext.class)
                        .getableManager()
                        .get(id.getParentWfRunId());
                if (parent != null) parent.advance(time);
            }
        }

        // ThreadRuns depend on each other, for example Exception Handler Threads or
        // child threads, so we need to signal to the other threads that they might
        // want to wake up. Ding Ding Ding! Get out of bed.
        advance(time);
    }

    private List<ThreadRunModel> getActiveThreads(){
        List<ThreadRunModel> activeThreads  = new ArrayList<>();
        for(int i = 0 ; i < threadRunsUseMeCarefully.size(); i++){
            ThreadRunModel thread = threadRunsUseMeCarefully.get(i);
            if(thread.isRunning()) activeThreads.add(thread);
        }
        return activeThreads;

    }
    

//     private void migrateWfRun(List<ThreadRunModel> activeThreads, WfSpecModel newWfSpec, WfRunMigrationPlanModel migrationPlan){
//         this.getOldWfSpecVersions().add(wfSpecId);
//         this.setWfSpec(newWfSpec);
//         this.setWfSpecId(newWfSpec.getId());

//         for(int i = 0 ; i < activeThreads.size(); i++){
//             ThreadRunModel migrateThread = activeThreads.get(i);
//             migrateThread.setWfSpecId(newWfSpec.getId());
//             ThreadMigrationPlanModel threadMigrationPlan = migrationPlan.getMigrationPlan().getThreadMigrations().get(migrateThread.getThreadSpecName());
//             String newThreadName = threadMigrationPlan.getNewThreadName();
//             migrateThread.setThreadSpecName(newThreadName);
//             NodeRunModel currNode = migrateThread.getCurrentNodeRun();
//             currNode.setWfSpecId(newWfSpec.getId());
//             currNode.setThreadSpecName(newThreadName);
//             currNode.setNodeName(threadMigrationPlan.getNodeMigrations().get(currNode.getNodeName()).getNewNode());

//         } 

//     }
}

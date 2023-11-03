package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureBeingHandledModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.PendingFailureHandlerModel;
import io.littlehorse.common.model.getable.core.wfrun.haltreason.ManualHaltModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WorkflowRetentionPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PendingFailureHandler;
import io.littlehorse.sdk.common.proto.PendingInterrupt;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Setter
@Getter
public class WfRunModel extends CoreGetable<WfRun> {

    public String id;
    public String wfSpecName;
    public int wfSpecVersion;
    public LHStatus status;
    public Date startTime;
    public Date endTime;
    private List<ThreadRunModel> threadRuns;
    public List<PendingInterruptModel> pendingInterrupts;
    public List<PendingFailureHandlerModel> pendingFailures;

    public WfRunModel() {
        threadRuns = new ArrayList<>();
        pendingInterrupts = new ArrayList<>();
        pendingFailures = new ArrayList<>();
    }

    public Date getCreatedAt() {
        return startTime;
    }

    // K -> V
    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("wfSpecVersion", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)));
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
            case "wfSpecVersion" -> {
                return List.of(new IndexedField(
                        key, LHUtil.toLHDbVersionFormat(this.getWfSpecVersion()), TagStorageType.LOCAL));
            }
        }
        return List.of();
    }

    public WfSpecModel getWfSpec() {
        if (wfSpec == null) {
            wfSpec = getDao().getWfSpec(wfSpecName, wfSpecVersion);
        }
        return wfSpec;
    }

    public void setWfSpec(WfSpecModel spec) {
        this.wfSpec = spec;
    }

    public ThreadRunModel getThreadRun(int threadRunNumber) {
        return threadRuns.stream()
                .filter(thread -> thread.getNumber() == threadRunNumber)
                .findFirst()
                .orElse(null);
    }

    public void initFrom(Message p) {
        WfRun proto = (WfRun) p;
        id = proto.getId();
        wfSpecName = proto.getWfSpecName();
        wfSpecVersion = proto.getWfSpecVersion();
        status = proto.getStatus();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());

        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        for (ThreadRun trpb : proto.getThreadRunsList()) {
            ThreadRunModel thr = ThreadRunModel.fromProto(trpb);
            thr.wfRun = this;
            threadRuns.add(thr);
        }
        for (PendingInterrupt pipb : proto.getPendingInterruptsList()) {
            pendingInterrupts.add(PendingInterruptModel.fromProto(pipb));
        }
        for (PendingFailureHandler pfhpb : proto.getPendingFailuresList()) {
            pendingFailures.add(PendingFailureHandlerModel.fromProto(pfhpb));
        }
    }

    public WfRunIdModel getObjectId() {
        return new WfRunIdModel(id);
    }

    /*
     * Returns true if this WfRun is currently running. Due to the inheritance
     * structure of threads, we can determine this by simply checking if the
     * entrypoint thread is running.
     */
    public boolean isRunning() {
        return threadRuns.get(0).isRunning();
    }

    public WfRun.Builder toProto() {
        WfRun.Builder out = WfRun.newBuilder()
                .setId(id)
                .setWfSpecName(wfSpecName)
                .setWfSpecVersion(wfSpecVersion)
                .setStatus(status)
                .setStartTime(LHUtil.fromDate(startTime));

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRunModel threadRunModel : threadRuns) {
            out.addThreadRuns(threadRunModel.toProto());
        }

        for (PendingInterruptModel pi : pendingInterrupts) {
            out.addPendingInterrupts(pi.toProto());
        }

        for (PendingFailureHandlerModel pfh : pendingFailures) {
            out.addPendingFailures(pfh.toProto());
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
        ThreadSpecModel tspec = wfSpec.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name, should be impossible");
        }

        ThreadRunModel thread = new ThreadRunModel();
        thread.number = threadRuns.size();
        thread.parentThreadId = parentThreadId;

        thread.status = LHStatus.RUNNING;
        thread.threadSpecName = threadName;
        thread.currentNodePosition = -1; // this gets bumped when we start the thread

        thread.startTime = new Date();

        thread.wfRun = this;
        thread.type = type;
        threadRuns.add(thread);

        try {
            tspec.validateStartVariables(variables);
        } catch (LHValidationError exn) {
            log.error("Invalid variables received", exn);
            // TODO: determine how observability events should look like for this case.
            thread.fail(
                    new FailureModel(
                            "Failed validating variables on start: " + exn.getMessage(),
                            LHConstants.VAR_MUTATION_ERROR),
                    thread.startTime);
            return thread;
        }

        for (VariableDefModel varDef : tspec.variableDefs) {
            String varName = varDef.name;
            VariableValueModel val;

            if (variables.containsKey(varName)) {
                val = variables.get(varName);
            } else if (varDef.getDefaultValue() != null) {
                val = varDef.getDefaultValue();
            } else {
                val = new VariableValueModel();
                val.type = VariableType.NULL;
            }

            try {
                thread.createVariable(varName, val);
            } catch (LHVarSubError exn) {
                throw new RuntimeException("Not possible");
            }
        }
        thread.activateNode(thread.getCurrentNode());
        thread.advance(start);
        return thread;
    }

    public String getWfSpecName() {
        return wfSpecName;
    }

    public LHStatus getStatus() {
        return status;
    }

    public String getWfSpecFormattedVersion() {
        return LHUtil.toLHDbVersionFormat(wfSpecVersion);
    }

    public int getWfSpecVersion() {
        return wfSpecVersion;
    }

    private boolean startXnHandlersAndInterrupts(Date time) {
        boolean somethingChanged = false;
        somethingChanged = startInterrupts(time) || somethingChanged;
        somethingChanged = startXnHandlers(time) || somethingChanged;
        return somethingChanged;
    }

    private boolean startInterrupts(Date time) {
        boolean somethingChanged = false;
        List<PendingInterruptModel> toHandleNow = new ArrayList<>();
        // Can only send one interrupt at a time to a thread...they need to complete
        // sequentially.
        Set<Integer> threadsToHandleNow = new HashSet<>();

        for (int i = pendingInterrupts.size() - 1; i >= 0; i--) {
            PendingInterruptModel pi = pendingInterrupts.get(i);
            ThreadRunModel toInterrupt = threadRuns.get(pi.interruptedThreadId);

            if (toInterrupt.canBeInterrupted()) {
                if (!threadsToHandleNow.contains(pi.interruptedThreadId)) {
                    threadsToHandleNow.add(pi.interruptedThreadId);
                    somethingChanged = true;
                    toHandleNow.add(0, pi);
                    pendingInterrupts.remove(i);
                }
            }
        }

        for (PendingInterruptModel pi : toHandleNow) {
            ThreadRunModel toInterrupt = threadRuns.get(pi.interruptedThreadId);
            Map<String, VariableValueModel> vars;

            ThreadSpecModel iSpec = wfSpec.threadSpecs.get(pi.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                ExternalEventModel event = getDao().get(pi.externalEventId);
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, event.content);
            } else {
                vars = new HashMap<>();
            }
            ThreadRunModel interruptor =
                    startThread(pi.handlerSpecName, time, pi.interruptedThreadId, vars, ThreadType.INTERRUPT);
            interruptor.interruptTriggerId = pi.externalEventId;

            if (interruptor.status == LHStatus.ERROR) {
                toInterrupt.fail(
                        new FailureModel(
                                "Failed launching interrupt thread with id: " + interruptor.number,
                                LHConstants.CHILD_FAILURE),
                        time);
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
            ThreadRunModel failedThr = threadRuns.get(pfh.failedThreadRun);

            if (!failedThr.canBeInterrupted()) {
                continue;
            }
            somethingChanged = true;
            pendingFailures.remove(i);
            Map<String, VariableValueModel> vars;

            ThreadSpecModel iSpec = wfSpec.threadSpecs.get(pfh.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                FailureModel failure = failedThr.getCurrentNodeRun().getLatestFailure();
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, failure.content);
            } else {
                vars = new HashMap<>();
            }

            ThreadRunModel fh =
                    startThread(pfh.handlerSpecName, time, pfh.failedThreadRun, vars, ThreadType.FAILURE_HANDLER);

            failedThr.getCurrentNodeRun().failureHandlerIds.add(fh.number);

            fh.failureBeingHandled = new FailureBeingHandledModel();
            fh.failureBeingHandled.setFailureNumber(
                    failedThr.getCurrentNodeRun().failures.size() - 1);
            fh.failureBeingHandled.setNodeRunPosition(failedThr.currentNodePosition);
            fh.failureBeingHandled.setThreadRunNumber(pfh.failedThreadRun);

            if (fh.status == LHStatus.ERROR) {
                fh.fail(
                        new FailureModel(
                                "Failed launching interrupt thread with id: " + fh.number, LHConstants.CHILD_FAILURE),
                        time);
            } else {
                failedThr.acknowledgeXnHandlerStarted(pfh, fh.number);
            }
        }

        return somethingChanged;
    }

    public void advance(Date time) {
        boolean statusChanged = false;
        // Update status and then advance
        for (ThreadRunModel thread : threadRuns) {
            statusChanged = thread.updateStatus() || statusChanged;
        }
        boolean xnHandlersStarted = startXnHandlersAndInterrupts(time);
        statusChanged = xnHandlersStarted || statusChanged;
        for (int i = threadRuns.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRuns.get(i);
            statusChanged = thread.advance(time) || statusChanged;
        }
        for (int i = threadRuns.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRuns.get(i);
            statusChanged = thread.updateStatus() || statusChanged;
        }

        while (statusChanged) {
            startXnHandlersAndInterrupts(time);
            statusChanged = false;
            for (int i = threadRuns.size() - 1; i >= 0; i--) {
                ThreadRunModel thread = threadRuns.get(i);
                statusChanged = thread.advance(time) || statusChanged;
            }

            for (int i = threadRuns.size() - 1; i >= 0; i--) {
                ThreadRunModel thread = threadRuns.get(i);
                statusChanged = thread.updateStatus() || statusChanged;
            }
        }

        // Now we remove any old threadruns that we don't want anymore.
        for (int i = threadRuns.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRuns.get(i);
            ThreadSpecModel spec = thread.getThreadSpecModel();
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

        threadRuns.removeIf(candidate -> candidate.getNumber() == thread.getNumber());
    }

    public void processExtEvtTimeout(ExternalEventTimeout timeout) {
        ThreadRunModel handler = threadRuns.get(timeout.threadRunNumber);
        handler.processExtEvtTimeout(timeout);
        advance(timeout.time);
    }

    public void failDueToWfSpecDeletion() {
        threadRuns.get(0).fail(new FailureModel("Appears wfSpec was deleted", LHConstants.INTERNAL_ERROR), new Date());
    }

    public void processExternalEvent(ExternalEventModel event) {
        // TODO LH-303: maybe if the event has a `threadRunNumber` and
        // `nodeRunPosition` set, it should do some validation here?
        for (ThreadRunModel thread : threadRuns) {
            thread.processExternalEvent(event);
        }
        advance(event.getCreatedAt());
    }

    public void processStopRequest(StopWfRunRequestModel req) {
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to stop a non-existent thread id.");
        }

        ThreadRunModel thread = threadRuns.get(req.threadRunNumber);
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

    public void processResumeRequest(ResumeWfRunRequestModel req) {
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Tried to resume a non-existent thread id.");
        }

        ThreadRunModel thread = threadRuns.get(req.threadRunNumber);

        for (int i = thread.haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReasonModel thr = thread.haltReasons.get(i);
            if (thr.type == ReasonCase.MANUAL_HALT) {
                thread.haltReasons.remove(i);
            }
        }
        this.advance(new Date());
    }

    public void processSleepNodeMatured(SleepNodeMatured req, Date time) throws LHValidationError {
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHValidationError(null, "Reference to nonexistent thread.");
        }

        ThreadRunModel thread = threadRuns.get(req.threadRunNumber);

        if (req.nodeRunPosition > thread.currentNodePosition) {
            throw new LHValidationError(null, "Reference to nonexistent nodeRun");
        }

        thread.processSleepNodeMatured(req);
        advance(time);
    }

    private void setStatus(LHStatus status) {
        this.status = status;

        WorkflowRetentionPolicyModel retentionPolicy = getWfSpec().getRetentionPolicy();
        if (retentionPolicy != null && isTerminated()) {
            Date terminationTime = retentionPolicy.scheduleTerminationFor(this);

            if (terminationTime != null) {
                LHTimer timer = new LHTimer();
                timer.topic = this.getDao().getCoreCmdTopic();
                timer.key = id;
                timer.maturationTime = terminationTime;
                DeleteWfRunRequestModel deleteWfRun = new DeleteWfRunRequestModel();
                deleteWfRun.wfRunId = new WfRunIdModel(id);

                CommandModel deleteWfRunCmd = new CommandModel();
                deleteWfRunCmd.setSubCommand(deleteWfRun);
                deleteWfRunCmd.time = timer.maturationTime;
                timer.payload = deleteWfRunCmd.toProto().build().toByteArray();
                this.getDao().scheduleTimer(timer);
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
            // TODO: In the future, there may be some other lifecycle hooks here, such as
            // forcibly
            // killing (or waiting for) any child threads. To be determined based on
            // threading
            // design.
            if (newStatus == LHStatus.COMPLETED) {
                endTime = time;
                setStatus(LHStatus.COMPLETED);
                log.info("Completed WfRun {} at {} ", id, new Date());
            } else if (newStatus == LHStatus.ERROR) {
                endTime = time;
                setStatus(LHStatus.ERROR);
            } else if (newStatus == LHStatus.EXCEPTION) {
                endTime = time;
                setStatus(LHStatus.EXCEPTION);
            }
        }

        // ThreadRuns depend on each other, for example Exception Handler Threads or
        // child threads, so we need to signal to the other threads that they might
        // want to wake up. Ding Ding Ding! Get out of bed.
        advance(time);
    }
}

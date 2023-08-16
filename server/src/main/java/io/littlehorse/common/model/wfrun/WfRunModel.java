package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.DeleteWfRun;
import io.littlehorse.common.model.command.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.command.subcommand.ResumeWfRun;
import io.littlehorse.common.model.command.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.command.subcommand.StopWfRun;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.wfrun.haltreason.ManualHalt;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PendingFailureHandlerPb;
import io.littlehorse.sdk.common.proto.PendingInterruptPb;
import io.littlehorse.sdk.common.proto.ThreadHaltReasonPb.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadTypePb;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
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
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Setter
@Getter
public class WfRunModel extends Getable<WfRun> {

    public String id;
    public String wfSpecName;
    public int wfSpecVersion;
    public LHStatus status;
    public Date startTime;
    public Date endTime;
    public List<ThreadRunModel> threadRunModels;
    public List<PendingInterrupt> pendingInterrupts;
    public List<PendingFailureHandler> pendingFailures;

    public WfRunModel() {
        threadRunModels = new ArrayList<>();
        pendingInterrupts = new ArrayList<>();
        pendingFailures = new ArrayList<>();
    }

    public Date getCreatedAt() {
        return startTime;
    }

    // K -> V
    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of(
            new GetableIndex<>(
                List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            new GetableIndex<>(
                List.of(
                    Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                    Pair.of("status", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            new GetableIndex<>(
                List.of(
                    Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("wfSpecVersion", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL)
            )
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getWfSpecName(),
                        tagStorageTypePb.get()
                    )
                );
            }
            case "status" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getStatus().toString(),
                        tagStorageTypePb.get()
                    )
                );
            }
            case "wfSpecVersion" -> {
                return List.of(
                    new IndexedField(
                        key,
                        LHUtil.toLHDbVersionFormat(this.getWfSpecVersion()),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
        }
        return List.of();
    }

    public WfSpecModel getWfSpecModel() {
        return wfSpecModel;
    }

    public void setWfSpecModel(WfSpecModel spec) {
        this.wfSpecModel = spec;
    }

    public ThreadRunModel getThreadRun(int threadRunNumber) {
        if (threadRunNumber < 0 || threadRunNumber >= threadRunModels.size()) {
            return null;
        }
        return threadRunModels.get(threadRunNumber);
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
            thr.wfRunModel = this;
            threadRunModels.add(thr);
        }
        for (PendingInterruptPb pipb : proto.getPendingInterruptsList()) {
            pendingInterrupts.add(PendingInterrupt.fromProto(pipb));
        }
        for (PendingFailureHandlerPb pfhpb : proto.getPendingFailuresList()) {
            pendingFailures.add(PendingFailureHandler.fromProto(pfhpb));
        }
    }

    public WfRunId getObjectId() {
        return new WfRunId(id);
    }

    /*
     * Returns true if this WfRun is currently running. Due to the inheritance
     * structure of threads, we can determine this by simply checking if the
     * entrypoint thread is running.
     */
    public boolean isRunning() {
        return threadRunModels.get(0).isRunning();
    }

    public WfRun.Builder toProto() {
        WfRun.Builder out = WfRun
            .newBuilder()
            .setId(id)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion)
            .setStatus(status)
            .setStartTime(LHUtil.fromDate(startTime));

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRunModel threadRunModel : threadRunModels) {
            out.addThreadRuns(threadRunModel.toProto());
        }

        for (PendingInterrupt pi : pendingInterrupts) {
            out.addPendingInterrupts(pi.toProto());
        }

        for (PendingFailureHandler pfh : pendingFailures) {
            out.addPendingFailures(pfh.toProto());
        }

        return out;
    }

    public Class<WfRun> getProtoBaseClass() {
        return WfRun.class;
    }

    // Below is used by scheduler

    public WfSpecModel wfSpecModel;

    public ThreadRunModel startThread(
        String threadName,
        Date start,
        Integer parentThreadId,
        Map<String, VariableValueModel> variables,
        ThreadTypePb type
    ) {
        ThreadSpecModel tspec = wfSpecModel.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name, should be impossible");
        }

        ThreadRunModel thread = new ThreadRunModel();
        thread.wfRunId = id;
        thread.number = threadRunModels.size();
        thread.parentThreadId = parentThreadId;

        thread.status = LHStatus.RUNNING;
        thread.wfSpecName = wfSpecName;
        thread.wfSpecVersion = wfSpecVersion;
        thread.threadSpecName = threadName;
        thread.currentNodePosition = -1; // this gets bumped when we start the thread

        thread.startTime = new Date();

        thread.wfRunModel = this;
        thread.type = type;
        threadRunModels.add(thread);

        try {
            tspec.validateStartVariables(variables);
        } catch (LHValidationError exn) {
            log.error("Invalid variables received", exn);
            // TODO: determine how observability events should look like for this case.
            thread.fail(
                new Failure(
                    "Failed validating variables on start: " + exn.getMessage(),
                    LHConstants.VAR_MUTATION_ERROR
                ),
                thread.startTime
            );
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
        List<PendingInterrupt> toHandleNow = new ArrayList<>();
        // Can only send one interrupt at a time to a thread...they need to complete
        // sequentially.
        Set<Integer> threadsToHandleNow = new HashSet<>();

        for (int i = pendingInterrupts.size() - 1; i >= 0; i--) {
            PendingInterrupt pi = pendingInterrupts.get(i);
            ThreadRunModel toInterrupt = threadRunModels.get(pi.interruptedThreadId);

            if (toInterrupt.canBeInterrupted()) {
                if (!threadsToHandleNow.contains(pi.interruptedThreadId)) {
                    threadsToHandleNow.add(pi.interruptedThreadId);
                    somethingChanged = true;
                    toHandleNow.add(0, pi);
                    pendingInterrupts.remove(i);
                }
            }
        }

        for (PendingInterrupt pi : toHandleNow) {
            ThreadRunModel toInterrupt = threadRunModels.get(pi.interruptedThreadId);
            Map<String, VariableValueModel> vars;

            ThreadSpecModel iSpec = wfSpecModel.threadSpecs.get(pi.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                ExternalEvent event = getDao()
                    .getExternalEvent(pi.externalEventId.getStoreKey());
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, event.content);
            } else {
                vars = new HashMap<>();
            }
            ThreadRunModel interruptor = startThread(
                pi.handlerSpecName,
                time,
                pi.interruptedThreadId,
                vars,
                ThreadTypePb.INTERRUPT
            );
            interruptor.interruptTriggerId = pi.externalEventId;

            if (interruptor.status == LHStatus.ERROR) {
                toInterrupt.fail(
                    new Failure(
                        "Failed launching interrupt thread with id: " +
                        interruptor.number,
                        LHConstants.CHILD_FAILURE
                    ),
                    time
                );
            } else {
                toInterrupt.acknowledgeInterruptStarted(pi, interruptor.number);
            }
        }

        return somethingChanged;
    }

    private boolean startXnHandlers(Date time) {
        boolean somethingChanged = false;

        for (int i = pendingFailures.size() - 1; i >= 0; i--) {
            PendingFailureHandler pfh = pendingFailures.get(i);
            ThreadRunModel failedThr = threadRunModels.get(pfh.failedThreadRun);

            if (!failedThr.canBeInterrupted()) {
                continue;
            }
            somethingChanged = true;
            pendingFailures.remove(i);
            Map<String, VariableValueModel> vars;

            ThreadSpecModel iSpec = wfSpecModel.threadSpecs.get(pfh.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                Failure failure = failedThr.getCurrentNodeRun().getLatestFailure();
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, failure.content);
            } else {
                vars = new HashMap<>();
            }

            ThreadRunModel fh = startThread(
                pfh.handlerSpecName,
                time,
                pfh.failedThreadRun,
                vars,
                ThreadTypePb.FAILURE_HANDLER
            );

            failedThr.getCurrentNodeRun().failureHandlerIds.add(fh.number);

            fh.failureBeingHandled = new FailureBeingHandled();
            fh.failureBeingHandled.setFailureNumber(
                failedThr.getCurrentNodeRun().failures.size() - 1
            );
            fh.failureBeingHandled.setNodeRunPosition(failedThr.currentNodePosition);
            fh.failureBeingHandled.setThreadRunNumber(pfh.failedThreadRun);

            if (fh.status == LHStatus.ERROR) {
                fh.fail(
                    new Failure(
                        "Failed launching interrupt thread with id: " + fh.number,
                        LHConstants.CHILD_FAILURE
                    ),
                    time
                );
            } else {
                failedThr.acknowledgeXnHandlerStarted(pfh, fh.number);
            }
        }

        return somethingChanged;
    }

    public void advance(Date time) {
        boolean statusChanged = false;
        // Update status and then advance
        for (ThreadRunModel thread : threadRunModels) {
            statusChanged = thread.updateStatus() || statusChanged;
        }
        boolean xnHandlersStarted = startXnHandlersAndInterrupts(time);
        statusChanged = xnHandlersStarted || statusChanged;
        for (int i = threadRunModels.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRunModels.get(i);
            statusChanged = thread.advance(time) || statusChanged;
        }
        for (int i = threadRunModels.size() - 1; i >= 0; i--) {
            ThreadRunModel thread = threadRunModels.get(i);
            statusChanged = thread.updateStatus() || statusChanged;
        }

        while (statusChanged) {
            statusChanged = startXnHandlersAndInterrupts(time) || statusChanged;
            statusChanged = false;
            for (int i = threadRunModels.size() - 1; i >= 0; i--) {
                ThreadRunModel thread = threadRunModels.get(i);
                statusChanged = thread.advance(time) || statusChanged;
            }

            for (int i = threadRunModels.size() - 1; i >= 0; i--) {
                ThreadRunModel thread = threadRunModels.get(i);
                statusChanged = thread.updateStatus() || statusChanged;
            }
        }
    }

    public void processExtEvtTimeout(ExternalEventTimeout timeout) {
        ThreadRunModel handler = threadRunModels.get(timeout.threadRunNumber);
        handler.processExtEvtTimeout(timeout);
        advance(timeout.time);
    }

    public void failDueToWfSpecDeletion() {
        threadRunModels
            .get(0)
            .fail(
                new Failure("Appears wfSpec was deleted", LHConstants.INTERNAL_ERROR),
                new Date()
            );
    }

    public void processExternalEvent(ExternalEvent event) {
        // TODO LH-303: maybe if the event has a `threadRunNumber` and
        // `nodeRunPosition` set, it should do some validation here?
        for (ThreadRunModel thread : threadRunModels) {
            thread.processExternalEvent(event);
        }
        advance(event.getCreatedAt());
    }

    public void processStopRequest(StopWfRun req) throws LHValidationError {
        if (
            req.threadRunNumber >= threadRunModels.size() || req.threadRunNumber < 0
        ) {
            throw new LHValidationError(
                null,
                "Tried to stop a non-existent thread id."
            );
        }

        ThreadRunModel thread = threadRunModels.get(req.threadRunNumber);
        ThreadHaltReason haltReason = new ThreadHaltReason();
        haltReason.type = ReasonCase.MANUAL_HALT;
        haltReason.manualHalt = new ManualHalt();
        stop(thread, haltReason);
    }

    public void stop(ThreadRunModel thread, ThreadHaltReason threadHaltReason) {
        // need to see if thread already is halted. If so, don't double halt it.
        for (ThreadHaltReason reason : thread.haltReasons) {
            if (reason.type == ReasonCase.MANUAL_HALT) {
                return;
            }
        }
        thread.halt(threadHaltReason);
        this.advance(new Date()); // Seems like a good idea, why not?
    }

    public void processResumeRequest(ResumeWfRun req) throws LHValidationError {
        if (
            req.threadRunNumber >= threadRunModels.size() || req.threadRunNumber < 0
        ) {
            throw new LHValidationError(
                null,
                "Tried to resume a non-existent thread id."
            );
        }

        ThreadRunModel thread = threadRunModels.get(req.threadRunNumber);

        for (int i = thread.haltReasons.size() - 1; i >= 0; i--) {
            ThreadHaltReason thr = thread.haltReasons.get(i);
            if (thr.type == ReasonCase.MANUAL_HALT) {
                thread.haltReasons.remove(i);
            }
        }
        this.advance(new Date());
    }

    public void processSleepNodeMatured(SleepNodeMatured req, Date time)
        throws LHValidationError {
        if (
            req.threadRunNumber >= threadRunModels.size() || req.threadRunNumber < 0
        ) {
            throw new LHValidationError(null, "Reference to nonexistent thread.");
        }

        ThreadRunModel thread = threadRunModels.get(req.threadRunNumber);

        if (req.nodeRunPosition > thread.currentNodePosition) {
            throw new LHValidationError(null, "Reference to nonexistent nodeRun");
        }

        thread.processSleepNodeMatured(req);
        advance(time);
    }

    private void setStatus(LHStatus status) {
        this.status = status;

        if (status.equals(LHStatus.COMPLETED) || status.equals(LHStatus.ERROR)) {
            LHTimer timer = new LHTimer();
            timer.topic = this.getDao().getCoreCmdTopic();
            timer.key = id;
            Date now = new Date();
            timer.maturationTime =
                DateUtils.addHours(now, this.wfSpecModel.retentionHours);
            DeleteWfRun deleteWfRun = new DeleteWfRun();
            deleteWfRun.wfRunId = id;

            Command deleteWfRunCmd = new Command();
            deleteWfRunCmd.setSubCommand(deleteWfRun);
            deleteWfRunCmd.time = timer.maturationTime;
            timer.payload = deleteWfRunCmd.toProto().build().toByteArray();
            this.getDao().scheduleTimer(timer);
        }
    }

    // As a precondition, the status of the calling thread must already be updated to complete.
    public void handleThreadStatus(
        int threadRunNumber,
        Date time,
        LHStatus newStatus
    ) {
        // WfRun Status is determined by the Entrypoint Thread
        if (threadRunNumber == 0) {
            // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
            // killing (or waiting for) any child threads. To be determined based on threading design.
            if (newStatus == LHStatus.COMPLETED) {
                endTime = time;
                setStatus(LHStatus.COMPLETED);
                log.info("Completed WfRun {} at {} ", id, new Date());
            } else if (newStatus == LHStatus.ERROR) {
                endTime = time;
                setStatus(LHStatus.ERROR);
            }
        }

        // ThreadRuns depend on each other, for example Exception Handler Threads or
        // child threads, so we need to signal to the other threads that they might
        // want to wake up. Ding Ding Ding! Get out of bed.
        advance(time);
    }
}

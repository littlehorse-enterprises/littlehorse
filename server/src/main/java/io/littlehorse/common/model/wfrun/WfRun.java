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
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.WfRunId;
import io.littlehorse.common.model.wfrun.haltreason.ManualHalt;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.PendingFailureHandlerPb;
import io.littlehorse.sdk.common.proto.PendingInterruptPb;
import io.littlehorse.sdk.common.proto.ThreadHaltReasonPb.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRunPb;
import io.littlehorse.sdk.common.proto.ThreadTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.proto.WfRunPb;
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
public class WfRun extends Getable<WfRunPb> {

    public String id;
    public String wfSpecName;
    public int wfSpecVersion;
    public LHStatusPb status;
    public Date startTime;
    public Date endTime;
    public List<ThreadRun> threadRuns;
    public List<PendingInterrupt> pendingInterrupts;
    public List<PendingFailureHandler> pendingFailures;

    public WfRun() {
        threadRuns = new ArrayList<>();
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

    public WfSpec getWfSpec() {
        return wfSpec;
    }

    public void setWfSpec(WfSpec spec) {
        this.wfSpec = spec;
    }

    public ThreadRun getThreadRun(int threadRunNumber) {
        if (threadRunNumber < 0 || threadRunNumber >= threadRuns.size()) {
            return null;
        }
        return threadRuns.get(threadRunNumber);
    }

    public void initFrom(Message p) {
        WfRunPb proto = (WfRunPb) p;
        id = proto.getId();
        wfSpecName = proto.getWfSpecName();
        wfSpecVersion = proto.getWfSpecVersion();
        status = proto.getStatus();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());

        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        for (ThreadRunPb trpb : proto.getThreadRunsList()) {
            ThreadRun thr = ThreadRun.fromProto(trpb);
            thr.wfRun = this;
            threadRuns.add(thr);
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
        return threadRuns.get(0).isRunning();
    }

    public WfRunPb.Builder toProto() {
        WfRunPb.Builder out = WfRunPb
            .newBuilder()
            .setId(id)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion)
            .setStatus(status)
            .setStartTime(LHUtil.fromDate(startTime));

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRun threadRun : threadRuns) {
            out.addThreadRuns(threadRun.toProto());
        }

        for (PendingInterrupt pi : pendingInterrupts) {
            out.addPendingInterrupts(pi.toProto());
        }

        for (PendingFailureHandler pfh : pendingFailures) {
            out.addPendingFailures(pfh.toProto());
        }

        return out;
    }

    public Class<WfRunPb> getProtoBaseClass() {
        return WfRunPb.class;
    }

    // Below is used by scheduler

    public WfSpec wfSpec;

    public ThreadRun startThread(
        String threadName,
        Date start,
        Integer parentThreadId,
        Map<String, VariableValue> variables,
        ThreadTypePb type
    ) {
        ThreadSpec tspec = wfSpec.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name, should be impossible");
        }

        ThreadRun thread = new ThreadRun();
        thread.wfRunId = id;
        thread.number = threadRuns.size();
        thread.parentThreadId = parentThreadId;

        thread.status = LHStatusPb.RUNNING;
        thread.wfSpecName = wfSpecName;
        thread.wfSpecVersion = wfSpecVersion;
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
                new Failure(
                    "Failed validating variables on start: " + exn.getMessage(),
                    LHConstants.VAR_MUTATION_ERROR
                ),
                thread.startTime
            );
            return thread;
        }

        for (VariableDef varDef : tspec.variableDefs) {
            String varName = varDef.name;
            VariableValue val;

            if (variables.containsKey(varName)) {
                val = variables.get(varName);
            } else if (varDef.getDefaultValue() != null) {
                val = varDef.getDefaultValue();
            } else {
                val = new VariableValue();
                val.type = VariableTypePb.NULL;
            }

            try {
                thread.putVariable(varName, val);
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

    public LHStatusPb getStatus() {
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
            ThreadRun toInterrupt = threadRuns.get(pi.interruptedThreadId);

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
            ThreadRun toInterrupt = threadRuns.get(pi.interruptedThreadId);
            Map<String, VariableValue> vars;

            ThreadSpec iSpec = wfSpec.threadSpecs.get(pi.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                ExternalEvent event = getDao()
                    .getExternalEvent(pi.externalEventId.getStoreKey());
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, event.content);
            } else {
                vars = new HashMap<>();
            }
            ThreadRun interruptor = startThread(
                pi.handlerSpecName,
                time,
                pi.interruptedThreadId,
                vars,
                ThreadTypePb.INTERRUPT
            );
            interruptor.interruptTriggerId = pi.externalEventId;

            if (interruptor.status == LHStatusPb.ERROR) {
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
            ThreadRun failedThr = threadRuns.get(pfh.failedThreadRun);

            if (!failedThr.canBeInterrupted()) {
                continue;
            }
            somethingChanged = true;
            pendingFailures.remove(i);
            Map<String, VariableValue> vars;

            ThreadSpec iSpec = wfSpec.threadSpecs.get(pfh.handlerSpecName);
            if (iSpec.variableDefs.size() > 0) {
                vars = new HashMap<>();
                Failure failure = failedThr.getCurrentNodeRun().getLatestFailure();
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, failure.content);
            } else {
                vars = new HashMap<>();
            }

            ThreadRun fh = startThread(
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

            if (fh.status == LHStatusPb.ERROR) {
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
        for (ThreadRun thread : threadRuns) {
            statusChanged = thread.updateStatus() || statusChanged;
        }
        boolean xnHandlersStarted = startXnHandlersAndInterrupts(time);
        statusChanged = xnHandlersStarted || statusChanged;
        for (int i = threadRuns.size() - 1; i >= 0; i--) {
            ThreadRun thread = threadRuns.get(i);
            statusChanged = thread.advance(time) || statusChanged;
        }
        for (int i = threadRuns.size() - 1; i >= 0; i--) {
            ThreadRun thread = threadRuns.get(i);
            statusChanged = thread.updateStatus() || statusChanged;
        }

        while (statusChanged) {
            statusChanged = startXnHandlersAndInterrupts(time) || statusChanged;
            statusChanged = false;
            for (int i = threadRuns.size() - 1; i >= 0; i--) {
                ThreadRun thread = threadRuns.get(i);
                statusChanged = thread.advance(time) || statusChanged;
            }

            for (int i = threadRuns.size() - 1; i >= 0; i--) {
                ThreadRun thread = threadRuns.get(i);
                statusChanged = thread.updateStatus() || statusChanged;
            }
        }
    }

    public void processExtEvtTimeout(ExternalEventTimeout timeout) {
        ThreadRun handler = threadRuns.get(timeout.threadRunNumber);
        handler.processExtEvtTimeout(timeout);
        advance(timeout.time);
    }

    public void failDueToWfSpecDeletion() {
        threadRuns
            .get(0)
            .fail(
                new Failure("Appears wfSpec was deleted", LHConstants.INTERNAL_ERROR),
                new Date()
            );
    }

    public void processExternalEvent(ExternalEvent event) {
        // TODO LH-303: maybe if the event has a `threadRunNumber` and
        // `nodeRunPosition` set, it should do some validation here?
        for (ThreadRun thread : threadRuns) {
            thread.processExternalEvent(event);
        }
        advance(event.getCreatedAt());
    }

    public void processStopRequest(StopWfRun req) throws LHValidationError {
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHValidationError(
                null,
                "Tried to stop a non-existent thread id."
            );
        }

        ThreadRun thread = threadRuns.get(req.threadRunNumber);

        // need to see if thread already is halted. If so, don't double halt it.
        for (ThreadHaltReason reason : thread.haltReasons) {
            if (reason.type == ReasonCase.MANUAL_HALT) {
                return;
            }
        }

        ThreadHaltReason haltReason = new ThreadHaltReason();
        haltReason.type = ReasonCase.MANUAL_HALT;
        haltReason.manualHalt = new ManualHalt();
        thread.halt(haltReason);
        this.advance(new Date()); // Seems like a good idea, why not?
    }

    public void processResumeRequest(ResumeWfRun req) throws LHValidationError {
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHValidationError(
                null,
                "Tried to resume a non-existent thread id."
            );
        }

        ThreadRun thread = threadRuns.get(req.threadRunNumber);

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
        if (req.threadRunNumber >= threadRuns.size() || req.threadRunNumber < 0) {
            throw new LHValidationError(null, "Reference to nonexistent thread.");
        }

        ThreadRun thread = threadRuns.get(req.threadRunNumber);

        if (req.nodeRunPosition > thread.currentNodePosition) {
            throw new LHValidationError(null, "Reference to nonexistent nodeRun");
        }

        thread.processSleepNodeMatured(req);
        advance(time);
    }

    private void setStatus(LHStatusPb status) {
        this.status = status;

        if (status.equals(LHStatusPb.COMPLETED) || status.equals(LHStatusPb.ERROR)) {
            LHTimer timer = new LHTimer();
            timer.topic = this.getDao().getCoreCmdTopic();
            timer.key = id;
            Date now = new Date();
            timer.maturationTime =
                DateUtils.addHours(now, this.wfSpec.retentionHours);
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
        LHStatusPb newStatus
    ) {
        // WfRun Status is determined by the Entrypoint Thread
        if (threadRunNumber == 0) {
            // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
            // killing (or waiting for) any child threads. To be determined based on threading design.
            if (newStatus == LHStatusPb.COMPLETED) {
                endTime = time;
                setStatus(LHStatusPb.COMPLETED);
                log.info("Completed WfRun {} at {} ", id, new Date());
            } else if (newStatus == LHStatusPb.ERROR) {
                endTime = time;
                setStatus(LHStatusPb.ERROR);
            }
        }

        // ThreadRuns depend on each other, for example Exception Handler Threads or
        // child threads, so we need to signal to the other threads that they might
        // want to wake up. Ding Ding Ding! Get out of bed.
        advance(time);
    }
}

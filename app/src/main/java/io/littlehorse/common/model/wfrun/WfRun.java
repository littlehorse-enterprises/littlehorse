package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.PendingFailureHandlerPb;
import io.littlehorse.common.proto.PendingInterruptPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.WfRunPb;
import io.littlehorse.common.proto.WfRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.CommandProcessorDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class WfRun extends GETable<WfRunPb> {

    public String id;
    public String wfSpecId;
    public String wfSpecName;
    public LHStatusPb status;
    public long lastUpdateOffset;
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

    @JsonIgnore
    public Date getCreatedAt() {
        return startTime;
    }

    public void initFrom(MessageOrBuilder p) {
        WfRunPbOrBuilder proto = (WfRunPbOrBuilder) p;
        id = proto.getId();
        wfSpecId = proto.getWfSpecId();
        wfSpecName = proto.getWfSpecName();
        status = proto.getStatus();
        lastUpdateOffset = proto.getLastUpdateOffset();
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

    /*
     * Returns true if this WfRun is currently running. Due to the inheritance
     * structure of threads, we can determine this by simply checking if the
     * entrypoint thread is running.
     */
    @JsonIgnore
    public boolean isRunning() {
        return threadRuns.get(0).isRunning();
    }

    @JsonIgnore
    public WfRunPb.Builder toProto() {
        WfRunPb.Builder out = WfRunPb
            .newBuilder()
            .setId(id)
            .setWfSpecId(wfSpecId)
            .setWfSpecName(wfSpecName)
            .setStatus(status)
            .setLastUpdateOffset(lastUpdateOffset)
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

    @JsonIgnore
    public Class<WfRunPb> getProtoBaseClass() {
        return WfRunPb.class;
    }

    @JsonIgnore
    @Override
    public String getObjectId() {
        return id;
    }

    @JsonIgnore
    @Override
    public String getPartitionKey() {
        return id;
    }

    @JsonIgnore
    public List<Tag> getTags() {
        List<Tag> out = Arrays.asList(
            new Tag(
                this,
                Pair.of("wfSpecName", wfSpecName),
                Pair.of("status", status.toString())
            ),
            new Tag(
                this,
                Pair.of("wfSpecId", wfSpecId),
                Pair.of("status", status.toString())
            )
        );

        return out;
    }

    // Below is used by scheduler

    @JsonIgnore
    public WfSpec wfSpec;

    @JsonIgnore
    public CommandProcessorDao stores;

    public ThreadRun startThread(
        String threadName,
        Date start,
        Integer parentThreadId,
        Map<String, VariableValue> variables
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
        thread.wfSpecId = wfSpecId;
        thread.threadSpecName = threadName;
        thread.currentNodePosition = -1; // this gets bumped when we start the thread

        thread.startTime = new Date();

        thread.wfRun = this;
        threadRuns.add(thread);

        try {
            tspec.validateStartVariables(variables);
        } catch (LHValidationError exn) {
            LHUtil.log("Invalid variables received");
            // Now we gotta figure out how to fail a workflow
            thread.fail(
                new Failure(
                    TaskResultCodePb.VAR_MUTATION_ERROR,
                    "Failed validating variables on start: " + exn.getMessage(),
                    LHConstants.VAR_MUTATION_ERROR
                ),
                thread.startTime
            );
            return thread;
        }

        for (Map.Entry<String, VariableDef> entry : tspec.variableDefs.entrySet()) {
            String varName = entry.getKey();
            VariableDef varDef = entry.getValue();
            VariableValue val;

            if (variables.containsKey(varName)) {
                val = variables.get(varName);
            } else if (varDef.defaultValue != null) {
                val = varDef.defaultValue;
            } else {
                val = new VariableValue();
                val.type = varDef.type;
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
                ExternalEvent event = stores.getExternalEvent(pi.externalEventId);
                vars.put(LHConstants.EXT_EVT_HANDLER_VAR, event.content);
            } else {
                vars = new HashMap<>();
            }
            ThreadRun interruptor = startThread(
                pi.handlerSpecName,
                time,
                pi.interruptedThreadId,
                vars
            );
            interruptor.interruptTriggerId = pi.externalEventId;

            if (interruptor.status == LHStatusPb.ERROR) {
                toInterrupt.fail(
                    new Failure(
                        TaskResultCodePb.FAILED,
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
                vars
            );

            fh.failureBeingHandled = new FailureBeingHandled();
            fh.failureBeingHandled.failureNumber =
                failedThr.getCurrentNodeRun().failures.size() - 1;
            fh.failureBeingHandled.nodeRunPosition = failedThr.currentNodePosition;
            fh.failureBeingHandled.threadRunNumber = pfh.failedThreadRun;

            if (fh.status == LHStatusPb.ERROR) {
                fh.fail(
                    new Failure(
                        TaskResultCodePb.FAILED,
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

    /*
     * TODO: This is kind of a mess and I haven't logically gone through to determine
     * how many of the loops are necessary. All we know is that "the tests pass"...
     * there may be some missing holes if our tests aren't complete; or there may
     * be some unnecessary computation. Need to go through and do a logical analysis.
     */
    public void processEvent(WfRunEvent e) {
        // Any structural changes to the threadruns
        for (ThreadRun thread : threadRuns) {
            thread.processEvent(e);
        }
        boolean statusChanged = false;
        // Update status and then advance
        for (ThreadRun thread : threadRuns) {
            statusChanged = thread.updateStatus() || statusChanged;
        }
        statusChanged = startXnHandlersAndInterrupts(e.time) || statusChanged;
        for (ThreadRun thread : threadRuns) {
            statusChanged = thread.advance(e.time) || statusChanged;
        }
        for (ThreadRun thread : threadRuns) {
            statusChanged = thread.updateStatus() || statusChanged;
        }

        while (statusChanged) {
            statusChanged = startXnHandlersAndInterrupts(e.time) || statusChanged;
            statusChanged = false;
            for (ThreadRun thread : threadRuns) {
                statusChanged = thread.advance(e.time) || statusChanged;
            }

            for (ThreadRun thread : threadRuns) {
                statusChanged = thread.updateStatus() || statusChanged;
            }
        }
    }

    // As a precondition, the status of the calling thread must already be updated to complete.
    public void handleThreadStatus(
        int threadRunNumber,
        Date time,
        LHStatusPb newStatus
    ) {
        if (threadRunNumber != 0) {
            // Nothing to do, since all we care about is the root thread.
            return;
        }

        // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
        // killing (or waiting for) any child threads. To be determined based on threading design.
        if (newStatus == LHStatusPb.COMPLETED) {
            endTime = time;
            status = LHStatusPb.COMPLETED;
        } else if (newStatus == LHStatusPb.ERROR) {
            endTime = time;
            status = LHStatusPb.ERROR;
        }
        // TODO: when there are multiple threads, we need to think about what happens when one thread
        // fails and others are still alive.
    }
}

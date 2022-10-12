package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.WfRunPb;
import io.littlehorse.common.proto.WfRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.processors.util.WfRunStoreAccess;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    public WfRun() {
        threadRuns = new ArrayList<>();
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
            new Tag(this, Pair.of("wfSpecName", wfSpecName)),
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
    public WfRunStoreAccess stores;

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
                TaskResultCodePb.VAR_MUTATION_ERROR,
                "Failed validating variables on start: " + exn.getMessage(),
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

    public void processEvent(WfRunEvent e) {
        for (ThreadRun thread : threadRuns) {
            thread.processEvent(e);
        }

        for (ThreadRun thread : threadRuns) {
            thread.advance(e.time);
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
/*

    public void oldProcessEvent(WfRunEvent e) {
        switch (e.type) {
            case RUN_REQUEST:
                throw new RuntimeException("Shouldn't happen here.");
            case TASK_RESULT:
                handleTaskResult(e);
                break;
            case STARTED_EVENT:
                handleStartedEvent(e);
                break;
            case EXTERNAL_EVENT:
                handleExternalEvent(e);
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException("Impossible or Out of date scheduler.");
        }
    }

    private void handleStartedEvent(WfRunEvent we) {
        TaskStartedEvent se = we.startedEvent;
        ThreadRun thread = threadRuns.get(se.threadRunNumber);
        thread.processStartedEvent(we);
        thread.advance(we.time);
    }

    private void handleTaskResult(WfRunEvent we) {
        TaskResultEvent ce = we.taskResult;
        ThreadRun thread = threadRuns.get(ce.threadRunNumber);
        thread.processCompletedEvent(we);
        thread.advance(we.time);
    }

    private void handleExternalEvent(WfRunEvent we) {
        // This doesn't need to do anything except advance the thread.
        if (we.externalEvent.threadRunNumber != null) {
            ThreadRun thread = threadRuns.get(we.externalEvent.threadRunNumber);
            if (thread != null) {
                thread.advance(we.time);
            } else {
                LHUtil.log("Warning: unknown threadrun for external event");
            }
        } else {
            for (ThreadRun thread : threadRuns) {
                thread.advance(we.time);
            }
        }
    }

 */

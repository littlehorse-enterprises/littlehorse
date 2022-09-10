package io.littlehorse.server.model.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.common.model.observability.ThreadStartOe;
import io.littlehorse.common.model.observability.WfRunStatusChangeOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.scheduler.ThreadRunStatePb;
import io.littlehorse.common.proto.scheduler.WfRunStatePb;
import io.littlehorse.common.proto.scheduler.WfRunStatePbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class WfRunState extends LHSerializable<WfRunStatePb> {
    public String id;
    public String wfSpecId;
    public String wfSpecName;
    public Date startTime;
    public Date endTime;
    public LHStatusPb status;

    public Map<Integer, ThreadRunState> threadRuns;

    public WfRunState() {
        threadRuns = new HashMap<>();
        oEvents = new ObservabilityEvents();
    }

    public Class<WfRunStatePb> getProtoBaseClass() {
        return WfRunStatePb.class;
    }

    // Below is Serialization/Deserialization stuff.
    public WfRunStatePb.Builder toProto() {
        WfRunStatePb.Builder b = WfRunStatePb.newBuilder()
            .setId(id)
            .setWfSpecId(wfSpecId)
            .setStatus(status);

        if (startTime != null) {
            b.setStartTime(LHUtil.fromDate(startTime));
        }
        if (endTime != null) {
            b.setEndTime(LHUtil.fromDate(endTime));
        }

        for (Map.Entry<Integer, ThreadRunState> entry: threadRuns.entrySet()) {
            b.putThreadRuns(entry.getKey(), entry.getValue().toProto().build());
        }
        return b;
    }

    public static WfRunState fromProto(WfRunStatePbOrBuilder proto) {
        WfRunState out = new WfRunState();
        out.initFrom(proto);
        return out;
    }

    public void initFrom(MessageOrBuilder p) {
        WfRunStatePbOrBuilder proto = (WfRunStatePbOrBuilder) p;
        this.id = proto.getId();
        this.oEvents.wfRunId = this.id;

        this.wfSpecId = proto.getWfSpecId();
        this.status = proto.getStatus();
        this.threadRuns = new HashMap<>();

        for (Map.Entry<Integer, ThreadRunStatePb> entry: proto.getThreadRunsMap().entrySet()) {
            ThreadRunState tr = ThreadRunState.fromProto(entry.getValue());
            tr.wfRun = this;
            this.threadRuns.put(entry.getKey(), tr);
        }
        if (proto.hasStartTime()) {
            this.startTime = LHUtil.fromProtoTs(proto.getStartTime());
        }
        if (proto.hasEndTime()) {
            this.endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
    }

    // All below is simply implementation.
    @JsonIgnore public WfSpec wfSpec;
    @JsonIgnore public List<TaskScheduleRequest> tasksToSchedule;
    @JsonIgnore public ObservabilityEvents oEvents;
    @JsonIgnore public List<LHTimer> timersToSchedule;

    public void startThread(
        String threadName,
        Date start,
        Integer parentThreadId,
        Map<String, VariableValue> variables
    ) {
        ThreadSpec tspec = wfSpec.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name, should be impossible");
        }

        oEvents.add(new ObservabilityEvent(
            new ThreadStartOe(threadRuns.size(), threadName),
            new Date()
        ));

        ThreadRunState thread = new ThreadRunState();
        thread.threadSpecName = threadName;
        thread.status = LHStatusPb.RUNNING;
        thread.threadRunNumber = threadRuns.size();
        thread.wfRun = this;
        threadRuns.put(thread.threadRunNumber, thread);

        try {
            tspec.validateStartVariables(variables);
            thread.advance(start);
        } catch(LHValidationError exn) {
            LHUtil.log("Invalid variables received");
            // Now we gotta figure out how to fail a workflow
            thread.setStatus(LHStatusPb.ERROR);
        }
    }

    public void processEvent(
        WfRunEvent e,
        List<TaskScheduleRequest> tasksToSchedule,
        List<LHTimer> timersToSchedule
    ) {
        this.timersToSchedule = timersToSchedule;
        this.tasksToSchedule = tasksToSchedule;

        switch(e.type) {
        case RUN_REQUEST:
            throw new RuntimeException("Shouldn't happen here.");
        case TASK_RESULT:
            handleTaskResult(e);
            break;
        case STARTED_EVENT:
            handleStartedEvent(e);
            break;
        case EVENT_NOT_SET:
            throw new RuntimeException("Impossible or Out of date scheduler.");
        }
    }

    // As a precondition, the status of the calling thread must already be updated to complete.
    public void handleThreadStatus(int threadRunNumber, Date time, LHStatusPb newStatus) {
        if (threadRunNumber != 0) {
            throw new RuntimeException("TODO: Support threading.");
        }
        // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
        // killing (or waiting for) any child threads. To be determined based on threading design.
        if (newStatus == LHStatusPb.COMPLETED) {
            endTime = time;
            status = LHStatusPb.COMPLETED;

            oEvents.add(
                new ObservabilityEvent(
                    new WfRunStatusChangeOe(status),
                    time
                )
            );
        } else if (newStatus == LHStatusPb.ERROR) {
            endTime = time;
            status = LHStatusPb.ERROR;
            oEvents.add(
                new ObservabilityEvent(
                    new WfRunStatusChangeOe(status),
                    time
                )
            );
        }

        // TODO: when there are multiple threads, we need to think about what happens when one thread
        // fails and others are still alive.
    }

    private void handleStartedEvent(WfRunEvent we) {
        TaskStartedEvent se = we.startedEvent;
        ThreadRunState thread = threadRuns.get(se.threadRunNumber);
        thread.processStartedEvent(we);
        thread.advance(we.time);
    }

    private void handleTaskResult(WfRunEvent we) {
        TaskResultEvent ce = we.taskResult;
        ThreadRunState thread = threadRuns.get(ce.threadRunNumber);
        thread.processCompletedEvent(we);
        thread.advance(we.time);
    }
}

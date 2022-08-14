package io.littlehorse.scheduler.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
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

    public List<ThreadRunState> threadRuns;

    public WfRunState() {
        threadRuns = new ArrayList<>();
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

        for (ThreadRunState t : threadRuns) {
            b.addThreadRuns(t.toProto());
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
        this.threadRuns = new ArrayList<>();

        for (ThreadRunStatePb tpb : proto.getThreadRunsList()) {
            ThreadRunState tr = ThreadRunState.fromProto(tpb);
            tr.wfRun = this;
            this.threadRuns.add(tr);
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
    @JsonIgnore public List<SchedulerTimer> timersToSchedule;

    public void startThread(String threadName, Date start, Integer parentThreadId) {
        ThreadSpec tspec = wfSpec.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException("Invalid thread name");
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
        threadRuns.add(thread);

        thread.advance();
    }

    public void processEvent(
        WfRunEvent e,
        List<TaskScheduleRequest> tasksToSchedule,
        List<SchedulerTimer> timersToSchedule
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

    public void complete(Date time) {
        endTime = time;
        status = LHStatusPb.COMPLETED;

        oEvents.add(
            new ObservabilityEvent(
                new WfRunStatusChangeOe(status),
                time
            )
        );
    }

    private void handleStartedEvent(WfRunEvent we) {

        TaskStartedEvent se = we.startedEvent;
        ThreadRunState thread = threadRuns.get(se.threadRunNumber);
        thread.processStartedEvent(we);
        thread.advance();
    }

    private void handleTaskResult(WfRunEvent we) {
        TaskResultEvent ce = we.taskResult;
        ThreadRunState thread = threadRuns.get(ce.threadRunNumber);
        thread.processCompletedEvent(we);
        thread.advance();
    }
}

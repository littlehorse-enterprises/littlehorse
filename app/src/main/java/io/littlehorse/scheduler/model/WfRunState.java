package io.littlehorse.scheduler.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.event.TaskCompletedEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observability.ObservabilityEvent;
import io.littlehorse.common.model.observability.ObservabilityEvents;
import io.littlehorse.common.model.observability.ThreadStartOe;
import io.littlehorse.common.model.observability.WfRunStatusChangeOe;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadRunStatePb;
import io.littlehorse.common.proto.WFRunPb;
import io.littlehorse.common.proto.WFRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;

public class WfRunState {
    public String id;
    public String wfSpecId;
    public String wfSpecName;
    public Date startTime;
    public Date endTime;
    public LHStatusPb status;

    public List<ThreadRunState> threadRuns;

    public WfRunState(String id) {
        this.id = id;
        threadRuns = new ArrayList<>();
        oEvents = new ObservabilityEvents();
        oEvents.wfRunId = id;
    }

    // Below is Serialization/Deserialization stuff.
    public WFRunPb.Builder toProtoBuilder() {
        WFRunPb.Builder b = WFRunPb.newBuilder()
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
            b.addThreadRuns(t.toProtoBuilder());
        }
        return b;
    }

    public static WfRunState fromProto(WFRunPbOrBuilder proto) {
        WfRunState out = new WfRunState(proto.getId());
        out.wfSpecId = proto.getWfSpecId();
        out.status = proto.getStatus();
        out.threadRuns = new ArrayList<>();

        for (ThreadRunStatePb tpb : proto.getThreadRunsList()) {
            ThreadRunState tr = ThreadRunState.fromProto(tpb);
            tr.wfRun = out;
            out.threadRuns.add(tr);
        }
        if (proto.hasStartTime()) {
            out.startTime = LHUtil.fromProtoTs(proto.getStartTime());
        }
        if (proto.hasEndTime()) {
            out.endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
        return out;
    }

    // All below is simply implementation.
    @JsonIgnore public WfSpec wfSpec;
    @JsonIgnore public List<TaskScheduleRequest> toSchedule;
    @JsonIgnore public ObservabilityEvents oEvents;

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

    public void processEvent(WFRunEvent e, List<TaskScheduleRequest> toSchedule) {
        this.toSchedule = toSchedule;

        switch(e.type) {
        case RUN_REQUEST:
            throw new RuntimeException("Shouldn't happen here.");
        case COMPLETED_EVENT:
            handleCompletedEvent(e);
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

    private void handleStartedEvent(WFRunEvent we) {

        TaskStartedEvent se = we.startedEvent;
        ThreadRunState thread = threadRuns.get(se.threadRunNumber);
        thread.processStartedEvent(we);
        thread.advance();
    }

    private void handleCompletedEvent(WFRunEvent we) {
        TaskCompletedEvent ce = we.completedEvent;
        ThreadRunState thread = threadRuns.get(ce.threadRunNumber);
        thread.processCompletedEvent(we);
        thread.advance();
    }
}

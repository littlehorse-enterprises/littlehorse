package io.littlehorse.common.model.wfrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GETable;
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
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.LHStatusPb;
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
        oEvents = new ObservabilityEvents();
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

        oEvents.wfRunId = id;
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
            new Tag(this, Pair.of("wfSpecId", wfSpecId)),
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
    public List<TaskScheduleRequest> tasksToSchedule;

    @JsonIgnore
    public WfRunStoreAccess stores;

    @JsonIgnore
    public ObservabilityEvents oEvents;

    @JsonIgnore
    public List<LHTimer> timersToSchedule;

    public void startThread(
        String threadName,
        Date start,
        Integer parentThreadId,
        Map<String, VariableValue> variables
    ) {
        ThreadSpec tspec = wfSpec.threadSpecs.get(threadName);
        if (tspec == null) {
            throw new RuntimeException(
                "Invalid thread name, should be impossible"
            );
        }

        oEvents.add(
            new ObservabilityEvent(
                new ThreadStartOe(threadRuns.size(), threadName),
                new Date()
            )
        );

        ThreadRun thread = new ThreadRun();
        thread.wfRunId = id;
        thread.number = threadRuns.size();

        thread.status = LHStatusPb.RUNNING;
        thread.wfSpecId = wfSpecId;
        thread.threadSpecName = threadName;
        thread.numSteps = 0;

        thread.startTime = new Date();

        thread.wfRun = this;
        threadRuns.add(thread);

        try {
            tspec.validateStartVariables(variables);
            thread.advance(start);
        } catch (LHValidationError exn) {
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

        switch (e.type) {
            case RUN_REQUEST:
                throw new RuntimeException("Shouldn't happen here.");
            case TASK_RESULT:
                handleTaskResult(e);
                break;
            case STARTED_EVENT:
                handleStartedEvent(e);
                break;
            case EVENT_NOT_SET:
                throw new RuntimeException(
                    "Impossible or Out of date scheduler."
                );
        }
    }

    // As a precondition, the status of the calling thread must already be updated to complete.
    public void handleThreadStatus(
        int threadRunNumber,
        Date time,
        LHStatusPb newStatus
    ) {
        if (threadRunNumber != 0) {
            throw new RuntimeException("TODO: Support threading.");
        }
        // TODO: In the future, there may be some other lifecycle hooks here, such as forcibly
        // killing (or waiting for) any child threads. To be determined based on threading design.
        if (newStatus == LHStatusPb.COMPLETED) {
            endTime = time;
            status = LHStatusPb.COMPLETED;

            oEvents.add(
                new ObservabilityEvent(new WfRunStatusChangeOe(status), time)
            );
        } else if (newStatus == LHStatusPb.ERROR) {
            endTime = time;
            status = LHStatusPb.ERROR;
            oEvents.add(
                new ObservabilityEvent(new WfRunStatusChangeOe(status), time)
            );
        }
        // TODO: when there are multiple threads, we need to think about what happens when one thread
        // fails and others are still alive.
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
}

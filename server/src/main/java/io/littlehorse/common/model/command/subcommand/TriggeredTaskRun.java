package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.UserTaskEvent;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.TriggeredTaskRunPb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb.EventCase;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TriggeredTaskRun extends SubCommand<TriggeredTaskRunPb> {

    public ScheduledTask scheduledTask;

    public Class<TriggeredTaskRunPb> getProtoBaseClass() {
        return TriggeredTaskRunPb.class;
    }

    public TriggeredTaskRunPb.Builder toProto() {
        TriggeredTaskRunPb.Builder out = TriggeredTaskRunPb
            .newBuilder()
            .setTaskToSchedule(scheduledTask.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        TriggeredTaskRunPb p = (TriggeredTaskRunPb) proto;
        scheduledTask = ScheduledTask.fromProto(p.getTaskToSchedule());
    }

    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        String wfRunId = scheduledTask.wfRunId;
        log.info(
            "Might schedule a one-off task for wfRun {} due to UserTask",
            wfRunId
        );
        WfRun wfRun = dao.getWfRun(wfRunId);
        if (wfRun == null) {
            log.info("WfRun no longer exists! Skipping the scheduled action trigger");
            return null;
        }

        // Now verify that the thing hasn't yet been completed.
        ThreadRun thread = wfRun.threadRuns.get(scheduledTask.threadRunNumber);

        // Impossible for thread to be null, but check anyways
        if (thread == null) {
            log.warn("Triggered scheduled task refers to missing thread!");
            return null;
        }

        // Get the NodeRun
        NodeRun userTaskNR = thread.getNodeRun(scheduledTask.taskRunPosition);

        if (userTaskNR.status != LHStatusPb.RUNNING) {
            log.info("NodeRun is not RUNNING anymore, so can't take action!");
            return null;
        }

        // Last thing to do: we need to put the "User Task Action Id"
        UserTaskEvent event = new UserTaskEvent();
        event.type = EventCase.TASK_EXECUTED;
        event.time = new Date();
        // TODO LH-339: Enable Audit Logging, then add this to the events.
        // event.executed = null;

        log.info("Scheduling a one-off task for wfRun {} due to UserTask", wfRunId);

        dao.scheduleTask(scheduledTask);
        return null;
    }

    public String getPartitionKey() {
        return scheduledTask.wfRunId;
    }

    public boolean hasResponse() {
        // as of now, it does not;
        return false;
    }
}

package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.meta.subnode.TaskNode;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.TaskRunId;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.TaskAttempt;
import io.littlehorse.common.model.wfrun.ThreadRun;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.common.model.wfrun.taskrun.UserTaskTriggerReference;
import io.littlehorse.common.model.wfrun.usertaskevent.UTETaskExecuted;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEvent;
import io.littlehorse.common.proto.TriggeredTaskRunPb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class TriggeredTaskRun extends SubCommand<TriggeredTaskRunPb> {

    private TaskNode taskToSchedule;
    private NodeRunId source;

    public TriggeredTaskRun() {}

    public TriggeredTaskRun(TaskNode taskToSchedule, NodeRunId source) {
        this.source = source;
        this.taskToSchedule = taskToSchedule;
    }

    public Class<TriggeredTaskRunPb> getProtoBaseClass() {
        return TriggeredTaskRunPb.class;
    }

    public TriggeredTaskRunPb.Builder toProto() {
        TriggeredTaskRunPb.Builder out = TriggeredTaskRunPb
            .newBuilder()
            .setTaskToSchedule(taskToSchedule.toProto())
            .setSource(source.toProto());

        return out;
    }

    public void initFrom(Message proto) {
        TriggeredTaskRunPb p = (TriggeredTaskRunPb) proto;
        taskToSchedule =
            LHSerializable.fromProto(p.getTaskToSchedule(), TaskNode.class);
        source = LHSerializable.fromProto(p.getSource(), NodeRunId.class);
    }

    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        taskToSchedule.setDao(dao);
        String wfRunId = source.getWfRunId();

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
        ThreadRun thread = wfRun.threadRuns.get(source.getThreadRunNumber());

        // Impossible for thread to be null, but check anyways
        if (thread == null) {
            log.warn("Triggered scheduled task refers to missing thread!");
            return null;
        }

        // Get the NodeRun
        NodeRun userTaskNR = dao.getNodeRun(source);

        if (userTaskNR.status != LHStatusPb.RUNNING) {
            log.info("NodeRun is not RUNNING anymore, so can't take action!");
            return null;
        }

        // At this point, need to update the events.
        log.info("Scheduling a one-off task for wfRun {} due to UserTask", wfRunId);

        try {
            List<VarNameAndVal> inputVars = taskToSchedule.assignInputVars(thread);
            TaskRunId taskRunId = new TaskRunId(wfRunId);
            ScheduledTask toSchedule = new ScheduledTask(
                taskToSchedule.getTaskDef().getObjectId(),
                inputVars,
                userTaskNR.getUserTaskRun()
            );
            toSchedule.setTaskRunId(taskRunId);

            TaskRun taskRun = new TaskRun(
                dao,
                inputVars,
                new TaskRunSource(
                    new UserTaskTriggerReference(userTaskNR.getUserTaskRun())
                ),
                taskToSchedule
            );
            taskRun.setId(taskRunId);
            taskRun.getAttempts().add(new TaskAttempt());
            dao.putTaskRun(taskRun);
            dao.scheduleTask(toSchedule);

            userTaskNR
                .getUserTaskRun()
                .getEvents()
                .add(new UserTaskEvent(new UTETaskExecuted(taskRunId), new Date()));

            dao.putNodeRun(userTaskNR); // should be unnecessary
        } catch (LHVarSubError exn) {
            log.error(
                "Failed scheduling a Triggered Task Run, but the WfRun will continue",
                exn
            );
        }
        return null;
    }

    public String getPartitionKey() {
        return source.getWfRunId();
    }

    public boolean hasResponse() {
        // Triggered Task Runs are sent by the LHTimer infrastructure, which means
        // there is no actual client waiting for the response.
        return false;
    }
}

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
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.TaskAttempt;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRun;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSource;
import io.littlehorse.common.model.wfrun.taskrun.UserTaskTriggerReference;
import io.littlehorse.common.model.wfrun.usertaskevent.UTETaskExecuted;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEvent;
import io.littlehorse.common.proto.TriggeredTaskRunPb;
import io.littlehorse.sdk.common.proto.LHStatus;
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

    @Override
    public Class<TriggeredTaskRunPb> getProtoBaseClass() {
        return TriggeredTaskRunPb.class;
    }

    @Override
    public TriggeredTaskRunPb.Builder toProto() {
        TriggeredTaskRunPb.Builder out = TriggeredTaskRunPb
            .newBuilder()
            .setTaskToSchedule(taskToSchedule.toProto())
            .setSource(source.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto) {
        TriggeredTaskRunPb p = (TriggeredTaskRunPb) proto;
        taskToSchedule =
            LHSerializable.fromProto(p.getTaskToSchedule(), TaskNode.class);
        source = LHSerializable.fromProto(p.getSource(), NodeRunId.class);
    }

    @Override
    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        taskToSchedule.setDao(dao);
        String wfRunId = source.getWfRunId();

        log.info(
            "Might schedule a one-off task for wfRun {} due to UserTask",
            wfRunId
        );
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            log.info("WfRun no longer exists! Skipping the scheduled action trigger");
            return null;
        }

        // Now verify that the thing hasn't yet been completed.
        ThreadRunModel thread = wfRunModel.threadRunModels.get(
            source.getThreadRunNumber()
        );

        // Impossible for thread to be null, but check anyways
        if (thread == null) {
            log.warn("Triggered scheduled task refers to missing thread!");
            return null;
        }

        // Get the NodeRun
        NodeRunModel userTaskNR = dao.getNodeRun(source);
        UserTaskRunId userTaskRunId = userTaskNR.getUserTaskRun().getUserTaskRunId();
        UserTaskRun userTaskRun = dao.getUserTaskRun(userTaskRunId);

        if (userTaskNR.status != LHStatus.RUNNING) {
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
                userTaskRun,
                userTaskRun.buildTaskContext()
            );
            toSchedule.setTaskRunId(taskRunId);

            TaskRun taskRun = new TaskRun(
                dao,
                inputVars,
                new TaskRunSource(new UserTaskTriggerReference(userTaskRun)),
                taskToSchedule
            );
            taskRun.setId(taskRunId);
            taskRun.getAttempts().add(new TaskAttempt());
            dao.putTaskRun(taskRun);
            dao.scheduleTask(toSchedule);

            userTaskRun
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

    @Override
    public String getPartitionKey() {
        return source.getWfRunId();
    }

    @Override
    public boolean hasResponse() {
        // Triggered Task Runs are sent by the LHTimer infrastructure, which means
        // there is no actual client waiting for the response.
        return false;
    }
}

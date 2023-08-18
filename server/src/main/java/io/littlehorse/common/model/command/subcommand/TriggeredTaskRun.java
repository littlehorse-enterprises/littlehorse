package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.AbstractResponse;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.meta.subnode.TaskNodeModel;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.ScheduledTaskModel;
import io.littlehorse.common.model.wfrun.TaskAttemptModel;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.common.model.wfrun.VarNameAndValModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.wfrun.taskrun.UserTaskTriggerReferenceModel;
import io.littlehorse.common.model.wfrun.usertaskevent.UTETaskExecutedModel;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEventModel;
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

    private TaskNodeModel taskToSchedule;
    private NodeRunIdModel source;

    public TriggeredTaskRun() {}

    public TriggeredTaskRun(TaskNodeModel taskToSchedule, NodeRunIdModel source) {
        this.source = source;
        this.taskToSchedule = taskToSchedule;
    }

    @Override
    public Class<TriggeredTaskRunPb> getProtoBaseClass() {
        return TriggeredTaskRunPb.class;
    }

    @Override
    public TriggeredTaskRunPb.Builder toProto() {
        TriggeredTaskRunPb.Builder out =
                TriggeredTaskRunPb.newBuilder()
                        .setTaskToSchedule(taskToSchedule.toProto())
                        .setSource(source.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto) {
        TriggeredTaskRunPb p = (TriggeredTaskRunPb) proto;
        taskToSchedule = LHSerializable.fromProto(p.getTaskToSchedule(), TaskNodeModel.class);
        source = LHSerializable.fromProto(p.getSource(), NodeRunIdModel.class);
    }

    @Override
    public AbstractResponse<?> process(LHDAO dao, LHConfig config) {
        taskToSchedule.setDao(dao);
        String wfRunId = source.getWfRunId();

        log.info("Might schedule a one-off task for wfRun {} due to UserTask", wfRunId);
        WfRunModel wfRunModel = dao.getWfRun(wfRunId);
        if (wfRunModel == null) {
            log.info("WfRun no longer exists! Skipping the scheduled action trigger");
            return null;
        }

        // Now verify that the thing hasn't yet been completed.
        ThreadRunModel thread = wfRunModel.threadRunModels.get(source.getThreadRunNumber());

        // Impossible for thread to be null, but check anyways
        if (thread == null) {
            log.warn("Triggered scheduled task refers to missing thread!");
            return null;
        }

        // Get the NodeRun
        NodeRunModel userTaskNR = dao.getNodeRun(source);
        UserTaskRunIdModel userTaskRunId = userTaskNR.getUserTaskRun().getUserTaskRunId();
        UserTaskRunModel userTaskRun = dao.getUserTaskRun(userTaskRunId);

        if (userTaskNR.status != LHStatus.RUNNING) {
            log.info("NodeRun is not RUNNING anymore, so can't take action!");
            return null;
        }

        // At this point, need to update the events.
        log.info("Scheduling a one-off task for wfRun {} due to UserTask", wfRunId);

        try {
            List<VarNameAndValModel> inputVars = taskToSchedule.assignInputVars(thread);
            TaskRunIdModel taskRunId = new TaskRunIdModel(wfRunId);

            ScheduledTaskModel toSchedule =
                    new ScheduledTaskModel(
                            taskToSchedule.getTaskDef().getObjectId(),
                            inputVars,
                            userTaskRun,
                            userTaskRun.buildTaskContext());
            toSchedule.setTaskRunId(taskRunId);

            TaskRunModel taskRun =
                    new TaskRunModel(
                            dao,
                            inputVars,
                            new TaskRunSourceModel(new UserTaskTriggerReferenceModel(userTaskRun)),
                            taskToSchedule);
            taskRun.setId(taskRunId);
            taskRun.getAttempts().add(new TaskAttemptModel());
            dao.putTaskRun(taskRun);
            dao.scheduleTask(toSchedule);

            userTaskRun
                    .getEvents()
                    .add(new UserTaskEventModel(new UTETaskExecutedModel(taskRunId), new Date()));

            dao.putNodeRun(userTaskNR); // should be unnecessary
        } catch (LHVarSubError exn) {
            log.error("Failed scheduling a Triggered Task Run, but the WfRun will continue", exn);
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

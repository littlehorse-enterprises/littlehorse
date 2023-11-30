package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskAttemptModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunSourceModel;
import io.littlehorse.common.model.getable.core.taskrun.UserTaskTriggerReferenceModel;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTETaskExecutedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.TaskNodeModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.TriggeredTaskRunPb;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class TriggeredTaskRun extends CoreSubCommand<TriggeredTaskRunPb> {

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
        TriggeredTaskRunPb.Builder out = TriggeredTaskRunPb.newBuilder()
                .setTaskToSchedule(taskToSchedule.toProto())
                .setSource(source.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TriggeredTaskRunPb p = (TriggeredTaskRunPb) proto;
        taskToSchedule = LHSerializable.fromProto(p.getTaskToSchedule(), TaskNodeModel.class, context);
        source = LHSerializable.fromProto(p.getSource(), NodeRunIdModel.class, context);
    }

    @Override
    public Empty process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        taskToSchedule.setDao(dao);
        WfRunIdModel wfRunId = source.getWfRunId();

        log.info("Might schedule a one-off task for wfRun {} due to UserTask", wfRunId);
        WfRunModel wfRunModel = dao.get(wfRunId);
        if (wfRunModel == null) {
            log.info("WfRun no longer exists! Skipping the scheduled action trigger");
            return null;
        }

        // Now verify that the thing hasn't yet been completed.
        ThreadRunModel thread = wfRunModel.getThreadRun(source.getThreadRunNumber());

        // Impossible for thread to be null, but check anyways
        if (thread == null) {
            log.warn("Triggered scheduled task refers to missing thread!");
            return null;
        }

        // Get the NodeRun
        NodeRunModel userTaskNR = executionContext.getableManager().get(source);
        UserTaskRunIdModel userTaskRunId = userTaskNR.getUserTaskRun().getUserTaskRunId();
        UserTaskRunModel userTaskRun = executionContext.getableManager().get(userTaskRunId);

        if (userTaskNR.status != LHStatus.RUNNING) {
            log.info("NodeRun is not RUNNING anymore, so can't take action!");
            return null;
        }

        // At this point, need to update the events.
        log.info("Scheduling a one-off task for wfRun {} due to UserTask", wfRunId);

        try {
            List<VarNameAndValModel> inputVars = taskToSchedule.assignInputVars(thread);
            TaskRunIdModel taskRunId = new TaskRunIdModel(wfRunId, executionContext);

            ScheduledTaskModel toSchedule = new ScheduledTaskModel(
                    taskToSchedule.getTaskDef().getObjectId(), inputVars, userTaskRun, executionContext);
            toSchedule.setTaskRunId(taskRunId);

            TaskRunModel taskRun = new TaskRunModel(
                    inputVars,
                    new TaskRunSourceModel(
                            new UserTaskTriggerReferenceModel(userTaskRun, executionContext), executionContext),
                    taskToSchedule,
                    executionContext);
            taskRun.setId(taskRunId);
            taskRun.getAttempts().add(new TaskAttemptModel());
            executionContext.getableManager().put(taskRun);
            executionContext.getTaskManager().scheduleTask(toSchedule);

            userTaskRun.getEvents().add(new UserTaskEventModel(new UTETaskExecutedModel(taskRunId), new Date()));

            executionContext.getableManager().put(userTaskNR); // should be unnecessary
        } catch (LHVarSubError exn) {
            log.error("Failed scheduling a Triggered Task Run, but the WfRun will continue", exn);
        }
        return Empty.getDefaultInstance();
    }

    @Override
    public String getPartitionKey() {
        return source.getWfRunId().getPartitionKey().get();
    }

    @Override
    public boolean hasResponse() {
        // Triggered Task Runs are sent by the LHTimer infrastructure, which means
        // there is no actual client waiting for the response.
        return false;
    }
}

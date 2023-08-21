package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.corecommand.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ReassignUserTask;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TriggeredTaskRun;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.Command.CommandCase;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandModel extends AbstractCommand<Command> {

    public String commandId;
    public Date time;

    public CommandCase type;
    public ReportTaskRunModel reportTaskRun;
    public TaskClaimEvent taskClaimEvent;
    public PutExternalEventRequestModel putExternalEventRequest;
    public RunWfRequestModel runWf;
    public StopWfRunRequestModel stopWfRun;
    public ResumeWfRunRequestModel resumeWfRun;
    public SleepNodeMatured sleepNodeMatured;
    public DeleteWfRunRequestModel deleteWfRun;
    public ExternalEventTimeout externalEventTimeout;
    public TaskWorkerHeartBeatRequestModel taskWorkerHeartBeat;
    public DeleteExternalEventRequestModel deleteExternalEvent;
    public AssignUserTaskRunRequestModel assignUserTaskRun;
    public CompleteUserTaskRunRequestModel completeUserTaskRun;
    public TriggeredTaskRun triggeredTaskRun;
    private ReassignUserTask reassignUserTask;
    private CancelUserTaskRunRequestModel cancelUserTaskRun;

    public Class<Command> getProtoBaseClass() {
        return Command.class;
    }

    public CommandModel() {}

    public CommandModel(SubCommand<?> cmd) {
        this.time = new Date();
        this.setSubCommand(cmd);
    }

    public CommandModel(SubCommand<?> cmd, Date time) {
        this.time = time;
        this.setSubCommand(cmd);
    }

    @Override
    public Command.Builder toProto() {
        Command.Builder out = Command.newBuilder();
        out.setTime(LHUtil.fromDate(time));

        if (commandId != null) {
            out.setCommandId(commandId);
        }

        switch (type) {
            case REPORT_TASK_RUN:
                out.setReportTaskRun(reportTaskRun.toProto());
                break;
            case TASK_CLAIM_EVENT:
                out.setTaskClaimEvent(taskClaimEvent.toProto());
                break;
            case PUT_EXTERNAL_EVENT:
                out.setPutExternalEvent(putExternalEventRequest.toProto());
                break;
            case RUN_WF:
                out.setRunWf(runWf.toProto());
                break;
            case STOP_WF_RUN:
                out.setStopWfRun(stopWfRun.toProto());
                break;
            case RESUME_WF_RUN:
                out.setResumeWfRun(resumeWfRun.toProto());
                break;
            case SLEEP_NODE_MATURED:
                out.setSleepNodeMatured(sleepNodeMatured.toProto());
                break;
            case DELETE_WF_RUN:
                out.setDeleteWfRun(deleteWfRun.toProto());
                break;
            case EXTERNAL_EVENT_TIMEOUT:
                out.setExternalEventTimeout(externalEventTimeout.toProto());
                break;
            case TASK_WORKER_HEART_BEAT:
                out.setTaskWorkerHeartBeat(taskWorkerHeartBeat.toProto());
                break;
            case DELETE_EXTERNAL_EVENT:
                out.setDeleteExternalEvent(deleteExternalEvent.toProto());
                break;
            case ASSIGN_USER_TASK_RUN:
                out.setAssignUserTaskRun(assignUserTaskRun.toProto());
                break;
            case COMPLETE_USER_TASK_RUN:
                out.setCompleteUserTaskRun(completeUserTaskRun.toProto());
                break;
            case TRIGGERED_TASK_RUN:
                out.setTriggeredTaskRun(triggeredTaskRun.toProto());
                break;
            case REASSIGNED_USER_TASK:
                out.setReassignedUserTask(reassignUserTask.toProto());
                break;
            case CANCEL_USER_TASK:
                out.setCancelUserTask(cancelUserTaskRun.toProto());
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        Command p = (Command) proto;
        time = LHUtil.fromProtoTs(p.getTime());

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }

        type = p.getCommandCase();
        switch (type) {
            case REPORT_TASK_RUN:
                reportTaskRun = ReportTaskRunModel.fromProto(p.getReportTaskRun());
                break;
            case TASK_CLAIM_EVENT:
                taskClaimEvent = TaskClaimEvent.fromProto(p.getTaskClaimEvent());
                break;
            case PUT_EXTERNAL_EVENT:
                putExternalEventRequest = PutExternalEventRequestModel.fromProto(p.getPutExternalEvent());
                break;
            case RUN_WF:
                runWf = RunWfRequestModel.fromProto(p.getRunWf());
                break;
            case STOP_WF_RUN:
                stopWfRun = StopWfRunRequestModel.fromProto(p.getStopWfRun());
                break;
            case RESUME_WF_RUN:
                resumeWfRun = ResumeWfRunRequestModel.fromProto(p.getResumeWfRun());
                break;
            case SLEEP_NODE_MATURED:
                sleepNodeMatured = SleepNodeMatured.fromProto(p.getSleepNodeMatured());
                break;
            case DELETE_WF_RUN:
                deleteWfRun = DeleteWfRunRequestModel.fromProto(p.getDeleteWfRun());
                break;
            case EXTERNAL_EVENT_TIMEOUT:
                externalEventTimeout = ExternalEventTimeout.fromProto(p.getExternalEventTimeout());
                break;
            case TASK_WORKER_HEART_BEAT:
                taskWorkerHeartBeat = TaskWorkerHeartBeatRequestModel.fromProto(p.getTaskWorkerHeartBeat());
                break;
            case DELETE_EXTERNAL_EVENT:
                deleteExternalEvent = DeleteExternalEventRequestModel.fromProto(p.getDeleteExternalEvent());
                break;
            case ASSIGN_USER_TASK_RUN:
                assignUserTaskRun =
                        LHSerializable.fromProto(p.getAssignUserTaskRun(), AssignUserTaskRunRequestModel.class);
                break;
            case COMPLETE_USER_TASK_RUN:
                completeUserTaskRun =
                        LHSerializable.fromProto(p.getCompleteUserTaskRun(), CompleteUserTaskRunRequestModel.class);
                break;
            case TRIGGERED_TASK_RUN:
                triggeredTaskRun = LHSerializable.fromProto(p.getTriggeredTaskRun(), TriggeredTaskRun.class);
                break;
            case REASSIGNED_USER_TASK:
                reassignUserTask = LHSerializable.fromProto(p.getReassignedUserTask(), ReassignUserTask.class);
                break;
            case CANCEL_USER_TASK:
                cancelUserTaskRun =
                        LHSerializable.fromProto(p.getCancelUserTask(), CancelUserTaskRunRequestModel.class);
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public SubCommand<?> getSubCommand() {
        switch (type) {
            case REPORT_TASK_RUN:
                return reportTaskRun;
            case TASK_CLAIM_EVENT:
                return taskClaimEvent;
            case PUT_EXTERNAL_EVENT:
                return putExternalEventRequest;
            case RUN_WF:
                return runWf;
            case STOP_WF_RUN:
                return stopWfRun;
            case RESUME_WF_RUN:
                return resumeWfRun;
            case SLEEP_NODE_MATURED:
                return sleepNodeMatured;
            case DELETE_WF_RUN:
                return deleteWfRun;
            case EXTERNAL_EVENT_TIMEOUT:
                return externalEventTimeout;
            case TASK_WORKER_HEART_BEAT:
                return taskWorkerHeartBeat;
            case DELETE_EXTERNAL_EVENT:
                return deleteExternalEvent;
            case ASSIGN_USER_TASK_RUN:
                return assignUserTaskRun;
            case COMPLETE_USER_TASK_RUN:
                return completeUserTaskRun;
            case TRIGGERED_TASK_RUN:
                return triggeredTaskRun;
            case REASSIGNED_USER_TASK:
                return reassignUserTask;
            case CANCEL_USER_TASK:
                return cancelUserTaskRun;
            case COMMAND_NOT_SET:
        }
        throw new IllegalStateException("Not possible to have missing subcommand.");
    }

    public void setSubCommand(SubCommand<?> cmd) {
        Class<?> cls = cmd.getClass();
        if (cls.equals(RunWfRequestModel.class)) {
            type = CommandCase.RUN_WF;
            runWf = (RunWfRequestModel) cmd;
        } else if (cls.equals(PutExternalEventRequestModel.class)) {
            type = CommandCase.PUT_EXTERNAL_EVENT;
            putExternalEventRequest = (PutExternalEventRequestModel) cmd;
        } else if (cls.equals(ReportTaskRunModel.class)) {
            type = CommandCase.REPORT_TASK_RUN;
            reportTaskRun = (ReportTaskRunModel) cmd;
        } else if (cls.equals(TaskClaimEvent.class)) {
            type = CommandCase.TASK_CLAIM_EVENT;
            taskClaimEvent = (TaskClaimEvent) cmd;
        } else if (cls.equals(StopWfRunRequestModel.class)) {
            type = CommandCase.STOP_WF_RUN;
            stopWfRun = (StopWfRunRequestModel) cmd;
        } else if (cls.equals(ResumeWfRunRequestModel.class)) {
            type = CommandCase.RESUME_WF_RUN;
            resumeWfRun = (ResumeWfRunRequestModel) cmd;
        } else if (cls.equals(SleepNodeMatured.class)) {
            type = CommandCase.SLEEP_NODE_MATURED;
            sleepNodeMatured = (SleepNodeMatured) cmd;
        } else if (cls.equals(DeleteWfRunRequestModel.class)) {
            type = CommandCase.DELETE_WF_RUN;
            deleteWfRun = (DeleteWfRunRequestModel) cmd;
        } else if (cls.equals(ExternalEventTimeout.class)) {
            type = CommandCase.EXTERNAL_EVENT_TIMEOUT;
            externalEventTimeout = (ExternalEventTimeout) cmd;
        } else if (cls.equals(TaskWorkerHeartBeatRequestModel.class)) {
            type = CommandCase.TASK_WORKER_HEART_BEAT;
            taskWorkerHeartBeat = (TaskWorkerHeartBeatRequestModel) cmd;
        } else if (cls.equals(DeleteExternalEventRequestModel.class)) {
            type = CommandCase.DELETE_EXTERNAL_EVENT;
            deleteExternalEvent = (DeleteExternalEventRequestModel) cmd;
        } else if (cls.equals(AssignUserTaskRunRequestModel.class)) {
            type = CommandCase.ASSIGN_USER_TASK_RUN;
            assignUserTaskRun = (AssignUserTaskRunRequestModel) cmd;
        } else if (cls.equals(CompleteUserTaskRunRequestModel.class)) {
            type = CommandCase.COMPLETE_USER_TASK_RUN;
            completeUserTaskRun = (CompleteUserTaskRunRequestModel) cmd;
        } else if (cls.equals(TriggeredTaskRun.class)) {
            type = CommandCase.TRIGGERED_TASK_RUN;
            triggeredTaskRun = (TriggeredTaskRun) cmd;
        } else if (cls.equals(ReassignUserTask.class)) {
            type = CommandCase.REASSIGNED_USER_TASK;
            reassignUserTask = (ReassignUserTask) cmd;
        } else if (cls.equals(CancelUserTaskRunRequestModel.class)) {
            type = CommandCase.CANCEL_USER_TASK;
            cancelUserTaskRun = (CancelUserTaskRunRequestModel) cmd;
        } else {
            throw new IllegalArgumentException("Unrecognized SubCommand class: " + cls.getName());
        }
    }

    @Override
    public String getPartitionKey() {
        return getSubCommand().getPartitionKey();
    }

    @Override
    public LHStoreType getStore() {
        return LHStoreType.CORE;
    }

    @Override
    public String getTopic(LHConfig config) {
        return config.getCoreCmdTopicName();
    }

    public boolean hasResponse() {
        return getSubCommand().hasResponse();
    }

    public Message process(CoreProcessorDAO dao, LHConfig config) {
        return getSubCommand().process(dao, config);
    }
}

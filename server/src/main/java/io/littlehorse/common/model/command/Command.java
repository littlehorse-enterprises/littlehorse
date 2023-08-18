package io.littlehorse.common.model.command;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteExternalEventRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.command.subcommand.ExternalEventTimeout;
import io.littlehorse.common.model.command.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.command.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.command.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.model.command.subcommand.ReassignUserTask;
import io.littlehorse.common.model.command.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.command.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.command.subcommand.SleepNodeMatured;
import io.littlehorse.common.model.command.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.command.subcommand.TriggeredTaskRun;
import io.littlehorse.common.proto.CommandPb;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streamsimpl.coreprocessors.KafkaStreamsLHDAOImpl;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command extends LHSerializable<CommandPb> {

    public String commandId;
    public Date time;

    public CommandCase type;
    public ReportTaskRunModel reportTaskRun;
    public TaskClaimEvent taskClaimEvent;
    public PutExternalEventRequestModel putExternalEventRequest;
    public PutWfSpecRequestModel putWfSpecRequest;
    public PutTaskDefRequestModel putTaskDefRequest;
    public PutExternalEventDefRequestModel putExternalEventDefRequest;
    public RunWfRequestModel runWf;
    public StopWfRunRequestModel stopWfRun;
    public ResumeWfRunRequestModel resumeWfRun;
    public SleepNodeMatured sleepNodeMatured;
    public DeleteWfRunRequestModel deleteWfRun;
    public DeleteWfSpecRequestModel deleteWfSpec;
    public DeleteTaskDefRequestModel deleteTaskDef;
    public DeleteExternalEventDefRequestModel deleteExternalEventDef;
    public ExternalEventTimeout externalEventTimeout;
    public TaskWorkerHeartBeatRequestModel taskWorkerHeartBeat;
    public DeleteExternalEventRequestModel deleteExternalEvent;
    public PutUserTaskDefRequestModel putUserTaskDefRequest;
    public AssignUserTaskRunRequestModel assignUserTaskRun;
    public CompleteUserTaskRunRequestModel completeUserTaskRun;
    public TriggeredTaskRun triggeredTaskRun;
    public DeleteUserTaskDefRequestModel deleteUserTaskDef;
    private ReassignUserTask reassignUserTask;
    private CancelUserTaskRunRequestModel cancelUserTaskRun;

    public Class<CommandPb> getProtoBaseClass() {
        return CommandPb.class;
    }

    public Command() {}

    public Command(SubCommand<?> cmd) {
        this.time = new Date();
        this.setSubCommand(cmd);
    }

    public Command(SubCommand<?> cmd, Date time) {
        this.time = time;
        this.setSubCommand(cmd);
    }

    public CommandPb.Builder toProto() {
        CommandPb.Builder out = CommandPb.newBuilder();
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
            case PUT_WF_SPEC:
                out.setPutWfSpec(putWfSpecRequest.toProto());
                break;
            case PUT_TASK_DEF:
                out.setPutTaskDef(putTaskDefRequest.toProto());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                out.setPutExternalEventDef(putExternalEventDefRequest.toProto());
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
            case DELETE_EXTERNAL_EVENT_DEF:
                out.setDeleteExternalEventDef(deleteExternalEventDef.toProto());
                break;
            case DELETE_TASK_DEF:
                out.setDeleteTaskDef(deleteTaskDef.toProto());
                break;
            case DELETE_WF_SPEC:
                out.setDeleteWfSpec(deleteWfSpec.toProto());
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
            case PUT_USER_TASK_DEF:
                out.setPutUserTaskDef(putUserTaskDefRequest.toProto());
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
            case DELETE_USER_TASK_DEF:
                out.setDeleteUserTaskDef(deleteUserTaskDef.toProto());
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

    public void initFrom(Message proto) {
        CommandPb p = (CommandPb) proto;
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
            case PUT_WF_SPEC:
                putWfSpecRequest = PutWfSpecRequestModel.fromProto(p.getPutWfSpec());
                break;
            case PUT_TASK_DEF:
                putTaskDefRequest = PutTaskDefRequestModel.fromProto(p.getPutTaskDef());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                putExternalEventDefRequest = PutExternalEventDefRequestModel.fromProto(p.getPutExternalEventDef());
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
            case DELETE_EXTERNAL_EVENT_DEF:
                deleteExternalEventDef = DeleteExternalEventDefRequestModel.fromProto(p.getDeleteExternalEventDef());
                break;
            case DELETE_TASK_DEF:
                deleteTaskDef = DeleteTaskDefRequestModel.fromProto(p.getDeleteTaskDef());
                break;
            case DELETE_WF_SPEC:
                deleteWfSpec = DeleteWfSpecRequestModel.fromProto(p.getDeleteWfSpec());
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
            case PUT_USER_TASK_DEF:
                putUserTaskDefRequest =
                        LHSerializable.fromProto(p.getPutUserTaskDef(), PutUserTaskDefRequestModel.class);
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
            case DELETE_USER_TASK_DEF:
                deleteUserTaskDef =
                        LHSerializable.fromProto(p.getDeleteUserTaskDef(), DeleteUserTaskDefRequestModel.class);
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
            case PUT_WF_SPEC:
                return putWfSpecRequest;
            case PUT_TASK_DEF:
                return putTaskDefRequest;
            case PUT_EXTERNAL_EVENT_DEF:
                return putExternalEventDefRequest;
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
            case DELETE_EXTERNAL_EVENT_DEF:
                return deleteExternalEventDef;
            case DELETE_TASK_DEF:
                return deleteTaskDef;
            case DELETE_WF_SPEC:
                return deleteWfSpec;
            case EXTERNAL_EVENT_TIMEOUT:
                return externalEventTimeout;
            case TASK_WORKER_HEART_BEAT:
                return taskWorkerHeartBeat;
            case DELETE_EXTERNAL_EVENT:
                return deleteExternalEvent;
            case PUT_USER_TASK_DEF:
                return putUserTaskDefRequest;
            case ASSIGN_USER_TASK_RUN:
                return assignUserTaskRun;
            case COMPLETE_USER_TASK_RUN:
                return completeUserTaskRun;
            case TRIGGERED_TASK_RUN:
                return triggeredTaskRun;
            case DELETE_USER_TASK_DEF:
                return deleteUserTaskDef;
            case REASSIGNED_USER_TASK:
                return reassignUserTask;
            case CANCEL_USER_TASK:
                return cancelUserTaskRun;
            case COMMAND_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    public void setSubCommand(SubCommand<?> cmd) {
        Class<?> cls = cmd.getClass();
        if (cls.equals(PutTaskDefRequestModel.class)) {
            type = CommandCase.PUT_TASK_DEF;
            putTaskDefRequest = (PutTaskDefRequestModel) cmd;
        } else if (cls.equals(PutExternalEventDefRequestModel.class)) {
            type = CommandCase.PUT_EXTERNAL_EVENT_DEF;
            putExternalEventDefRequest = (PutExternalEventDefRequestModel) cmd;
        } else if (cls.equals(PutWfSpecRequestModel.class)) {
            type = CommandCase.PUT_WF_SPEC;
            putWfSpecRequest = (PutWfSpecRequestModel) cmd;
        } else if (cls.equals(RunWfRequestModel.class)) {
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
        } else if (cls.equals(DeleteExternalEventDefRequestModel.class)) {
            type = CommandCase.DELETE_EXTERNAL_EVENT_DEF;
            deleteExternalEventDef = (DeleteExternalEventDefRequestModel) cmd;
        } else if (cls.equals(DeleteTaskDefRequestModel.class)) {
            type = CommandCase.DELETE_TASK_DEF;
            deleteTaskDef = (DeleteTaskDefRequestModel) cmd;
        } else if (cls.equals(DeleteWfSpecRequestModel.class)) {
            type = CommandCase.DELETE_WF_SPEC;
            deleteWfSpec = (DeleteWfSpecRequestModel) cmd;
        } else if (cls.equals(ExternalEventTimeout.class)) {
            type = CommandCase.EXTERNAL_EVENT_TIMEOUT;
            externalEventTimeout = (ExternalEventTimeout) cmd;
        } else if (cls.equals(TaskWorkerHeartBeatRequestModel.class)) {
            type = CommandCase.TASK_WORKER_HEART_BEAT;
            taskWorkerHeartBeat = (TaskWorkerHeartBeatRequestModel) cmd;
        } else if (cls.equals(DeleteExternalEventRequestModel.class)) {
            type = CommandCase.DELETE_EXTERNAL_EVENT;
            deleteExternalEvent = (DeleteExternalEventRequestModel) cmd;
        } else if (cls.equals(PutUserTaskDefRequestModel.class)) {
            type = CommandCase.PUT_USER_TASK_DEF;
            putUserTaskDefRequest = (PutUserTaskDefRequestModel) cmd;
        } else if (cls.equals(AssignUserTaskRunRequestModel.class)) {
            type = CommandCase.ASSIGN_USER_TASK_RUN;
            assignUserTaskRun = (AssignUserTaskRunRequestModel) cmd;
        } else if (cls.equals(CompleteUserTaskRunRequestModel.class)) {
            type = CommandCase.COMPLETE_USER_TASK_RUN;
            completeUserTaskRun = (CompleteUserTaskRunRequestModel) cmd;
        } else if (cls.equals(TriggeredTaskRun.class)) {
            type = CommandCase.TRIGGERED_TASK_RUN;
            triggeredTaskRun = (TriggeredTaskRun) cmd;
        } else if (cls.equals(DeleteUserTaskDefRequestModel.class)) {
            type = CommandCase.DELETE_USER_TASK_DEF;
            deleteUserTaskDef = (DeleteUserTaskDefRequestModel) cmd;
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

    public String getPartitionKey() {
        return getSubCommand().getPartitionKey();
    }

    public boolean hasResponse() {
        return getSubCommand().hasResponse();
    }

    public AbstractResponse<?> process(KafkaStreamsLHDAOImpl dao, LHConfig config) {
        return getSubCommand().process(dao, config);
    }
}

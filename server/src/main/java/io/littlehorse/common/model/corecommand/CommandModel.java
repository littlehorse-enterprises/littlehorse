package io.littlehorse.common.model.corecommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.subcommand.*;
import io.littlehorse.common.model.corecommand.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.BulkUpdateJobModel;
import io.littlehorse.common.model.corecommand.subcommand.CancelUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeadlineReassignUserTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteCorrelatedEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeleteScheduledWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ExternalEventTimeoutModel;
import io.littlehorse.common.model.corecommand.subcommand.InternalDeleteWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.PutCorrelatedEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.PutExternalEventRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ReportTaskRunModel;
import io.littlehorse.common.model.corecommand.subcommand.RescueThreadRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ResumeWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SaveUserTaskRunProgressRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ScheduleWfRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.SleepNodeMaturedModel;
import io.littlehorse.common.model.corecommand.subcommand.StopWfRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskAttemptRetryReadyModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.corecommand.subcommand.TaskWorkerHeartBeatRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.TriggeredTaskRun;
import io.littlehorse.common.model.corecommand.subcommand.UpdateCorrelationMarkerModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskWorkerGroupRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.ScheduleWfRunCommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.common.proto.Command.CommandCase;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandModel extends AbstractCommand<Command> {

    public Date time;

    public CommandCase type;
    public ReportTaskRunModel reportTaskRun;
    public TaskClaimEvent taskClaimEvent;
    public PutExternalEventRequestModel putExternalEventRequest;
    public RunWfRequestModel runWf;
    public StopWfRunRequestModel stopWfRun;
    public ResumeWfRunRequestModel resumeWfRun;
    public SleepNodeMaturedModel sleepNodeMatured;
    public InternalDeleteWfRunRequestModel deleteWfRun;
    public ExternalEventTimeoutModel externalEventTimeout;
    public TaskWorkerHeartBeatRequestModel taskWorkerHeartBeat;
    public DeleteExternalEventRequestModel deleteExternalEvent;
    public AssignUserTaskRunRequestModel assignUserTaskRun;
    public CompleteUserTaskRunRequestModel completeUserTaskRun;
    public TriggeredTaskRun triggeredTaskRun;
    private DeadlineReassignUserTaskModel reassignUserTask;
    private CancelUserTaskRunRequestModel cancelUserTaskRun;
    private PutUserTaskRunCommentReqeustModel putUserTaskRunComment;
    private EditUserTaskRunCommentRequestModel editUserTaskRunComment;
    private DeleteUserTaskRunCommentRequestModel deleteUserTaskRunComment;
    private TaskAttemptRetryReadyModel taskAttemptRetryReady;
    private BulkUpdateJobModel bulkJob;
    private RescueThreadRunRequestModel rescueThreadRun;
    private DeleteTaskWorkerGroupRequestModel deleteTaskWorkerGroup;
    private SaveUserTaskRunProgressRequestModel saveUserTaskRunProgress;
    private ScheduleWfRunCommandModel scheduleWfRun;
    private ScheduleWfRequestModel scheduleWfRunRequest;
    private DeleteScheduledWfRunRequestModel deleteScheduledWfRun;
    private PutCorrelatedEventRequestModel putCorrelatedEvent;
    private UpdateCorrelationMarkerModel updateCorrellationMarker;
    private DeleteCorrelatedEventRequestModel deleteCorrelatedEvent;
    private PutCheckpointRequestModel putCheckpoint;

    public Class<Command> getProtoBaseClass() {
        return Command.class;
    }

    public CommandModel() {}

    public CommandModel(CoreSubCommand<?> cmd) {
        this.time = new Date();
        this.setSubCommand(cmd);
    }

    public CommandModel(CoreSubCommand<?> cmd, Date time) {
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
            case PUT_USER_TASK_RUN_COMMENT:
                out.setPutUserTaskRunComment(putUserTaskRunComment.toProto());
                break;
            case EDIT_USER_TASK_RUN_COMMENT:
                out.setEditUserTaskRunComment(editUserTaskRunComment.toProto());
                break;
            case DELETE_USER_TASK_RUN_COMMENT:
                out.setDeleteUserTaskRunComment(deleteUserTaskRunComment.toProto());
                break;
            case BULK_JOB:
                out.setBulkJob(bulkJob.toProto());
                break;
            case TASK_ATTEMPT_RETRY_READY:
                out.setTaskAttemptRetryReady(taskAttemptRetryReady.toProto());
                break;
            case RESCUE_THREAD_RUN:
                out.setRescueThreadRun(rescueThreadRun.toProto());
                break;
            case DELETE_TASK_WORKER_GROUP:
                out.setDeleteTaskWorkerGroup(deleteTaskWorkerGroup.toProto());
                break;
            case SAVE_USER_TASK_RUN_PROGRESS:
                out.setSaveUserTaskRunProgress(saveUserTaskRunProgress.toProto());
                break;
            case SCHEDULE_WF_RUN:
                out.setScheduleWfRun(scheduleWfRun.toProto());
                break;
            case SCHEDULE_WF_RUN_REQUEST:
                out.setScheduleWfRunRequest(scheduleWfRunRequest.toProto());
                break;
            case DELETE_SCHEDULED_WF_RUN:
                out.setDeleteScheduledWfRun(deleteScheduledWfRun.toProto());
                break;
            case PUT_CORRELATED_EVENT:
                out.setPutCorrelatedEvent(putCorrelatedEvent.toProto());
                break;
            case UPDATE_CORRELATION_MARKER:
                out.setUpdateCorrelationMarker(updateCorrellationMarker.toProto());
                break;
            case DELETE_CORRELATED_EVENT:
                out.setDeleteCorrelatedEvent(deleteCorrelatedEvent.toProto());
                break;
            case PUT_CHECKPOINT:
                out.setPutCheckpoint(putCheckpoint.toProto());
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Command p = (Command) proto;
        time = LHUtil.fromProtoTs(p.getTime());

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }

        type = p.getCommandCase();
        switch (type) {
            case REPORT_TASK_RUN:
                reportTaskRun = ReportTaskRunModel.fromProto(p.getReportTaskRun(), context);
                break;
            case TASK_CLAIM_EVENT:
                taskClaimEvent = TaskClaimEvent.fromProto(p.getTaskClaimEvent(), context);
                break;
            case PUT_EXTERNAL_EVENT:
                putExternalEventRequest = PutExternalEventRequestModel.fromProto(p.getPutExternalEvent(), context);
                break;
            case RUN_WF:
                runWf = RunWfRequestModel.fromProto(p.getRunWf(), context);
                break;
            case STOP_WF_RUN:
                stopWfRun = StopWfRunRequestModel.fromProto(p.getStopWfRun(), context);
                break;
            case RESUME_WF_RUN:
                resumeWfRun = ResumeWfRunRequestModel.fromProto(p.getResumeWfRun(), context);
                break;
            case SLEEP_NODE_MATURED:
                sleepNodeMatured = SleepNodeMaturedModel.fromProto(p.getSleepNodeMatured(), context);
                break;
            case DELETE_WF_RUN:
                deleteWfRun = InternalDeleteWfRunRequestModel.fromProto(p.getDeleteWfRun(), context);
                break;
            case EXTERNAL_EVENT_TIMEOUT:
                externalEventTimeout = ExternalEventTimeoutModel.fromProto(p.getExternalEventTimeout(), context);
                break;
            case TASK_WORKER_HEART_BEAT:
                taskWorkerHeartBeat = TaskWorkerHeartBeatRequestModel.fromProto(p.getTaskWorkerHeartBeat(), context);
                break;
            case DELETE_EXTERNAL_EVENT:
                deleteExternalEvent = DeleteExternalEventRequestModel.fromProto(p.getDeleteExternalEvent(), context);
                break;
            case ASSIGN_USER_TASK_RUN:
                assignUserTaskRun = LHSerializable.fromProto(
                        p.getAssignUserTaskRun(), AssignUserTaskRunRequestModel.class, context);
                break;
            case COMPLETE_USER_TASK_RUN:
                completeUserTaskRun = LHSerializable.fromProto(
                        p.getCompleteUserTaskRun(), CompleteUserTaskRunRequestModel.class, context);
                break;
            case TRIGGERED_TASK_RUN:
                triggeredTaskRun = LHSerializable.fromProto(p.getTriggeredTaskRun(), TriggeredTaskRun.class, context);
                break;
            case REASSIGNED_USER_TASK:
                reassignUserTask = LHSerializable.fromProto(
                        p.getReassignedUserTask(), DeadlineReassignUserTaskModel.class, context);
                break;
            case CANCEL_USER_TASK:
                cancelUserTaskRun =
                        LHSerializable.fromProto(p.getCancelUserTask(), CancelUserTaskRunRequestModel.class, context);
                break;
            case PUT_USER_TASK_RUN_COMMENT:
                putUserTaskRunComment = LHSerializable.fromProto(
                        p.getPutUserTaskRunComment(), PutUserTaskRunCommentReqeustModel.class, context);
                break;
            case EDIT_USER_TASK_RUN_COMMENT:
                editUserTaskRunComment = LHSerializable.fromProto(
                        p.getEditUserTaskRunComment(), EditUserTaskRunCommentRequestModel.class, context);
                break;
            case DELETE_USER_TASK_RUN_COMMENT:
                deleteUserTaskRunComment = LHSerializable.fromProto(
                        p.getDeleteUserTaskRunComment(), DeleteUserTaskRunCommentRequestModel.class, context);
                break;
            case BULK_JOB:
                bulkJob = LHSerializable.fromProto(p.getBulkJob(), BulkUpdateJobModel.class, context);
                break;
            case TASK_ATTEMPT_RETRY_READY:
                taskAttemptRetryReady = LHSerializable.fromProto(
                        p.getTaskAttemptRetryReady(), TaskAttemptRetryReadyModel.class, context);
                break;
            case RESCUE_THREAD_RUN:
                rescueThreadRun =
                        LHSerializable.fromProto(p.getRescueThreadRun(), RescueThreadRunRequestModel.class, context);
                break;
            case DELETE_TASK_WORKER_GROUP:
                deleteTaskWorkerGroup = LHSerializable.fromProto(
                        p.getDeleteTaskWorkerGroup(), DeleteTaskWorkerGroupRequestModel.class, context);
                break;
            case SAVE_USER_TASK_RUN_PROGRESS:
                saveUserTaskRunProgress = LHSerializable.fromProto(
                        p.getSaveUserTaskRunProgress(), SaveUserTaskRunProgressRequestModel.class, context);
                break;
            case SCHEDULE_WF_RUN:
                scheduleWfRun =
                        LHSerializable.fromProto(p.getScheduleWfRun(), ScheduleWfRunCommandModel.class, context);
                break;
            case SCHEDULE_WF_RUN_REQUEST:
                scheduleWfRunRequest =
                        LHSerializable.fromProto(p.getScheduleWfRunRequest(), ScheduleWfRequestModel.class, context);
                break;
            case DELETE_SCHEDULED_WF_RUN:
                deleteScheduledWfRun = LHSerializable.fromProto(
                        p.getDeleteScheduledWfRun(), DeleteScheduledWfRunRequestModel.class, context);
                break;
            case PUT_CORRELATED_EVENT:
                putCorrelatedEvent = LHSerializable.fromProto(
                        p.getPutCorrelatedEvent(), PutCorrelatedEventRequestModel.class, context);
                break;
            case UPDATE_CORRELATION_MARKER:
                updateCorrellationMarker = LHSerializable.fromProto(
                        p.getUpdateCorrelationMarker(), UpdateCorrelationMarkerModel.class, context);
                break;
            case DELETE_CORRELATED_EVENT:
                deleteCorrelatedEvent = LHSerializable.fromProto(
                        p.getDeleteCorrelatedEvent(), DeleteCorrelatedEventRequestModel.class, context);
                break;
            case PUT_CHECKPOINT:
                putCheckpoint =
                        LHSerializable.fromProto(p.getPutCheckpoint(), PutCheckpointRequestModel.class, context);
                break;
            case COMMAND_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    public CoreSubCommand<?> getSubCommand() {
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
            case PUT_USER_TASK_RUN_COMMENT:
                return putUserTaskRunComment;
            case EDIT_USER_TASK_RUN_COMMENT:
                return editUserTaskRunComment;
            case DELETE_USER_TASK_RUN_COMMENT:
                return deleteUserTaskRunComment;
            case BULK_JOB:
                return bulkJob;
            case TASK_ATTEMPT_RETRY_READY:
                return taskAttemptRetryReady;
            case RESCUE_THREAD_RUN:
                return rescueThreadRun;
            case DELETE_TASK_WORKER_GROUP:
                return deleteTaskWorkerGroup;
            case SAVE_USER_TASK_RUN_PROGRESS:
                return saveUserTaskRunProgress;
            case SCHEDULE_WF_RUN:
                return scheduleWfRun;
            case SCHEDULE_WF_RUN_REQUEST:
                return scheduleWfRunRequest;
            case DELETE_SCHEDULED_WF_RUN:
                return deleteScheduledWfRun;
            case PUT_CORRELATED_EVENT:
                return putCorrelatedEvent;
            case UPDATE_CORRELATION_MARKER:
                return updateCorrellationMarker;
            case DELETE_CORRELATED_EVENT:
                return deleteCorrelatedEvent;
            case PUT_CHECKPOINT:
                return putCheckpoint;
            case COMMAND_NOT_SET:
        }
        throw new IllegalStateException("Not possible to have missing subcommand.");
    }

    public void setSubCommand(CoreSubCommand<?> cmd) {
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
        } else if (cls.equals(SleepNodeMaturedModel.class)) {
            type = CommandCase.SLEEP_NODE_MATURED;
            sleepNodeMatured = (SleepNodeMaturedModel) cmd;
        } else if (cls.equals(InternalDeleteWfRunRequestModel.class)) {
            type = CommandCase.DELETE_WF_RUN;
            deleteWfRun = (InternalDeleteWfRunRequestModel) cmd;
        } else if (cls.equals(ExternalEventTimeoutModel.class)) {
            type = CommandCase.EXTERNAL_EVENT_TIMEOUT;
            externalEventTimeout = (ExternalEventTimeoutModel) cmd;
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
        } else if (cls.equals(DeadlineReassignUserTaskModel.class)) {
            type = CommandCase.REASSIGNED_USER_TASK;
            reassignUserTask = (DeadlineReassignUserTaskModel) cmd;
        } else if (cls.equals(CancelUserTaskRunRequestModel.class)) {
            type = CommandCase.CANCEL_USER_TASK;
            cancelUserTaskRun = (CancelUserTaskRunRequestModel) cmd;
        } else if (cls.equals(PutUserTaskRunCommentReqeustModel.class)) {
            type = CommandCase.PUT_USER_TASK_RUN_COMMENT;
            putUserTaskRunComment = (PutUserTaskRunCommentReqeustModel) cmd;
        } else if (cls.equals(EditUserTaskRunCommentRequestModel.class)) {
            type = CommandCase.EDIT_USER_TASK_RUN_COMMENT;
            editUserTaskRunComment = (EditUserTaskRunCommentRequestModel) cmd;
        } else if (cls.equals(DeleteUserTaskRunCommentRequestModel.class)) {
            type = CommandCase.DELETE_USER_TASK_RUN_COMMENT;
            deleteUserTaskRunComment = (DeleteUserTaskRunCommentRequestModel) cmd;
        } else if (cls.equals(BulkUpdateJobModel.class)) {
            type = CommandCase.BULK_JOB;
            bulkJob = (BulkUpdateJobModel) cmd;
        } else if (cls.equals(TaskAttemptRetryReadyModel.class)) {
            type = CommandCase.TASK_ATTEMPT_RETRY_READY;
            taskAttemptRetryReady = (TaskAttemptRetryReadyModel) cmd;
        } else if (cls.equals(RescueThreadRunRequestModel.class)) {
            type = CommandCase.RESCUE_THREAD_RUN;
            rescueThreadRun = (RescueThreadRunRequestModel) cmd;
        } else if (cls.equals(DeleteTaskWorkerGroupRequestModel.class)) {
            type = CommandCase.DELETE_TASK_WORKER_GROUP;
            deleteTaskWorkerGroup = (DeleteTaskWorkerGroupRequestModel) cmd;
        } else if (cls.equals(SaveUserTaskRunProgressRequestModel.class)) {
            type = CommandCase.SAVE_USER_TASK_RUN_PROGRESS;
            saveUserTaskRunProgress = (SaveUserTaskRunProgressRequestModel) cmd;
        } else if (cls.equals(ScheduleWfRunCommandModel.class)) {
            type = CommandCase.SCHEDULE_WF_RUN;
            scheduleWfRun = (ScheduleWfRunCommandModel) cmd;
        } else if (cls.equals(ScheduleWfRequestModel.class)) {
            type = CommandCase.SCHEDULE_WF_RUN_REQUEST;
            scheduleWfRunRequest = (ScheduleWfRequestModel) cmd;
        } else if (cls.equals(DeleteScheduledWfRunRequestModel.class)) {
            type = CommandCase.DELETE_SCHEDULED_WF_RUN;
            deleteScheduledWfRun = (DeleteScheduledWfRunRequestModel) cmd;
        } else if (cls.equals(PutCorrelatedEventRequestModel.class)) {
            type = CommandCase.PUT_CORRELATED_EVENT;
            putCorrelatedEvent = (PutCorrelatedEventRequestModel) cmd;
        } else if (cls.equals(UpdateCorrelationMarkerModel.class)) {
            type = CommandCase.UPDATE_CORRELATION_MARKER;
            updateCorrellationMarker = (UpdateCorrelationMarkerModel) cmd;
        } else if (cls.equals(DeleteCorrelatedEventRequestModel.class)) {
            type = CommandCase.DELETE_CORRELATED_EVENT;
            deleteCorrelatedEvent = (DeleteCorrelatedEventRequestModel) cmd;
        } else if (cls.equals(PutCheckpointRequestModel.class)) {
            type = CommandCase.PUT_CHECKPOINT;
            putCheckpoint = (PutCheckpointRequestModel) cmd;
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
    public String getTopic(LHServerConfig config) {
        return config.getCoreCmdTopicName();
    }

    @Override
    public Optional<String> getCommandId() {
        return super.getCommandId();
    }

    public boolean hasResponse() {
        return commandId != null;
    }

    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        return getSubCommand().process(executionContext, config);
    }
}

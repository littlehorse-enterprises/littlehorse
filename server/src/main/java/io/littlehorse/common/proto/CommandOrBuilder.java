// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.common.proto;

public interface CommandOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Command)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();

  /**
   * <code>optional string command_id = 2;</code>
   * @return Whether the commandId field is set.
   */
  boolean hasCommandId();
  /**
   * <code>optional string command_id = 2;</code>
   * @return The commandId.
   */
  java.lang.String getCommandId();
  /**
   * <code>optional string command_id = 2;</code>
   * @return The bytes for commandId.
   */
  com.google.protobuf.ByteString
      getCommandIdBytes();

  /**
   * <code>.littlehorse.ReportTaskRun report_task_run = 4;</code>
   * @return Whether the reportTaskRun field is set.
   */
  boolean hasReportTaskRun();
  /**
   * <code>.littlehorse.ReportTaskRun report_task_run = 4;</code>
   * @return The reportTaskRun.
   */
  io.littlehorse.sdk.common.proto.ReportTaskRun getReportTaskRun();
  /**
   * <code>.littlehorse.ReportTaskRun report_task_run = 4;</code>
   */
  io.littlehorse.sdk.common.proto.ReportTaskRunOrBuilder getReportTaskRunOrBuilder();

  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 5;</code>
   * @return Whether the taskClaimEvent field is set.
   */
  boolean hasTaskClaimEvent();
  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 5;</code>
   * @return The taskClaimEvent.
   */
  io.littlehorse.common.proto.TaskClaimEventPb getTaskClaimEvent();
  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 5;</code>
   */
  io.littlehorse.common.proto.TaskClaimEventPbOrBuilder getTaskClaimEventOrBuilder();

  /**
   * <code>.littlehorse.RunWfRequest run_wf = 6;</code>
   * @return Whether the runWf field is set.
   */
  boolean hasRunWf();
  /**
   * <code>.littlehorse.RunWfRequest run_wf = 6;</code>
   * @return The runWf.
   */
  io.littlehorse.sdk.common.proto.RunWfRequest getRunWf();
  /**
   * <code>.littlehorse.RunWfRequest run_wf = 6;</code>
   */
  io.littlehorse.sdk.common.proto.RunWfRequestOrBuilder getRunWfOrBuilder();

  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 7;</code>
   * @return Whether the putExternalEvent field is set.
   */
  boolean hasPutExternalEvent();
  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 7;</code>
   * @return The putExternalEvent.
   */
  io.littlehorse.sdk.common.proto.PutExternalEventRequest getPutExternalEvent();
  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 7;</code>
   */
  io.littlehorse.sdk.common.proto.PutExternalEventRequestOrBuilder getPutExternalEventOrBuilder();

  /**
   * <code>.littlehorse.StopWfRunRequest stop_wf_run = 8;</code>
   * @return Whether the stopWfRun field is set.
   */
  boolean hasStopWfRun();
  /**
   * <code>.littlehorse.StopWfRunRequest stop_wf_run = 8;</code>
   * @return The stopWfRun.
   */
  io.littlehorse.sdk.common.proto.StopWfRunRequest getStopWfRun();
  /**
   * <code>.littlehorse.StopWfRunRequest stop_wf_run = 8;</code>
   */
  io.littlehorse.sdk.common.proto.StopWfRunRequestOrBuilder getStopWfRunOrBuilder();

  /**
   * <code>.littlehorse.ResumeWfRunRequest resume_wf_run = 9;</code>
   * @return Whether the resumeWfRun field is set.
   */
  boolean hasResumeWfRun();
  /**
   * <code>.littlehorse.ResumeWfRunRequest resume_wf_run = 9;</code>
   * @return The resumeWfRun.
   */
  io.littlehorse.sdk.common.proto.ResumeWfRunRequest getResumeWfRun();
  /**
   * <code>.littlehorse.ResumeWfRunRequest resume_wf_run = 9;</code>
   */
  io.littlehorse.sdk.common.proto.ResumeWfRunRequestOrBuilder getResumeWfRunOrBuilder();

  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 10;</code>
   * @return Whether the sleepNodeMatured field is set.
   */
  boolean hasSleepNodeMatured();
  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 10;</code>
   * @return The sleepNodeMatured.
   */
  io.littlehorse.common.proto.SleepNodeMaturedPb getSleepNodeMatured();
  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 10;</code>
   */
  io.littlehorse.common.proto.SleepNodeMaturedPbOrBuilder getSleepNodeMaturedOrBuilder();

  /**
   * <code>.littlehorse.DeleteWfRunRequest delete_wf_run = 11;</code>
   * @return Whether the deleteWfRun field is set.
   */
  boolean hasDeleteWfRun();
  /**
   * <code>.littlehorse.DeleteWfRunRequest delete_wf_run = 11;</code>
   * @return The deleteWfRun.
   */
  io.littlehorse.sdk.common.proto.DeleteWfRunRequest getDeleteWfRun();
  /**
   * <code>.littlehorse.DeleteWfRunRequest delete_wf_run = 11;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteWfRunRequestOrBuilder getDeleteWfRunOrBuilder();

  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 12;</code>
   * @return Whether the externalEventTimeout field is set.
   */
  boolean hasExternalEventTimeout();
  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 12;</code>
   * @return The externalEventTimeout.
   */
  io.littlehorse.common.proto.ExternalEventNodeTimeoutPb getExternalEventTimeout();
  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 12;</code>
   */
  io.littlehorse.common.proto.ExternalEventNodeTimeoutPbOrBuilder getExternalEventTimeoutOrBuilder();

  /**
   * <code>.littlehorse.TaskWorkerHeartBeatRequest task_worker_heart_beat = 13;</code>
   * @return Whether the taskWorkerHeartBeat field is set.
   */
  boolean hasTaskWorkerHeartBeat();
  /**
   * <code>.littlehorse.TaskWorkerHeartBeatRequest task_worker_heart_beat = 13;</code>
   * @return The taskWorkerHeartBeat.
   */
  io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest getTaskWorkerHeartBeat();
  /**
   * <code>.littlehorse.TaskWorkerHeartBeatRequest task_worker_heart_beat = 13;</code>
   */
  io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequestOrBuilder getTaskWorkerHeartBeatOrBuilder();

  /**
   * <code>.littlehorse.DeleteExternalEventRequest delete_external_event = 14;</code>
   * @return Whether the deleteExternalEvent field is set.
   */
  boolean hasDeleteExternalEvent();
  /**
   * <code>.littlehorse.DeleteExternalEventRequest delete_external_event = 14;</code>
   * @return The deleteExternalEvent.
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventRequest getDeleteExternalEvent();
  /**
   * <code>.littlehorse.DeleteExternalEventRequest delete_external_event = 14;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventRequestOrBuilder getDeleteExternalEventOrBuilder();

  /**
   * <code>.littlehorse.AssignUserTaskRunRequest assign_user_task_run = 15;</code>
   * @return Whether the assignUserTaskRun field is set.
   */
  boolean hasAssignUserTaskRun();
  /**
   * <code>.littlehorse.AssignUserTaskRunRequest assign_user_task_run = 15;</code>
   * @return The assignUserTaskRun.
   */
  io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest getAssignUserTaskRun();
  /**
   * <code>.littlehorse.AssignUserTaskRunRequest assign_user_task_run = 15;</code>
   */
  io.littlehorse.sdk.common.proto.AssignUserTaskRunRequestOrBuilder getAssignUserTaskRunOrBuilder();

  /**
   * <code>.littlehorse.CompleteUserTaskRunRequest complete_user_task_run = 16;</code>
   * @return Whether the completeUserTaskRun field is set.
   */
  boolean hasCompleteUserTaskRun();
  /**
   * <code>.littlehorse.CompleteUserTaskRunRequest complete_user_task_run = 16;</code>
   * @return The completeUserTaskRun.
   */
  io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest getCompleteUserTaskRun();
  /**
   * <code>.littlehorse.CompleteUserTaskRunRequest complete_user_task_run = 16;</code>
   */
  io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequestOrBuilder getCompleteUserTaskRunOrBuilder();

  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 17;</code>
   * @return Whether the triggeredTaskRun field is set.
   */
  boolean hasTriggeredTaskRun();
  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 17;</code>
   * @return The triggeredTaskRun.
   */
  io.littlehorse.common.proto.TriggeredTaskRunPb getTriggeredTaskRun();
  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 17;</code>
   */
  io.littlehorse.common.proto.TriggeredTaskRunPbOrBuilder getTriggeredTaskRunOrBuilder();

  /**
   * <code>.littlehorse.DeadlineReassignUserTask reassigned_user_task = 18;</code>
   * @return Whether the reassignedUserTask field is set.
   */
  boolean hasReassignedUserTask();
  /**
   * <code>.littlehorse.DeadlineReassignUserTask reassigned_user_task = 18;</code>
   * @return The reassignedUserTask.
   */
  io.littlehorse.common.proto.DeadlineReassignUserTask getReassignedUserTask();
  /**
   * <code>.littlehorse.DeadlineReassignUserTask reassigned_user_task = 18;</code>
   */
  io.littlehorse.common.proto.DeadlineReassignUserTaskOrBuilder getReassignedUserTaskOrBuilder();

  /**
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   * @return Whether the cancelUserTask field is set.
   */
  boolean hasCancelUserTask();
  /**
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   * @return The cancelUserTask.
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest getCancelUserTask();
  /**
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequestOrBuilder getCancelUserTaskOrBuilder();

  /**
   * <code>.littlehorse.BulkUpdateJob bulk_job = 20;</code>
   * @return Whether the bulkJob field is set.
   */
  boolean hasBulkJob();
  /**
   * <code>.littlehorse.BulkUpdateJob bulk_job = 20;</code>
   * @return The bulkJob.
   */
  io.littlehorse.common.proto.BulkUpdateJob getBulkJob();
  /**
   * <code>.littlehorse.BulkUpdateJob bulk_job = 20;</code>
   */
  io.littlehorse.common.proto.BulkUpdateJobOrBuilder getBulkJobOrBuilder();

  /**
   * <code>.littlehorse.TaskAttemptRetryReady task_attempt_retry_ready = 21;</code>
   * @return Whether the taskAttemptRetryReady field is set.
   */
  boolean hasTaskAttemptRetryReady();
  /**
   * <code>.littlehorse.TaskAttemptRetryReady task_attempt_retry_ready = 21;</code>
   * @return The taskAttemptRetryReady.
   */
  io.littlehorse.common.proto.TaskAttemptRetryReady getTaskAttemptRetryReady();
  /**
   * <code>.littlehorse.TaskAttemptRetryReady task_attempt_retry_ready = 21;</code>
   */
  io.littlehorse.common.proto.TaskAttemptRetryReadyOrBuilder getTaskAttemptRetryReadyOrBuilder();

  /**
   * <code>.littlehorse.RescueThreadRunRequest rescue_thread_run = 22;</code>
   * @return Whether the rescueThreadRun field is set.
   */
  boolean hasRescueThreadRun();
  /**
   * <code>.littlehorse.RescueThreadRunRequest rescue_thread_run = 22;</code>
   * @return The rescueThreadRun.
   */
  io.littlehorse.sdk.common.proto.RescueThreadRunRequest getRescueThreadRun();
  /**
   * <code>.littlehorse.RescueThreadRunRequest rescue_thread_run = 22;</code>
   */
  io.littlehorse.sdk.common.proto.RescueThreadRunRequestOrBuilder getRescueThreadRunOrBuilder();

  /**
   * <code>.littlehorse.DeleteTaskWorkerGroupRequest delete_task_worker_group = 23;</code>
   * @return Whether the deleteTaskWorkerGroup field is set.
   */
  boolean hasDeleteTaskWorkerGroup();
  /**
   * <code>.littlehorse.DeleteTaskWorkerGroupRequest delete_task_worker_group = 23;</code>
   * @return The deleteTaskWorkerGroup.
   */
  io.littlehorse.common.proto.DeleteTaskWorkerGroupRequest getDeleteTaskWorkerGroup();
  /**
   * <code>.littlehorse.DeleteTaskWorkerGroupRequest delete_task_worker_group = 23;</code>
   */
  io.littlehorse.common.proto.DeleteTaskWorkerGroupRequestOrBuilder getDeleteTaskWorkerGroupOrBuilder();

  /**
   * <code>.littlehorse.ScheduleWfRun schedule_wf_run = 24;</code>
   * @return Whether the scheduleWfRun field is set.
   */
  boolean hasScheduleWfRun();
  /**
   * <code>.littlehorse.ScheduleWfRun schedule_wf_run = 24;</code>
   * @return The scheduleWfRun.
   */
  io.littlehorse.common.proto.ScheduleWfRun getScheduleWfRun();
  /**
   * <code>.littlehorse.ScheduleWfRun schedule_wf_run = 24;</code>
   */
  io.littlehorse.common.proto.ScheduleWfRunOrBuilder getScheduleWfRunOrBuilder();

  /**
   * <code>.littlehorse.ScheduleWfRequest schedule_wf_run_request = 25;</code>
   * @return Whether the scheduleWfRunRequest field is set.
   */
  boolean hasScheduleWfRunRequest();
  /**
   * <code>.littlehorse.ScheduleWfRequest schedule_wf_run_request = 25;</code>
   * @return The scheduleWfRunRequest.
   */
  io.littlehorse.sdk.common.proto.ScheduleWfRequest getScheduleWfRunRequest();
  /**
   * <code>.littlehorse.ScheduleWfRequest schedule_wf_run_request = 25;</code>
   */
  io.littlehorse.sdk.common.proto.ScheduleWfRequestOrBuilder getScheduleWfRunRequestOrBuilder();

  /**
   * <code>.littlehorse.DeleteScheduledWfRunRequest delete_scheduled_wf_run = 26;</code>
   * @return Whether the deleteScheduledWfRun field is set.
   */
  boolean hasDeleteScheduledWfRun();
  /**
   * <code>.littlehorse.DeleteScheduledWfRunRequest delete_scheduled_wf_run = 26;</code>
   * @return The deleteScheduledWfRun.
   */
  io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest getDeleteScheduledWfRun();
  /**
   * <code>.littlehorse.DeleteScheduledWfRunRequest delete_scheduled_wf_run = 26;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequestOrBuilder getDeleteScheduledWfRunOrBuilder();

  /**
   * <code>.littlehorse.SaveUserTaskRunProgressRequest save_user_task_run_progress = 27;</code>
   * @return Whether the saveUserTaskRunProgress field is set.
   */
  boolean hasSaveUserTaskRunProgress();
  /**
   * <code>.littlehorse.SaveUserTaskRunProgressRequest save_user_task_run_progress = 27;</code>
   * @return The saveUserTaskRunProgress.
   */
  io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest getSaveUserTaskRunProgress();
  /**
   * <code>.littlehorse.SaveUserTaskRunProgressRequest save_user_task_run_progress = 27;</code>
   */
  io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequestOrBuilder getSaveUserTaskRunProgressOrBuilder();

  io.littlehorse.common.proto.Command.CommandCase getCommandCase();
}

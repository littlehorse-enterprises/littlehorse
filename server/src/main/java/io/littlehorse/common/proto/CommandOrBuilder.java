// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

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
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 18;</code>
   * @return Whether the reassignedUserTask field is set.
   */
  boolean hasReassignedUserTask();
  /**
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 18;</code>
   * @return The reassignedUserTask.
   */
  io.littlehorse.common.proto.ReassignedUserTaskPb getReassignedUserTask();
  /**
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 18;</code>
   */
  io.littlehorse.common.proto.ReassignedUserTaskPbOrBuilder getReassignedUserTaskOrBuilder();

  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   * @return Whether the cancelUserTask field is set.
   */
  boolean hasCancelUserTask();
  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   * @return The cancelUserTask.
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest getCancelUserTask();
  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunRequest cancel_user_task = 19;</code>
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequestOrBuilder getCancelUserTaskOrBuilder();

  io.littlehorse.common.proto.Command.CommandCase getCommandCase();
}

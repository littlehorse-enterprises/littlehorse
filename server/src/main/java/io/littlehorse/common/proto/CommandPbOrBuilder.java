// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

public interface CommandPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.CommandPb)
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
   * <code>.littlehorse.ReportTaskRunPb report_task_run = 3;</code>
   * @return Whether the reportTaskRun field is set.
   */
  boolean hasReportTaskRun();
  /**
   * <code>.littlehorse.ReportTaskRunPb report_task_run = 3;</code>
   * @return The reportTaskRun.
   */
  io.littlehorse.sdk.common.proto.ReportTaskRunPb getReportTaskRun();
  /**
   * <code>.littlehorse.ReportTaskRunPb report_task_run = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ReportTaskRunPbOrBuilder getReportTaskRunOrBuilder();

  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 4;</code>
   * @return Whether the taskClaimEvent field is set.
   */
  boolean hasTaskClaimEvent();
  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 4;</code>
   * @return The taskClaimEvent.
   */
  io.littlehorse.common.proto.TaskClaimEventPb getTaskClaimEvent();
  /**
   * <code>.littlehorse.TaskClaimEventPb task_claim_event = 4;</code>
   */
  io.littlehorse.common.proto.TaskClaimEventPbOrBuilder getTaskClaimEventOrBuilder();

  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 6;</code>
   * @return Whether the putWfSpec field is set.
   */
  boolean hasPutWfSpec();
  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 6;</code>
   * @return The putWfSpec.
   */
  io.littlehorse.sdk.common.proto.PutWfSpecRequest getPutWfSpec();
  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 6;</code>
   */
  io.littlehorse.sdk.common.proto.PutWfSpecRequestOrBuilder getPutWfSpecOrBuilder();

  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 7;</code>
   * @return Whether the putTaskDef field is set.
   */
  boolean hasPutTaskDef();
  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 7;</code>
   * @return The putTaskDef.
   */
  io.littlehorse.sdk.common.proto.PutTaskDefRequest getPutTaskDef();
  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 7;</code>
   */
  io.littlehorse.sdk.common.proto.PutTaskDefRequestOrBuilder getPutTaskDefOrBuilder();

  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 8;</code>
   * @return Whether the putExternalEventDef field is set.
   */
  boolean hasPutExternalEventDef();
  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 8;</code>
   * @return The putExternalEventDef.
   */
  io.littlehorse.sdk.common.proto.PutExternalEventDefRequest getPutExternalEventDef();
  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 8;</code>
   */
  io.littlehorse.sdk.common.proto.PutExternalEventDefRequestOrBuilder getPutExternalEventDefOrBuilder();

  /**
   * <code>.littlehorse.RunWfPb run_wf = 9;</code>
   * @return Whether the runWf field is set.
   */
  boolean hasRunWf();
  /**
   * <code>.littlehorse.RunWfPb run_wf = 9;</code>
   * @return The runWf.
   */
  io.littlehorse.sdk.common.proto.RunWfPb getRunWf();
  /**
   * <code>.littlehorse.RunWfPb run_wf = 9;</code>
   */
  io.littlehorse.sdk.common.proto.RunWfPbOrBuilder getRunWfOrBuilder();

  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 10;</code>
   * @return Whether the putExternalEvent field is set.
   */
  boolean hasPutExternalEvent();
  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 10;</code>
   * @return The putExternalEvent.
   */
  io.littlehorse.sdk.common.proto.PutExternalEventRequest getPutExternalEvent();
  /**
   * <code>.littlehorse.PutExternalEventRequest put_external_event = 10;</code>
   */
  io.littlehorse.sdk.common.proto.PutExternalEventRequestOrBuilder getPutExternalEventOrBuilder();

  /**
   * <code>.littlehorse.StopWfRunPb stop_wf_run = 11;</code>
   * @return Whether the stopWfRun field is set.
   */
  boolean hasStopWfRun();
  /**
   * <code>.littlehorse.StopWfRunPb stop_wf_run = 11;</code>
   * @return The stopWfRun.
   */
  io.littlehorse.sdk.common.proto.StopWfRunPb getStopWfRun();
  /**
   * <code>.littlehorse.StopWfRunPb stop_wf_run = 11;</code>
   */
  io.littlehorse.sdk.common.proto.StopWfRunPbOrBuilder getStopWfRunOrBuilder();

  /**
   * <code>.littlehorse.ResumeWfRunPb resume_wf_run = 12;</code>
   * @return Whether the resumeWfRun field is set.
   */
  boolean hasResumeWfRun();
  /**
   * <code>.littlehorse.ResumeWfRunPb resume_wf_run = 12;</code>
   * @return The resumeWfRun.
   */
  io.littlehorse.sdk.common.proto.ResumeWfRunPb getResumeWfRun();
  /**
   * <code>.littlehorse.ResumeWfRunPb resume_wf_run = 12;</code>
   */
  io.littlehorse.sdk.common.proto.ResumeWfRunPbOrBuilder getResumeWfRunOrBuilder();

  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 13;</code>
   * @return Whether the sleepNodeMatured field is set.
   */
  boolean hasSleepNodeMatured();
  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 13;</code>
   * @return The sleepNodeMatured.
   */
  io.littlehorse.common.proto.SleepNodeMaturedPb getSleepNodeMatured();
  /**
   * <code>.littlehorse.SleepNodeMaturedPb sleep_node_matured = 13;</code>
   */
  io.littlehorse.common.proto.SleepNodeMaturedPbOrBuilder getSleepNodeMaturedOrBuilder();

  /**
   * <code>.littlehorse.DeleteWfRunPb delete_wf_run = 14;</code>
   * @return Whether the deleteWfRun field is set.
   */
  boolean hasDeleteWfRun();
  /**
   * <code>.littlehorse.DeleteWfRunPb delete_wf_run = 14;</code>
   * @return The deleteWfRun.
   */
  io.littlehorse.sdk.common.proto.DeleteWfRunPb getDeleteWfRun();
  /**
   * <code>.littlehorse.DeleteWfRunPb delete_wf_run = 14;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteWfRunPbOrBuilder getDeleteWfRunOrBuilder();

  /**
   * <code>.littlehorse.DeleteWfSpecPb delete_wf_spec = 15;</code>
   * @return Whether the deleteWfSpec field is set.
   */
  boolean hasDeleteWfSpec();
  /**
   * <code>.littlehorse.DeleteWfSpecPb delete_wf_spec = 15;</code>
   * @return The deleteWfSpec.
   */
  io.littlehorse.sdk.common.proto.DeleteWfSpecPb getDeleteWfSpec();
  /**
   * <code>.littlehorse.DeleteWfSpecPb delete_wf_spec = 15;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteWfSpecPbOrBuilder getDeleteWfSpecOrBuilder();

  /**
   * <code>.littlehorse.DeleteTaskDefPb delete_task_def = 16;</code>
   * @return Whether the deleteTaskDef field is set.
   */
  boolean hasDeleteTaskDef();
  /**
   * <code>.littlehorse.DeleteTaskDefPb delete_task_def = 16;</code>
   * @return The deleteTaskDef.
   */
  io.littlehorse.sdk.common.proto.DeleteTaskDefPb getDeleteTaskDef();
  /**
   * <code>.littlehorse.DeleteTaskDefPb delete_task_def = 16;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteTaskDefPbOrBuilder getDeleteTaskDefOrBuilder();

  /**
   * <code>.littlehorse.DeleteExternalEventDefPb delete_external_event_def = 17;</code>
   * @return Whether the deleteExternalEventDef field is set.
   */
  boolean hasDeleteExternalEventDef();
  /**
   * <code>.littlehorse.DeleteExternalEventDefPb delete_external_event_def = 17;</code>
   * @return The deleteExternalEventDef.
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb getDeleteExternalEventDef();
  /**
   * <code>.littlehorse.DeleteExternalEventDefPb delete_external_event_def = 17;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventDefPbOrBuilder getDeleteExternalEventDefOrBuilder();

  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 18;</code>
   * @return Whether the externalEventTimeout field is set.
   */
  boolean hasExternalEventTimeout();
  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 18;</code>
   * @return The externalEventTimeout.
   */
  io.littlehorse.common.proto.ExternalEventNodeTimeoutPb getExternalEventTimeout();
  /**
   * <code>.littlehorse.ExternalEventNodeTimeoutPb external_event_timeout = 18;</code>
   */
  io.littlehorse.common.proto.ExternalEventNodeTimeoutPbOrBuilder getExternalEventTimeoutOrBuilder();

  /**
   * <code>.littlehorse.TaskWorkerHeartBeatPb task_worker_heart_beat = 19;</code>
   * @return Whether the taskWorkerHeartBeat field is set.
   */
  boolean hasTaskWorkerHeartBeat();
  /**
   * <code>.littlehorse.TaskWorkerHeartBeatPb task_worker_heart_beat = 19;</code>
   * @return The taskWorkerHeartBeat.
   */
  io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatPb getTaskWorkerHeartBeat();
  /**
   * <code>.littlehorse.TaskWorkerHeartBeatPb task_worker_heart_beat = 19;</code>
   */
  io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatPbOrBuilder getTaskWorkerHeartBeatOrBuilder();

  /**
   * <code>.littlehorse.DeleteExternalEventPb delete_external_event = 20;</code>
   * @return Whether the deleteExternalEvent field is set.
   */
  boolean hasDeleteExternalEvent();
  /**
   * <code>.littlehorse.DeleteExternalEventPb delete_external_event = 20;</code>
   * @return The deleteExternalEvent.
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventPb getDeleteExternalEvent();
  /**
   * <code>.littlehorse.DeleteExternalEventPb delete_external_event = 20;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventPbOrBuilder getDeleteExternalEventOrBuilder();

  /**
   * <code>.littlehorse.AssignUserTaskRunPb assign_user_task_run = 21;</code>
   * @return Whether the assignUserTaskRun field is set.
   */
  boolean hasAssignUserTaskRun();
  /**
   * <code>.littlehorse.AssignUserTaskRunPb assign_user_task_run = 21;</code>
   * @return The assignUserTaskRun.
   */
  io.littlehorse.sdk.common.proto.AssignUserTaskRunPb getAssignUserTaskRun();
  /**
   * <code>.littlehorse.AssignUserTaskRunPb assign_user_task_run = 21;</code>
   */
  io.littlehorse.sdk.common.proto.AssignUserTaskRunPbOrBuilder getAssignUserTaskRunOrBuilder();

  /**
   * <code>.littlehorse.CompleteUserTaskRunPb complete_user_task_run = 22;</code>
   * @return Whether the completeUserTaskRun field is set.
   */
  boolean hasCompleteUserTaskRun();
  /**
   * <code>.littlehorse.CompleteUserTaskRunPb complete_user_task_run = 22;</code>
   * @return The completeUserTaskRun.
   */
  io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb getCompleteUserTaskRun();
  /**
   * <code>.littlehorse.CompleteUserTaskRunPb complete_user_task_run = 22;</code>
   */
  io.littlehorse.sdk.common.proto.CompleteUserTaskRunPbOrBuilder getCompleteUserTaskRunOrBuilder();

  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 23;</code>
   * @return Whether the putUserTaskDef field is set.
   */
  boolean hasPutUserTaskDef();
  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 23;</code>
   * @return The putUserTaskDef.
   */
  io.littlehorse.sdk.common.proto.PutUserTaskDefRequest getPutUserTaskDef();
  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 23;</code>
   */
  io.littlehorse.sdk.common.proto.PutUserTaskDefRequestOrBuilder getPutUserTaskDefOrBuilder();

  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 24;</code>
   * @return Whether the triggeredTaskRun field is set.
   */
  boolean hasTriggeredTaskRun();
  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 24;</code>
   * @return The triggeredTaskRun.
   */
  io.littlehorse.common.proto.TriggeredTaskRunPb getTriggeredTaskRun();
  /**
   * <code>.littlehorse.TriggeredTaskRunPb triggered_task_run = 24;</code>
   */
  io.littlehorse.common.proto.TriggeredTaskRunPbOrBuilder getTriggeredTaskRunOrBuilder();

  /**
   * <code>.littlehorse.DeleteUserTaskDefPb delete_user_task_def = 25;</code>
   * @return Whether the deleteUserTaskDef field is set.
   */
  boolean hasDeleteUserTaskDef();
  /**
   * <code>.littlehorse.DeleteUserTaskDefPb delete_user_task_def = 25;</code>
   * @return The deleteUserTaskDef.
   */
  io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb getDeleteUserTaskDef();
  /**
   * <code>.littlehorse.DeleteUserTaskDefPb delete_user_task_def = 25;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteUserTaskDefPbOrBuilder getDeleteUserTaskDefOrBuilder();

  /**
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 26;</code>
   * @return Whether the reassignedUserTask field is set.
   */
  boolean hasReassignedUserTask();
  /**
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 26;</code>
   * @return The reassignedUserTask.
   */
  io.littlehorse.common.proto.ReassignedUserTaskPb getReassignedUserTask();
  /**
   * <code>.littlehorse.ReassignedUserTaskPb reassigned_user_task = 26;</code>
   */
  io.littlehorse.common.proto.ReassignedUserTaskPbOrBuilder getReassignedUserTaskOrBuilder();

  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunPb cancel_user_task = 27;</code>
   * @return Whether the cancelUserTask field is set.
   */
  boolean hasCancelUserTask();
  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunPb cancel_user_task = 27;</code>
   * @return The cancelUserTask.
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunPb getCancelUserTask();
  /**
   * <pre>
   * TODO: Add SaveUserTask
   * </pre>
   *
   * <code>.littlehorse.CancelUserTaskRunPb cancel_user_task = 27;</code>
   */
  io.littlehorse.sdk.common.proto.CancelUserTaskRunPbOrBuilder getCancelUserTaskOrBuilder();

  public io.littlehorse.common.proto.CommandPb.CommandCase getCommandCase();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskNodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskNode)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string user_task_def_name = 1;</code>
   * @return The userTaskDefName.
   */
  java.lang.String getUserTaskDefName();
  /**
   * <code>string user_task_def_name = 1;</code>
   * @return The bytes for userTaskDefName.
   */
  com.google.protobuf.ByteString
      getUserTaskDefNameBytes();

  /**
   * <pre>
   * to whom should the User Task Run be assigned?
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment user_group = 2;</code>
   * @return Whether the userGroup field is set.
   */
  boolean hasUserGroup();
  /**
   * <pre>
   * to whom should the User Task Run be assigned?
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment user_group = 2;</code>
   * @return The userGroup.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getUserGroup();
  /**
   * <pre>
   * to whom should the User Task Run be assigned?
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment user_group = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getUserGroupOrBuilder();

  /**
   * <code>optional .littlehorse.VariableAssignment user_id = 3;</code>
   * @return Whether the userId field is set.
   */
  boolean hasUserId();
  /**
   * <code>optional .littlehorse.VariableAssignment user_id = 3;</code>
   * @return The userId.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getUserId();
  /**
   * <code>optional .littlehorse.VariableAssignment user_id = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getUserIdOrBuilder();

  /**
   * <pre>
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   * </pre>
   *
   * <code>repeated .littlehorse.UTActionTrigger actions = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.UTActionTrigger> 
      getActionsList();
  /**
   * <pre>
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   * </pre>
   *
   * <code>repeated .littlehorse.UTActionTrigger actions = 4;</code>
   */
  io.littlehorse.sdk.common.proto.UTActionTrigger getActions(int index);
  /**
   * <pre>
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   * </pre>
   *
   * <code>repeated .littlehorse.UTActionTrigger actions = 4;</code>
   */
  int getActionsCount();
  /**
   * <pre>
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   * </pre>
   *
   * <code>repeated .littlehorse.UTActionTrigger actions = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.UTActionTriggerOrBuilder> 
      getActionsOrBuilderList();
  /**
   * <pre>
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   * </pre>
   *
   * <code>repeated .littlehorse.UTActionTrigger actions = 4;</code>
   */
  io.littlehorse.sdk.common.proto.UTActionTriggerOrBuilder getActionsOrBuilder(
      int index);

  /**
   * <pre>
   * So, once the WfSpec is created, this will be pinned to a version. Customer
   * can optionally specify a specific version or can leave it null, in which
   * case we just use the latest
   * </pre>
   *
   * <code>optional int32 user_task_def_version = 5;</code>
   * @return Whether the userTaskDefVersion field is set.
   */
  boolean hasUserTaskDefVersion();
  /**
   * <pre>
   * So, once the WfSpec is created, this will be pinned to a version. Customer
   * can optionally specify a specific version or can leave it null, in which
   * case we just use the latest
   * </pre>
   *
   * <code>optional int32 user_task_def_version = 5;</code>
   * @return The userTaskDefVersion.
   */
  int getUserTaskDefVersion();

  /**
   * <pre>
   * Allow WfRun-specific notes for this User Task.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment notes = 6;</code>
   * @return Whether the notes field is set.
   */
  boolean hasNotes();
  /**
   * <pre>
   * Allow WfRun-specific notes for this User Task.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment notes = 6;</code>
   * @return The notes.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getNotes();
  /**
   * <pre>
   * Allow WfRun-specific notes for this User Task.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment notes = 6;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getNotesOrBuilder();

  /**
   * <pre>
   * Specifies the name of the exception thrown when the User Task is canceled
   * </pre>
   *
   * <code>optional string on_cancel_exception_name = 7;</code>
   * @return Whether the onCancelExceptionName field is set.
   */
  boolean hasOnCancelExceptionName();
  /**
   * <pre>
   * Specifies the name of the exception thrown when the User Task is canceled
   * </pre>
   *
   * <code>optional string on_cancel_exception_name = 7;</code>
   * @return The onCancelExceptionName.
   */
  java.lang.String getOnCancelExceptionName();
  /**
   * <pre>
   * Specifies the name of the exception thrown when the User Task is canceled
   * </pre>
   *
   * <code>optional string on_cancel_exception_name = 7;</code>
   * @return The bytes for onCancelExceptionName.
   */
  com.google.protobuf.ByteString
      getOnCancelExceptionNameBytes();
}

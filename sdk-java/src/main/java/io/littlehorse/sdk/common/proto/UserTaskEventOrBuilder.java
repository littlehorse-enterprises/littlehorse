// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * the time the event occurred.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <pre>
   * the time the event occurred.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <pre>
   * the time the event occurred.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();

  /**
   * <pre>
   * Denotes that a TaskRun was scheduled via a trigger.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTETaskExecuted task_executed = 2;</code>
   * @return Whether the taskExecuted field is set.
   */
  boolean hasTaskExecuted();
  /**
   * <pre>
   * Denotes that a TaskRun was scheduled via a trigger.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTETaskExecuted task_executed = 2;</code>
   * @return The taskExecuted.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTETaskExecuted getTaskExecuted();
  /**
   * <pre>
   * Denotes that a TaskRun was scheduled via a trigger.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTETaskExecuted task_executed = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTETaskExecutedOrBuilder getTaskExecutedOrBuilder();

  /**
   * <pre>
   * Denotes that the UserTaskRun was assigned.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTEAssigned assigned = 3;</code>
   * @return Whether the assigned field is set.
   */
  boolean hasAssigned();
  /**
   * <pre>
   * Denotes that the UserTaskRun was assigned.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTEAssigned assigned = 3;</code>
   * @return The assigned.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTEAssigned getAssigned();
  /**
   * <pre>
   * Denotes that the UserTaskRun was assigned.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTEAssigned assigned = 3;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTEAssignedOrBuilder getAssignedOrBuilder();

  /**
   * <pre>
   * Denotes that the UserTaskRun was cancelled.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECancelled cancelled = 4;</code>
   * @return Whether the cancelled field is set.
   */
  boolean hasCancelled();
  /**
   * <pre>
   * Denotes that the UserTaskRun was cancelled.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECancelled cancelled = 4;</code>
   * @return The cancelled.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECancelled getCancelled();
  /**
   * <pre>
   * Denotes that the UserTaskRun was cancelled.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECancelled cancelled = 4;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECancelledOrBuilder getCancelledOrBuilder();

  /**
   * <pre>
   * Denotes that the `UserTaskRun` was saved.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTESaved saved = 5;</code>
   * @return Whether the saved field is set.
   */
  boolean hasSaved();
  /**
   * <pre>
   * Denotes that the `UserTaskRun` was saved.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTESaved saved = 5;</code>
   * @return The saved.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTESaved getSaved();
  /**
   * <pre>
   * Denotes that the `UserTaskRun` was saved.
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTESaved saved = 5;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTESavedOrBuilder getSavedOrBuilder();

  /**
   * <pre>
   * Denotes that there was a comment on a `userTaskRun`
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_added = 6;</code>
   * @return Whether the commentAdded field is set.
   */
  boolean hasCommentAdded();
  /**
   * <pre>
   * Denotes that there was a comment on a `userTaskRun`
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_added = 6;</code>
   * @return The commentAdded.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommented getCommentAdded();
  /**
   * <pre>
   * Denotes that there was a comment on a `userTaskRun`
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_added = 6;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentedOrBuilder getCommentAddedOrBuilder();

  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` has been edited
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_edited = 7;</code>
   * @return Whether the commentEdited field is set.
   */
  boolean hasCommentEdited();
  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` has been edited
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_edited = 7;</code>
   * @return The commentEdited.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommented getCommentEdited();
  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` has been edited
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommented comment_edited = 7;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentedOrBuilder getCommentEditedOrBuilder();

  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` was deleted
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommentDeleted comment_deleted = 8;</code>
   * @return Whether the commentDeleted field is set.
   */
  boolean hasCommentDeleted();
  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` was deleted
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommentDeleted comment_deleted = 8;</code>
   * @return The commentDeleted.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentDeleted getCommentDeleted();
  /**
   * <pre>
   * Denotes that a comment on a `userTaskRun` was deleted
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECommentDeleted comment_deleted = 8;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentDeletedOrBuilder getCommentDeletedOrBuilder();

  /**
   * <pre>
   * Denotes that a `userTaskRun` has been completed
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECompleted completed = 9;</code>
   * @return Whether the completed field is set.
   */
  boolean hasCompleted();
  /**
   * <pre>
   * Denotes that a `userTaskRun` has been completed
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECompleted completed = 9;</code>
   * @return The completed.
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECompleted getCompleted();
  /**
   * <pre>
   * Denotes that a `userTaskRun` has been completed
   * </pre>
   *
   * <code>.littlehorse.UserTaskEvent.UTECompleted completed = 9;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent.UTECompletedOrBuilder getCompletedOrBuilder();

  io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase getEventCase();
}

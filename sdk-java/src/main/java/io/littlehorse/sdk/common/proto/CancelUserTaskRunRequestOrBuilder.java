// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: user_tasks.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface CancelUserTaskRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.CancelUserTaskRunRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return Whether the userTaskRunId field is set.
   */
  boolean hasUserTaskRunId();
  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return The userTaskRunId.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunId getUserTaskRunId();
  /**
   * <pre>
   * The id of the UserTaskRun to cancel.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getUserTaskRunIdOrBuilder();
}

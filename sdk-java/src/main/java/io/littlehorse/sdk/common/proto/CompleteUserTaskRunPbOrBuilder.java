// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface CompleteUserTaskRunPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.CompleteUserTaskRunPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.UserTaskRunIdPb user_task_run_id = 1;</code>
   * @return Whether the userTaskRunId field is set.
   */
  boolean hasUserTaskRunId();
  /**
   * <code>.littlehorse.UserTaskRunIdPb user_task_run_id = 1;</code>
   * @return The userTaskRunId.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunIdPb getUserTaskRunId();
  /**
   * <code>.littlehorse.UserTaskRunIdPb user_task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskRunIdPbOrBuilder getUserTaskRunIdOrBuilder();

  /**
   * <code>.littlehorse.UserTaskResultPb result = 2;</code>
   * @return Whether the result field is set.
   */
  boolean hasResult();
  /**
   * <code>.littlehorse.UserTaskResultPb result = 2;</code>
   * @return The result.
   */
  io.littlehorse.sdk.common.proto.UserTaskResultPb getResult();
  /**
   * <code>.littlehorse.UserTaskResultPb result = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskResultPbOrBuilder getResultOrBuilder();

  /**
   * <code>string user_id = 3;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <code>string user_id = 3;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <code>bool ignore_claim = 4;</code>
   * @return The ignoreClaim.
   */
  boolean getIgnoreClaim();
}

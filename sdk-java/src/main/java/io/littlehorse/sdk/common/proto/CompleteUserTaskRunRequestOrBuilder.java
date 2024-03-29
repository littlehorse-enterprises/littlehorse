// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

public interface CompleteUserTaskRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.CompleteUserTaskRunRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The id of UserTaskRun to complete.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return Whether the userTaskRunId field is set.
   */
  boolean hasUserTaskRunId();
  /**
   * <pre>
   * The id of UserTaskRun to complete.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   * @return The userTaskRunId.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunId getUserTaskRunId();
  /**
   * <pre>
   * The id of UserTaskRun to complete.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId user_task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getUserTaskRunIdOrBuilder();

  /**
   * <pre>
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 2;</code>
   */
  int getResultsCount();
  /**
   * <pre>
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 2;</code>
   */
  boolean containsResults(
      java.lang.String key);
  /**
   * Use {@link #getResultsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getResults();
  /**
   * <pre>
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 2;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getResultsMap();
  /**
   * <pre>
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 2;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.VariableValue getResultsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.VariableValue defaultValue);
  /**
   * <pre>
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValue getResultsOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * The ID of the user who executed the task.
   * </pre>
   *
   * <code>string user_id = 3;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <pre>
   * The ID of the user who executed the task.
   * </pre>
   *
   * <code>string user_id = 3;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ReportTaskRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ReportTaskRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * ID of the TaskRun
   * </pre>
   *
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   * @return Whether the taskRunId field is set.
   */
  boolean hasTaskRunId();
  /**
   * <pre>
   * ID of the TaskRun
   * </pre>
   *
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   * @return The taskRunId.
   */
  io.littlehorse.sdk.common.proto.TaskRunId getTaskRunId();
  /**
   * <pre>
   * ID of the TaskRun
   * </pre>
   *
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.TaskRunIdOrBuilder getTaskRunIdOrBuilder();

  /**
   * <pre>
   * When the TaskRun was executed
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 2;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <pre>
   * When the TaskRun was executed
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 2;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <pre>
   * When the TaskRun was executed
   * </pre>
   *
   * <code>.google.protobuf.Timestamp time = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();

  /**
   * <pre>
   * Status of the TaskRun
   * </pre>
   *
   * <code>.littlehorse.TaskStatus status = 3;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <pre>
   * Status of the TaskRun
   * </pre>
   *
   * <code>.littlehorse.TaskStatus status = 3;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.TaskStatus getStatus();

  /**
   * <pre>
   * Optional information for logging or exceptions
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   * @return Whether the logOutput field is set.
   */
  boolean hasLogOutput();
  /**
   * <pre>
   * Optional information for logging or exceptions
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   * @return The logOutput.
   */
  io.littlehorse.sdk.common.proto.VariableValue getLogOutput();
  /**
   * <pre>
   * Optional information for logging or exceptions
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getLogOutputOrBuilder();

  /**
   * <pre>
   * Attempt number of the TaskRun
   * </pre>
   *
   * <code>int32 attempt_number = 6;</code>
   * @return The attemptNumber.
   */
  int getAttemptNumber();

  /**
   * <pre>
   * Successfully completed task
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 4;</code>
   * @return Whether the output field is set.
   */
  boolean hasOutput();
  /**
   * <pre>
   * Successfully completed task
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 4;</code>
   * @return The output.
   */
  io.littlehorse.sdk.common.proto.VariableValue getOutput();
  /**
   * <pre>
   * Successfully completed task
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getOutputOrBuilder();

  /**
   * <pre>
   * Technical error
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 7;</code>
   * @return Whether the error field is set.
   */
  boolean hasError();
  /**
   * <pre>
   * Technical error
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 7;</code>
   * @return The error.
   */
  io.littlehorse.sdk.common.proto.LHTaskError getError();
  /**
   * <pre>
   * Technical error
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 7;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskErrorOrBuilder getErrorOrBuilder();

  /**
   * <pre>
   * Business exception
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   * @return Whether the exception field is set.
   */
  boolean hasException();
  /**
   * <pre>
   * Business exception
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   * @return The exception.
   */
  io.littlehorse.sdk.common.proto.LHTaskException getException();
  /**
   * <pre>
   * Business exception
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskExceptionOrBuilder getExceptionOrBuilder();

  io.littlehorse.sdk.common.proto.ReportTaskRun.ResultCase getResultCase();
}

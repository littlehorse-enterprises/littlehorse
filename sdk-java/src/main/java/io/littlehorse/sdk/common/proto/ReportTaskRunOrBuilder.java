// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ReportTaskRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ReportTaskRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   * @return Whether the taskRunId field is set.
   */
  boolean hasTaskRunId();
  /**
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   * @return The taskRunId.
   */
  io.littlehorse.sdk.common.proto.TaskRunId getTaskRunId();
  /**
   * <code>.littlehorse.TaskRunId task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.TaskRunIdOrBuilder getTaskRunIdOrBuilder();

  /**
   * <code>.google.protobuf.Timestamp time = 2;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <code>.google.protobuf.Timestamp time = 2;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <code>.google.protobuf.Timestamp time = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();

  /**
   * <code>.littlehorse.TaskStatus status = 3;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <code>.littlehorse.TaskStatus status = 3;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.TaskStatus getStatus();

  /**
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   * @return Whether the logOutput field is set.
   */
  boolean hasLogOutput();
  /**
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   * @return The logOutput.
   */
  io.littlehorse.sdk.common.proto.VariableValue getLogOutput();
  /**
   * <code>optional .littlehorse.VariableValue log_output = 5;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getLogOutputOrBuilder();

  /**
   * <code>int32 attempt_number = 6;</code>
   * @return The attemptNumber.
   */
  int getAttemptNumber();

  /**
   * <code>.littlehorse.VariableValue output = 4;</code>
   * @return Whether the output field is set.
   */
  boolean hasOutput();
  /**
   * <code>.littlehorse.VariableValue output = 4;</code>
   * @return The output.
   */
  io.littlehorse.sdk.common.proto.VariableValue getOutput();
  /**
   * <code>.littlehorse.VariableValue output = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getOutputOrBuilder();

  /**
   * <code>.littlehorse.LHTaskError error = 7;</code>
   * @return Whether the error field is set.
   */
  boolean hasError();
  /**
   * <code>.littlehorse.LHTaskError error = 7;</code>
   * @return The error.
   */
  io.littlehorse.sdk.common.proto.LHTaskError getError();
  /**
   * <code>.littlehorse.LHTaskError error = 7;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskErrorOrBuilder getErrorOrBuilder();

  /**
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   * @return Whether the exception field is set.
   */
  boolean hasException();
  /**
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   * @return The exception.
   */
  io.littlehorse.sdk.common.proto.LHTaskException getException();
  /**
   * <code>.littlehorse.LHTaskException exception = 8;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskExceptionOrBuilder getExceptionOrBuilder();

  io.littlehorse.sdk.common.proto.ReportTaskRun.ResultCase getResultCase();
}

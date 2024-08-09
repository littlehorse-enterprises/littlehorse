// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: task_run.proto

package io.littlehorse.sdk.common.proto;

public interface TaskAttemptOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskAttempt)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Optional information provided by the Task Worker SDK for debugging. Usually, if set
   * it contains a stacktrace or it contains information logged via `WorkerContext#log()`.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 2;</code>
   * @return Whether the logOutput field is set.
   */
  boolean hasLogOutput();
  /**
   * <pre>
   * Optional information provided by the Task Worker SDK for debugging. Usually, if set
   * it contains a stacktrace or it contains information logged via `WorkerContext#log()`.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 2;</code>
   * @return The logOutput.
   */
  io.littlehorse.sdk.common.proto.VariableValue getLogOutput();
  /**
   * <pre>
   * Optional information provided by the Task Worker SDK for debugging. Usually, if set
   * it contains a stacktrace or it contains information logged via `WorkerContext#log()`.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue log_output = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getLogOutputOrBuilder();

  /**
   * <pre>
   * The time the TaskAttempt was scheduled on the Task Queue. Not set for a TaskAttempt that is
   * in the TASK_PENDING status; for example, when waiting between retries with exponential
   * backoff.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp schedule_time = 3;</code>
   * @return Whether the scheduleTime field is set.
   */
  boolean hasScheduleTime();
  /**
   * <pre>
   * The time the TaskAttempt was scheduled on the Task Queue. Not set for a TaskAttempt that is
   * in the TASK_PENDING status; for example, when waiting between retries with exponential
   * backoff.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp schedule_time = 3;</code>
   * @return The scheduleTime.
   */
  com.google.protobuf.Timestamp getScheduleTime();
  /**
   * <pre>
   * The time the TaskAttempt was scheduled on the Task Queue. Not set for a TaskAttempt that is
   * in the TASK_PENDING status; for example, when waiting between retries with exponential
   * backoff.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp schedule_time = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getScheduleTimeOrBuilder();

  /**
   * <pre>
   * The time the TaskAttempt was pulled off the queue and sent to a TaskWorker.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp start_time = 4;</code>
   * @return Whether the startTime field is set.
   */
  boolean hasStartTime();
  /**
   * <pre>
   * The time the TaskAttempt was pulled off the queue and sent to a TaskWorker.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp start_time = 4;</code>
   * @return The startTime.
   */
  com.google.protobuf.Timestamp getStartTime();
  /**
   * <pre>
   * The time the TaskAttempt was pulled off the queue and sent to a TaskWorker.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp start_time = 4;</code>
   */
  com.google.protobuf.TimestampOrBuilder getStartTimeOrBuilder();

  /**
   * <pre>
   * The time the TaskAttempt was finished (either completed, reported as failed, or
   * timed out)
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp end_time = 5;</code>
   * @return Whether the endTime field is set.
   */
  boolean hasEndTime();
  /**
   * <pre>
   * The time the TaskAttempt was finished (either completed, reported as failed, or
   * timed out)
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp end_time = 5;</code>
   * @return The endTime.
   */
  com.google.protobuf.Timestamp getEndTime();
  /**
   * <pre>
   * The time the TaskAttempt was finished (either completed, reported as failed, or
   * timed out)
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp end_time = 5;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEndTimeOrBuilder();

  /**
   * <pre>
   * EXPERIMENTAL: the ID of the Task Worker who executed this TaskRun.
   * </pre>
   *
   * <code>string task_worker_id = 7;</code>
   * @return The taskWorkerId.
   */
  java.lang.String getTaskWorkerId();
  /**
   * <pre>
   * EXPERIMENTAL: the ID of the Task Worker who executed this TaskRun.
   * </pre>
   *
   * <code>string task_worker_id = 7;</code>
   * @return The bytes for taskWorkerId.
   */
  com.google.protobuf.ByteString
      getTaskWorkerIdBytes();

  /**
   * <pre>
   * The version of the Task Worker that executed the TaskAttempt.
   * </pre>
   *
   * <code>optional string task_worker_version = 8;</code>
   * @return Whether the taskWorkerVersion field is set.
   */
  boolean hasTaskWorkerVersion();
  /**
   * <pre>
   * The version of the Task Worker that executed the TaskAttempt.
   * </pre>
   *
   * <code>optional string task_worker_version = 8;</code>
   * @return The taskWorkerVersion.
   */
  java.lang.String getTaskWorkerVersion();
  /**
   * <pre>
   * The version of the Task Worker that executed the TaskAttempt.
   * </pre>
   *
   * <code>optional string task_worker_version = 8;</code>
   * @return The bytes for taskWorkerVersion.
   */
  com.google.protobuf.ByteString
      getTaskWorkerVersionBytes();

  /**
   * <pre>
   * The status of this TaskAttempt.
   * </pre>
   *
   * <code>.littlehorse.TaskStatus status = 9;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <pre>
   * The status of this TaskAttempt.
   * </pre>
   *
   * <code>.littlehorse.TaskStatus status = 9;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.TaskStatus getStatus();

  /**
   * <pre>
   * Denotes the Task Function executed properly and returned an output.
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 1;</code>
   * @return Whether the output field is set.
   */
  boolean hasOutput();
  /**
   * <pre>
   * Denotes the Task Function executed properly and returned an output.
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 1;</code>
   * @return The output.
   */
  io.littlehorse.sdk.common.proto.VariableValue getOutput();
  /**
   * <pre>
   * Denotes the Task Function executed properly and returned an output.
   * </pre>
   *
   * <code>.littlehorse.VariableValue output = 1;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getOutputOrBuilder();

  /**
   * <pre>
   * An unexpected technical error was encountered. May or may not be retriable.
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 10;</code>
   * @return Whether the error field is set.
   */
  boolean hasError();
  /**
   * <pre>
   * An unexpected technical error was encountered. May or may not be retriable.
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 10;</code>
   * @return The error.
   */
  io.littlehorse.sdk.common.proto.LHTaskError getError();
  /**
   * <pre>
   * An unexpected technical error was encountered. May or may not be retriable.
   * </pre>
   *
   * <code>.littlehorse.LHTaskError error = 10;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskErrorOrBuilder getErrorOrBuilder();

  /**
   * <pre>
   * The Task Function encountered a business problem and threw a technical exception.
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 11;</code>
   * @return Whether the exception field is set.
   */
  boolean hasException();
  /**
   * <pre>
   * The Task Function encountered a business problem and threw a technical exception.
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 11;</code>
   * @return The exception.
   */
  io.littlehorse.sdk.common.proto.LHTaskException getException();
  /**
   * <pre>
   * The Task Function encountered a business problem and threw a technical exception.
   * </pre>
   *
   * <code>.littlehorse.LHTaskException exception = 11;</code>
   */
  io.littlehorse.sdk.common.proto.LHTaskExceptionOrBuilder getExceptionOrBuilder();

  /**
   * <code>bool masked_value = 12;</code>
   * @return The maskedValue.
   */
  boolean getMaskedValue();

  io.littlehorse.sdk.common.proto.TaskAttempt.ResultCase getResultCase();
}

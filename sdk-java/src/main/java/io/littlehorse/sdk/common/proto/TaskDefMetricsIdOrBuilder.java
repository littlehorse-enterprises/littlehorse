// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: object_id.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface TaskDefMetricsIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskDefMetricsId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return Whether the windowStart field is set.
   */
  boolean hasWindowStart();
  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return The windowStart.
   */
  com.google.protobuf.Timestamp getWindowStart();
  /**
   * <pre>
   * The timestamp at which this metrics window starts.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder();

  /**
   * <pre>
   * The length of this window.
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The enum numeric value on the wire for windowType.
   */
  int getWindowTypeValue();
  /**
   * <pre>
   * The length of this window.
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The windowType.
   */
  io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowType();

  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   * @return Whether the taskDefId field is set.
   */
  boolean hasTaskDefId();
  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   * @return The taskDefId.
   */
  io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId();
  /**
   * <pre>
   * The TaskDefId that this metrics window reports on.
   * </pre>
   *
   * <code>.littlehorse.TaskDefId task_def_id = 3;</code>
   */
  io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface RepartitionCommandPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.RepartitionCommandPb)
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
   * <code>.littlehorse.CreateRemoteTagPb create_remote_tag = 5;</code>
   * @return Whether the createRemoteTag field is set.
   */
  boolean hasCreateRemoteTag();
  /**
   * <code>.littlehorse.CreateRemoteTagPb create_remote_tag = 5;</code>
   * @return The createRemoteTag.
   */
  io.littlehorse.common.proto.CreateRemoteTagPb getCreateRemoteTag();
  /**
   * <code>.littlehorse.CreateRemoteTagPb create_remote_tag = 5;</code>
   */
  io.littlehorse.common.proto.CreateRemoteTagPbOrBuilder getCreateRemoteTagOrBuilder();

  /**
   * <code>.littlehorse.RemoveRemoteTagPb remove_remote_tag = 6;</code>
   * @return Whether the removeRemoteTag field is set.
   */
  boolean hasRemoveRemoteTag();
  /**
   * <code>.littlehorse.RemoveRemoteTagPb remove_remote_tag = 6;</code>
   * @return The removeRemoteTag.
   */
  io.littlehorse.common.proto.RemoveRemoteTagPb getRemoveRemoteTag();
  /**
   * <code>.littlehorse.RemoveRemoteTagPb remove_remote_tag = 6;</code>
   */
  io.littlehorse.common.proto.RemoveRemoteTagPbOrBuilder getRemoveRemoteTagOrBuilder();

  /**
   * <code>.littlehorse.AggregateWfMetrics aggregate_wf_metrics = 7;</code>
   * @return Whether the aggregateWfMetrics field is set.
   */
  boolean hasAggregateWfMetrics();
  /**
   * <code>.littlehorse.AggregateWfMetrics aggregate_wf_metrics = 7;</code>
   * @return The aggregateWfMetrics.
   */
  io.littlehorse.common.proto.AggregateWfMetrics getAggregateWfMetrics();
  /**
   * <code>.littlehorse.AggregateWfMetrics aggregate_wf_metrics = 7;</code>
   */
  io.littlehorse.common.proto.AggregateWfMetricsOrBuilder getAggregateWfMetricsOrBuilder();

  /**
   * <code>.littlehorse.AggregateTaskMetrics aggregate_task_metrics = 8;</code>
   * @return Whether the aggregateTaskMetrics field is set.
   */
  boolean hasAggregateTaskMetrics();
  /**
   * <code>.littlehorse.AggregateTaskMetrics aggregate_task_metrics = 8;</code>
   * @return The aggregateTaskMetrics.
   */
  io.littlehorse.common.proto.AggregateTaskMetrics getAggregateTaskMetrics();
  /**
   * <code>.littlehorse.AggregateTaskMetrics aggregate_task_metrics = 8;</code>
   */
  io.littlehorse.common.proto.AggregateTaskMetricsOrBuilder getAggregateTaskMetricsOrBuilder();

  io.littlehorse.common.proto.RepartitionCommandPb.RepartitionCommandCase getRepartitionCommandCase();
}

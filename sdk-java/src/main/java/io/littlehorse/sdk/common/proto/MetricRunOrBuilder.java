// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package io.littlehorse.sdk.common.proto;

public interface MetricRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.MetricRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.MetricRunId getId();
  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder getIdOrBuilder();

  /**
   * <code>double value = 2;</code>
   * @return The value.
   */
  double getValue();

  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();
}

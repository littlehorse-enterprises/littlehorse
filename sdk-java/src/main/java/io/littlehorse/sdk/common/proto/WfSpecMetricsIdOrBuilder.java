// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: object_id.proto

package io.littlehorse.sdk.common.proto;

public interface WfSpecMetricsIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.WfSpecMetricsId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return Whether the windowStart field is set.
   */
  boolean hasWindowStart();
  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   * @return The windowStart.
   */
  com.google.protobuf.Timestamp getWindowStart();
  /**
   * <code>.google.protobuf.Timestamp window_start = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder();

  /**
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The enum numeric value on the wire for windowType.
   */
  int getWindowTypeValue();
  /**
   * <code>.littlehorse.MetricsWindowLength window_type = 2;</code>
   * @return The windowType.
   */
  io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowType();

  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return Whether the wfSpecId field is set.
   */
  boolean hasWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return The wfSpecId.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder();
}

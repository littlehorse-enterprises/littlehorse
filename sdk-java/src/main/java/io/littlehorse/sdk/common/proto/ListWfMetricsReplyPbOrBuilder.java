// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ListWfMetricsReplyPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ListWfMetricsReplyPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.LHResponseCodePb code = 1;</code>
   * @return The enum numeric value on the wire for code.
   */
  int getCodeValue();
  /**
   * <code>.littlehorse.LHResponseCodePb code = 1;</code>
   * @return The code.
   */
  io.littlehorse.sdk.common.proto.LHResponseCodePb getCode();

  /**
   * <code>optional string message = 2;</code>
   * @return Whether the message field is set.
   */
  boolean hasMessage();
  /**
   * <code>optional string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>optional string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>repeated .littlehorse.WfSpecMetrics results = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.WfSpecMetrics> 
      getResultsList();
  /**
   * <code>repeated .littlehorse.WfSpecMetrics results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecMetrics getResults(int index);
  /**
   * <code>repeated .littlehorse.WfSpecMetrics results = 3;</code>
   */
  int getResultsCount();
  /**
   * <code>repeated .littlehorse.WfSpecMetrics results = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.WfSpecMetricsOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <code>repeated .littlehorse.WfSpecMetrics results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecMetricsOrBuilder getResultsOrBuilder(
      int index);
}

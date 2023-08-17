// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ListTaskMetricsReplyPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ListTaskMetricsReplyPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.LHResponseCode code = 1;</code>
   * @return The enum numeric value on the wire for code.
   */
  int getCodeValue();
  /**
   * <code>.littlehorse.LHResponseCode code = 1;</code>
   * @return The code.
   */
  io.littlehorse.sdk.common.proto.LHResponseCode getCode();

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
   * <code>repeated .littlehorse.TaskDefMetrics results = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.TaskDefMetrics> 
      getResultsList();
  /**
   * <code>repeated .littlehorse.TaskDefMetrics results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.TaskDefMetrics getResults(int index);
  /**
   * <code>repeated .littlehorse.TaskDefMetrics results = 3;</code>
   */
  int getResultsCount();
  /**
   * <code>repeated .littlehorse.TaskDefMetrics results = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.TaskDefMetricsOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <code>repeated .littlehorse.TaskDefMetrics results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.TaskDefMetricsOrBuilder getResultsOrBuilder(
      int index);
}

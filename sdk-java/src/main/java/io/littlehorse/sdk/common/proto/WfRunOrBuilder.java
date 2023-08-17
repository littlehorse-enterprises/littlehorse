// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface WfRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.WfRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>string wf_spec_name = 2;</code>
   * @return The wfSpecName.
   */
  java.lang.String getWfSpecName();
  /**
   * <code>string wf_spec_name = 2;</code>
   * @return The bytes for wfSpecName.
   */
  com.google.protobuf.ByteString
      getWfSpecNameBytes();

  /**
   * <code>int32 wf_spec_version = 3;</code>
   * @return The wfSpecVersion.
   */
  int getWfSpecVersion();

  /**
   * <code>.littlehorse.LHStatus status = 4;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <code>.littlehorse.LHStatus status = 4;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.LHStatus getStatus();

  /**
   * <code>.google.protobuf.Timestamp start_time = 6;</code>
   * @return Whether the startTime field is set.
   */
  boolean hasStartTime();
  /**
   * <code>.google.protobuf.Timestamp start_time = 6;</code>
   * @return The startTime.
   */
  com.google.protobuf.Timestamp getStartTime();
  /**
   * <code>.google.protobuf.Timestamp start_time = 6;</code>
   */
  com.google.protobuf.TimestampOrBuilder getStartTimeOrBuilder();

  /**
   * <code>optional .google.protobuf.Timestamp end_time = 7;</code>
   * @return Whether the endTime field is set.
   */
  boolean hasEndTime();
  /**
   * <code>optional .google.protobuf.Timestamp end_time = 7;</code>
   * @return The endTime.
   */
  com.google.protobuf.Timestamp getEndTime();
  /**
   * <code>optional .google.protobuf.Timestamp end_time = 7;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEndTimeOrBuilder();

  /**
   * <code>repeated .littlehorse.ThreadRun thread_runs = 8;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.ThreadRun> 
      getThreadRunsList();
  /**
   * <code>repeated .littlehorse.ThreadRun thread_runs = 8;</code>
   */
  io.littlehorse.sdk.common.proto.ThreadRun getThreadRuns(int index);
  /**
   * <code>repeated .littlehorse.ThreadRun thread_runs = 8;</code>
   */
  int getThreadRunsCount();
  /**
   * <code>repeated .littlehorse.ThreadRun thread_runs = 8;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.ThreadRunOrBuilder> 
      getThreadRunsOrBuilderList();
  /**
   * <code>repeated .littlehorse.ThreadRun thread_runs = 8;</code>
   */
  io.littlehorse.sdk.common.proto.ThreadRunOrBuilder getThreadRunsOrBuilder(
      int index);

  /**
   * <code>repeated .littlehorse.PendingInterrupt pending_interrupts = 9;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.PendingInterrupt> 
      getPendingInterruptsList();
  /**
   * <code>repeated .littlehorse.PendingInterrupt pending_interrupts = 9;</code>
   */
  io.littlehorse.sdk.common.proto.PendingInterrupt getPendingInterrupts(int index);
  /**
   * <code>repeated .littlehorse.PendingInterrupt pending_interrupts = 9;</code>
   */
  int getPendingInterruptsCount();
  /**
   * <code>repeated .littlehorse.PendingInterrupt pending_interrupts = 9;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.PendingInterruptOrBuilder> 
      getPendingInterruptsOrBuilderList();
  /**
   * <code>repeated .littlehorse.PendingInterrupt pending_interrupts = 9;</code>
   */
  io.littlehorse.sdk.common.proto.PendingInterruptOrBuilder getPendingInterruptsOrBuilder(
      int index);

  /**
   * <code>repeated .littlehorse.PendingFailureHandler pending_failures = 10;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.PendingFailureHandler> 
      getPendingFailuresList();
  /**
   * <code>repeated .littlehorse.PendingFailureHandler pending_failures = 10;</code>
   */
  io.littlehorse.sdk.common.proto.PendingFailureHandler getPendingFailures(int index);
  /**
   * <code>repeated .littlehorse.PendingFailureHandler pending_failures = 10;</code>
   */
  int getPendingFailuresCount();
  /**
   * <code>repeated .littlehorse.PendingFailureHandler pending_failures = 10;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.PendingFailureHandlerOrBuilder> 
      getPendingFailuresOrBuilderList();
  /**
   * <code>repeated .littlehorse.PendingFailureHandler pending_failures = 10;</code>
   */
  io.littlehorse.sdk.common.proto.PendingFailureHandlerOrBuilder getPendingFailuresOrBuilder(
      int index);
}

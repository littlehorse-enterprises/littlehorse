// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: storage.proto

package io.littlehorse.common.proto;

public interface WfRunStoredInventoryOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.WfRunStoredInventory)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();

  /**
   * <code>repeated .littlehorse.ExternalEventId external_events = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.ExternalEventId> 
      getExternalEventsList();
  /**
   * <code>repeated .littlehorse.ExternalEventId external_events = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventId getExternalEvents(int index);
  /**
   * <code>repeated .littlehorse.ExternalEventId external_events = 3;</code>
   */
  int getExternalEventsCount();
  /**
   * <code>repeated .littlehorse.ExternalEventId external_events = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder> 
      getExternalEventsOrBuilderList();
  /**
   * <code>repeated .littlehorse.ExternalEventId external_events = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getExternalEventsOrBuilder(
      int index);
}

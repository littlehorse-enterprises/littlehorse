// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: node_run.proto

package io.littlehorse.sdk.common.proto;

public interface ExternalEventRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExternalEventRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ExternalEventDefId that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return Whether the externalEventDefId field is set.
   */
  boolean hasExternalEventDefId();
  /**
   * <pre>
   * The ExternalEventDefId that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return The externalEventDefId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId();
  /**
   * <pre>
   * The ExternalEventDefId that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder();

  /**
   * <pre>
   * The time that the ExternalEvent arrived. Unset if still waiting.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp event_time = 2;</code>
   * @return Whether the eventTime field is set.
   */
  boolean hasEventTime();
  /**
   * <pre>
   * The time that the ExternalEvent arrived. Unset if still waiting.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp event_time = 2;</code>
   * @return The eventTime.
   */
  com.google.protobuf.Timestamp getEventTime();
  /**
   * <pre>
   * The time that the ExternalEvent arrived. Unset if still waiting.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp event_time = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEventTimeOrBuilder();

  /**
   * <pre>
   * The ExternalEventId of the ExternalEvent. Unset if still waiting.
   * </pre>
   *
   * <code>optional .littlehorse.ExternalEventId external_event_id = 3;</code>
   * @return Whether the externalEventId field is set.
   */
  boolean hasExternalEventId();
  /**
   * <pre>
   * The ExternalEventId of the ExternalEvent. Unset if still waiting.
   * </pre>
   *
   * <code>optional .littlehorse.ExternalEventId external_event_id = 3;</code>
   * @return The externalEventId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventId getExternalEventId();
  /**
   * <pre>
   * The ExternalEventId of the ExternalEvent. Unset if still waiting.
   * </pre>
   *
   * <code>optional .littlehorse.ExternalEventId external_event_id = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getExternalEventIdOrBuilder();
}

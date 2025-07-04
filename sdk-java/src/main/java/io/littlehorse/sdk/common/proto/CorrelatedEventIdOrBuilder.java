// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: object_id.proto

package io.littlehorse.sdk.common.proto;

public interface CorrelatedEventIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.CorrelatedEventId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The key of a CorrelatedEvent is used as the correlation ID for ExternalEventNodeRuns.
   * </pre>
   *
   * <code>string key = 1;</code>
   * @return The key.
   */
  java.lang.String getKey();
  /**
   * <pre>
   * The key of a CorrelatedEvent is used as the correlation ID for ExternalEventNodeRuns.
   * </pre>
   *
   * <code>string key = 1;</code>
   * @return The bytes for key.
   */
  com.google.protobuf.ByteString
      getKeyBytes();

  /**
   * <pre>
   * The ExternalEventDef for this CorrelatedEvent and any ExternalEvent's that are created
   * by it.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   * @return Whether the externalEventDefId field is set.
   */
  boolean hasExternalEventDefId();
  /**
   * <pre>
   * The ExternalEventDef for this CorrelatedEvent and any ExternalEvent's that are created
   * by it.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   * @return The externalEventDefId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId();
  /**
   * <pre>
   * The ExternalEventDef for this CorrelatedEvent and any ExternalEvent's that are created
   * by it.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface DeleteExternalEventRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.DeleteExternalEventRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the ExternalEvent to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The ID of the ExternalEvent to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.ExternalEventId getId();
  /**
   * <pre>
   * The ID of the ExternalEvent to delete.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getIdOrBuilder();
}

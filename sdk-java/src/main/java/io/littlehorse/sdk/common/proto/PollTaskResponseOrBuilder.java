// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface PollTaskResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PollTaskResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * If possible, a ScheduledTask is returned.
   * </pre>
   *
   * <code>optional .littlehorse.ScheduledTask result = 1;</code>
   * @return Whether the result field is set.
   */
  boolean hasResult();
  /**
   * <pre>
   * If possible, a ScheduledTask is returned.
   * </pre>
   *
   * <code>optional .littlehorse.ScheduledTask result = 1;</code>
   * @return The result.
   */
  io.littlehorse.sdk.common.proto.ScheduledTask getResult();
  /**
   * <pre>
   * If possible, a ScheduledTask is returned.
   * </pre>
   *
   * <code>optional .littlehorse.ScheduledTask result = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ScheduledTaskOrBuilder getResultOrBuilder();
}

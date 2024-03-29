// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: task_run.proto

package io.littlehorse.sdk.common.proto;

public interface LHTaskExceptionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.LHTaskException)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The user-defined Failure name, for example, "credit-card-declined"
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The user-defined Failure name, for example, "credit-card-declined"
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * Human readadble description of the failure.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <pre>
   * Human readadble description of the failure.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>.littlehorse.VariableValue content = 3;</code>
   * @return Whether the content field is set.
   */
  boolean hasContent();
  /**
   * <code>.littlehorse.VariableValue content = 3;</code>
   * @return The content.
   */
  io.littlehorse.sdk.common.proto.VariableValue getContent();
  /**
   * <code>.littlehorse.VariableValue content = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getContentOrBuilder();
}

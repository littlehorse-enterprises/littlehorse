// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: task_run.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface LHTaskErrorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.LHTaskError)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The technical error code.
   * </pre>
   *
   * <code>.littlehorse.LHErrorType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * The technical error code.
   * </pre>
   *
   * <code>.littlehorse.LHErrorType type = 1;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.LHErrorType getType();

  /**
   * <pre>
   * Human readable message for debugging.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <pre>
   * Human readable message for debugging.
   * </pre>
   *
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();
}

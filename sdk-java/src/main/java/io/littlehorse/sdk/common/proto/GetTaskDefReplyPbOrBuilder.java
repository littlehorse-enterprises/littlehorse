// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface GetTaskDefReplyPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.GetTaskDefReplyPb)
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
   * <code>optional .littlehorse.TaskDef result = 3;</code>
   * @return Whether the result field is set.
   */
  boolean hasResult();
  /**
   * <code>optional .littlehorse.TaskDef result = 3;</code>
   * @return The result.
   */
  io.littlehorse.sdk.common.proto.TaskDef getResult();
  /**
   * <code>optional .littlehorse.TaskDef result = 3;</code>
   */
  io.littlehorse.sdk.common.proto.TaskDefOrBuilder getResultOrBuilder();
}

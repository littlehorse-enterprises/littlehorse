// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface FailureDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.FailureDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string failure_name = 1;</code>
   * @return The failureName.
   */
  java.lang.String getFailureName();
  /**
   * <code>string failure_name = 1;</code>
   * @return The bytes for failureName.
   */
  com.google.protobuf.ByteString
      getFailureNameBytes();

  /**
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>optional .littlehorse.VariableAssignment content = 3;</code>
   * @return Whether the content field is set.
   */
  boolean hasContent();
  /**
   * <code>optional .littlehorse.VariableAssignment content = 3;</code>
   * @return The content.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getContent();
  /**
   * <code>optional .littlehorse.VariableAssignment content = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getContentOrBuilder();
}

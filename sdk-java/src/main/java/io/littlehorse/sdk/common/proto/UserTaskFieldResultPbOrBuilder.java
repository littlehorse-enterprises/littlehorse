// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskFieldResultPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskFieldResultPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional .littlehorse.VariableValue value = 2;</code>
   * @return Whether the value field is set.
   */
  boolean hasValue();
  /**
   * <code>optional .littlehorse.VariableValue value = 2;</code>
   * @return The value.
   */
  io.littlehorse.sdk.common.proto.VariableValue getValue();
  /**
   * <code>optional .littlehorse.VariableValue value = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getValueOrBuilder();
}

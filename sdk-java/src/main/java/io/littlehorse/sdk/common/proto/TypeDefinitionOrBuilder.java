// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

public interface TypeDefinitionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TypeDefinition)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The basic type of the value. Will become a `oneof` once StructDef's and Struct's
   * are implemented according to issue #880.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * The basic type of the value. Will become a `oneof` once StructDef's and Struct's
   * are implemented according to issue #880.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 1;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.VariableType getType();

  /**
   * <pre>
   * Set to true if values of this type contain sensitive information and must be masked.
   * </pre>
   *
   * <code>bool masked = 4;</code>
   * @return The masked.
   */
  boolean getMasked();
}

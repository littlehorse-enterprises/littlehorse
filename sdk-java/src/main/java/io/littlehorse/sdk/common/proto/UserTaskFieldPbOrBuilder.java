// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskFieldPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskFieldPb)
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
   * <code>.littlehorse.VariableType type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.littlehorse.VariableType type = 2;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.VariableType getType();

  /**
   * <code>optional string description = 3;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <code>optional string description = 3;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 3;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>string display_name = 4;</code>
   * @return The displayName.
   */
  java.lang.String getDisplayName();
  /**
   * <code>string display_name = 4;</code>
   * @return The bytes for displayName.
   */
  com.google.protobuf.ByteString
      getDisplayNameBytes();

  /**
   * <pre>
   * Later versions will allow stuff such as:
   * 1. Validation (eg. email address, integer between 1-10, etc)
   * 2. Nested object structures
   * 3. Multi-Page forms (survey-js style)
   * 4. Conditional rendering of forms based on input (surveyjs style)
   * 5. Default values and optional fields
   * </pre>
   *
   * <code>bool required = 5;</code>
   * @return The required.
   */
  boolean getRequired();
}

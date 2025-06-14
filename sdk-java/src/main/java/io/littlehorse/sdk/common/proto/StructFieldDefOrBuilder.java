// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: struct_def.proto

package io.littlehorse.sdk.common.proto;

public interface StructFieldDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.StructFieldDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   * @return Whether the fieldType field is set.
   */
  boolean hasFieldType();
  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   * @return The fieldType.
   */
  io.littlehorse.sdk.common.proto.TypeDefinition getFieldType();
  /**
   * <pre>
   * The type of the field.
   * </pre>
   *
   * <code>.littlehorse.TypeDefinition field_type = 1;</code>
   */
  io.littlehorse.sdk.common.proto.TypeDefinitionOrBuilder getFieldTypeOrBuilder();

  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   * @return Whether the defaultValue field is set.
   */
  boolean hasDefaultValue();
  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   * @return The defaultValue.
   */
  io.littlehorse.sdk.common.proto.VariableValue getDefaultValue();
  /**
   * <pre>
   * The default value of the field, which should match the Field Type. If not
   * provided, then the field is treated as required.
   * </pre>
   *
   * <code>optional .littlehorse.VariableValue default_value = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getDefaultValueOrBuilder();
}

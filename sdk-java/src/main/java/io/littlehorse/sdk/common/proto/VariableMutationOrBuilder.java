// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

public interface VariableMutationOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.VariableMutation)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string lhs_name = 1;</code>
   * @return The lhsName.
   */
  java.lang.String getLhsName();
  /**
   * <code>string lhs_name = 1;</code>
   * @return The bytes for lhsName.
   */
  com.google.protobuf.ByteString
      getLhsNameBytes();

  /**
   * <code>optional string lhs_json_path = 2;</code>
   * @return Whether the lhsJsonPath field is set.
   */
  boolean hasLhsJsonPath();
  /**
   * <code>optional string lhs_json_path = 2;</code>
   * @return The lhsJsonPath.
   */
  java.lang.String getLhsJsonPath();
  /**
   * <code>optional string lhs_json_path = 2;</code>
   * @return The bytes for lhsJsonPath.
   */
  com.google.protobuf.ByteString
      getLhsJsonPathBytes();

  /**
   * <code>.littlehorse.VariableMutationType operation = 3;</code>
   * @return The enum numeric value on the wire for operation.
   */
  int getOperationValue();
  /**
   * <code>.littlehorse.VariableMutationType operation = 3;</code>
   * @return The operation.
   */
  io.littlehorse.sdk.common.proto.VariableMutationType getOperation();

  /**
   * <code>.littlehorse.VariableAssignment source_variable = 4;</code>
   * @return Whether the sourceVariable field is set.
   */
  boolean hasSourceVariable();
  /**
   * <code>.littlehorse.VariableAssignment source_variable = 4;</code>
   * @return The sourceVariable.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getSourceVariable();
  /**
   * <code>.littlehorse.VariableAssignment source_variable = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getSourceVariableOrBuilder();

  /**
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   * @return Whether the literalValue field is set.
   */
  boolean hasLiteralValue();
  /**
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   * @return The literalValue.
   */
  io.littlehorse.sdk.common.proto.VariableValue getLiteralValue();
  /**
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getLiteralValueOrBuilder();

  /**
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   * @return Whether the nodeOutput field is set.
   */
  boolean hasNodeOutput();
  /**
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   * @return The nodeOutput.
   */
  io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource getNodeOutput();
  /**
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSourceOrBuilder getNodeOutputOrBuilder();

  io.littlehorse.sdk.common.proto.VariableMutation.RhsValueCase getRhsValueCase();
}

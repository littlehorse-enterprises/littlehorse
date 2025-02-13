// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: common_wfspec.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface VariableMutationOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.VariableMutation)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the variable to mutate
   * </pre>
   *
   * <code>string lhs_name = 1;</code>
   * @return The lhsName.
   */
  java.lang.String getLhsName();
  /**
   * <pre>
   * The name of the variable to mutate
   * </pre>
   *
   * <code>string lhs_name = 1;</code>
   * @return The bytes for lhsName.
   */
  com.google.protobuf.ByteString
      getLhsNameBytes();

  /**
   * <pre>
   * For JSON_ARR and JSON_OBJ variables, this allows you to optionally mutate
   * a specific sub-field of the variable.
   * </pre>
   *
   * <code>optional string lhs_json_path = 2;</code>
   * @return Whether the lhsJsonPath field is set.
   */
  boolean hasLhsJsonPath();
  /**
   * <pre>
   * For JSON_ARR and JSON_OBJ variables, this allows you to optionally mutate
   * a specific sub-field of the variable.
   * </pre>
   *
   * <code>optional string lhs_json_path = 2;</code>
   * @return The lhsJsonPath.
   */
  java.lang.String getLhsJsonPath();
  /**
   * <pre>
   * For JSON_ARR and JSON_OBJ variables, this allows you to optionally mutate
   * a specific sub-field of the variable.
   * </pre>
   *
   * <code>optional string lhs_json_path = 2;</code>
   * @return The bytes for lhsJsonPath.
   */
  com.google.protobuf.ByteString
      getLhsJsonPathBytes();

  /**
   * <pre>
   * Defines the operation that we are executing.
   * </pre>
   *
   * <code>.littlehorse.VariableMutationType operation = 3;</code>
   * @return The enum numeric value on the wire for operation.
   */
  int getOperationValue();
  /**
   * <pre>
   * Defines the operation that we are executing.
   * </pre>
   *
   * <code>.littlehorse.VariableMutationType operation = 3;</code>
   * @return The operation.
   */
  io.littlehorse.sdk.common.proto.VariableMutationType getOperation();

  /**
   * <pre>
   * Assigns the value to be used as the RHS of the mutation.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment rhs_assignment = 4;</code>
   * @return Whether the rhsAssignment field is set.
   */
  boolean hasRhsAssignment();
  /**
   * <pre>
   * Assigns the value to be used as the RHS of the mutation.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment rhs_assignment = 4;</code>
   * @return The rhsAssignment.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getRhsAssignment();
  /**
   * <pre>
   * Assigns the value to be used as the RHS of the mutation.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment rhs_assignment = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getRhsAssignmentOrBuilder();

  /**
   * <pre>
   * Use a literal value as the RHS. DEPRECATED: use rhs_assignment.literal_value
   * instead.
   * </pre>
   *
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   * @return Whether the literalValue field is set.
   */
  boolean hasLiteralValue();
  /**
   * <pre>
   * Use a literal value as the RHS. DEPRECATED: use rhs_assignment.literal_value
   * instead.
   * </pre>
   *
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   * @return The literalValue.
   */
  io.littlehorse.sdk.common.proto.VariableValue getLiteralValue();
  /**
   * <pre>
   * Use a literal value as the RHS. DEPRECATED: use rhs_assignment.literal_value
   * instead.
   * </pre>
   *
   * <code>.littlehorse.VariableValue literal_value = 5;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getLiteralValueOrBuilder();

  /**
   * <pre>
   * Use the output of the current node as the RHS. DEPRECATED: use
   * rhs_assignment.node_output instead.
   * </pre>
   *
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   * @return Whether the nodeOutput field is set.
   */
  boolean hasNodeOutput();
  /**
   * <pre>
   * Use the output of the current node as the RHS. DEPRECATED: use
   * rhs_assignment.node_output instead.
   * </pre>
   *
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   * @return The nodeOutput.
   */
  io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSource getNodeOutput();
  /**
   * <pre>
   * Use the output of the current node as the RHS. DEPRECATED: use
   * rhs_assignment.node_output instead.
   * </pre>
   *
   * <code>.littlehorse.VariableMutation.NodeOutputSource node_output = 6;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutation.NodeOutputSourceOrBuilder getNodeOutputOrBuilder();

  io.littlehorse.sdk.common.proto.VariableMutation.RhsValueCase getRhsValueCase();
}

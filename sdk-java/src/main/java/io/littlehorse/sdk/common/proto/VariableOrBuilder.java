// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: variable.proto

package io.littlehorse.sdk.common.proto;

public interface VariableOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Variable)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * ID of this Variable. Note that the VariableId contains the relevant
   * WfRunId inside it, the threadRunNumber, and the name of the Variabe.
   * </pre>
   *
   * <code>.littlehorse.VariableId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * ID of this Variable. Note that the VariableId contains the relevant
   * WfRunId inside it, the threadRunNumber, and the name of the Variabe.
   * </pre>
   *
   * <code>.littlehorse.VariableId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.VariableId getId();
  /**
   * <pre>
   * ID of this Variable. Note that the VariableId contains the relevant
   * WfRunId inside it, the threadRunNumber, and the name of the Variabe.
   * </pre>
   *
   * <code>.littlehorse.VariableId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.VariableIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * The value of this Variable.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   * @return Whether the value field is set.
   */
  boolean hasValue();
  /**
   * <pre>
   * The value of this Variable.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   * @return The value.
   */
  io.littlehorse.sdk.common.proto.VariableValue getValue();
  /**
   * <pre>
   * The value of this Variable.
   * </pre>
   *
   * <code>.littlehorse.VariableValue value = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getValueOrBuilder();

  /**
   * <pre>
   * When the Variable was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * When the Variable was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * When the Variable was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <pre>
   * The ID of the WfSpec that this Variable belongs to.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   * @return Whether the wfSpecId field is set.
   */
  boolean hasWfSpecId();
  /**
   * <pre>
   * The ID of the WfSpec that this Variable belongs to.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   * @return The wfSpecId.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId();
  /**
   * <pre>
   * The ID of the WfSpec that this Variable belongs to.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder();

  /**
   * <pre>
   * Marks a variable to show masked values
   * </pre>
   *
   * <code>bool masked = 5;</code>
   * @return The masked.
   */
  boolean getMasked();
}

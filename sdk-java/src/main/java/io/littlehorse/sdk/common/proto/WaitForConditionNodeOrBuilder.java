// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_spec.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface WaitForConditionNodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.WaitForConditionNode)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The condition that this node will block for.
   * </pre>
   *
   * <code>.littlehorse.EdgeCondition condition = 1;</code>
   * @return Whether the condition field is set.
   */
  boolean hasCondition();
  /**
   * <pre>
   * The condition that this node will block for.
   * </pre>
   *
   * <code>.littlehorse.EdgeCondition condition = 1;</code>
   * @return The condition.
   */
  io.littlehorse.sdk.common.proto.EdgeCondition getCondition();
  /**
   * <pre>
   * The condition that this node will block for.
   * </pre>
   *
   * <code>.littlehorse.EdgeCondition condition = 1;</code>
   */
  io.littlehorse.sdk.common.proto.EdgeConditionOrBuilder getConditionOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface EdgeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Edge)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the Node that the Edge points to.
   * </pre>
   *
   * <code>string sink_node_name = 1;</code>
   * @return The sinkNodeName.
   */
  java.lang.String getSinkNodeName();
  /**
   * <pre>
   * The name of the Node that the Edge points to.
   * </pre>
   *
   * <code>string sink_node_name = 1;</code>
   * @return The bytes for sinkNodeName.
   */
  com.google.protobuf.ByteString
      getSinkNodeNameBytes();

  /**
   * <pre>
   * The Condition on which this Edge will be traversed. When choosing an Edge
   * to travel after the completion of a NodeRun, the Edges are evaluated in
   * order. The first one to either have no condition or have a condition which
   * evaluates to `true` is taken.
   * </pre>
   *
   * <code>optional .littlehorse.EdgeCondition condition = 2;</code>
   * @return Whether the condition field is set.
   */
  boolean hasCondition();
  /**
   * <pre>
   * The Condition on which this Edge will be traversed. When choosing an Edge
   * to travel after the completion of a NodeRun, the Edges are evaluated in
   * order. The first one to either have no condition or have a condition which
   * evaluates to `true` is taken.
   * </pre>
   *
   * <code>optional .littlehorse.EdgeCondition condition = 2;</code>
   * @return The condition.
   */
  io.littlehorse.sdk.common.proto.EdgeCondition getCondition();
  /**
   * <pre>
   * The Condition on which this Edge will be traversed. When choosing an Edge
   * to travel after the completion of a NodeRun, the Edges are evaluated in
   * order. The first one to either have no condition or have a condition which
   * evaluates to `true` is taken.
   * </pre>
   *
   * <code>optional .littlehorse.EdgeCondition condition = 2;</code>
   */
  io.littlehorse.sdk.common.proto.EdgeConditionOrBuilder getConditionOrBuilder();

  /**
   * <pre>
   * Ordered list of Variable Mutations to execute when traversing this Edge.
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMutation variable_mutations = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.VariableMutation> 
      getVariableMutationsList();
  /**
   * <pre>
   * Ordered list of Variable Mutations to execute when traversing this Edge.
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMutation variable_mutations = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutation getVariableMutations(int index);
  /**
   * <pre>
   * Ordered list of Variable Mutations to execute when traversing this Edge.
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMutation variable_mutations = 3;</code>
   */
  int getVariableMutationsCount();
  /**
   * <pre>
   * Ordered list of Variable Mutations to execute when traversing this Edge.
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMutation variable_mutations = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.VariableMutationOrBuilder> 
      getVariableMutationsOrBuilderList();
  /**
   * <pre>
   * Ordered list of Variable Mutations to execute when traversing this Edge.
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMutation variable_mutations = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMutationOrBuilder getVariableMutationsOrBuilder(
      int index);
}

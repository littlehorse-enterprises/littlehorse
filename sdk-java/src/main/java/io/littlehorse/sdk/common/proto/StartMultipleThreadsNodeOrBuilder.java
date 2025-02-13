// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_spec.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface StartMultipleThreadsNodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.StartMultipleThreadsNode)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the ThreadSpec to spawn.
   * </pre>
   *
   * <code>string thread_spec_name = 1;</code>
   * @return The threadSpecName.
   */
  java.lang.String getThreadSpecName();
  /**
   * <pre>
   * The name of the ThreadSpec to spawn.
   * </pre>
   *
   * <code>string thread_spec_name = 1;</code>
   * @return The bytes for threadSpecName.
   */
  com.google.protobuf.ByteString
      getThreadSpecNameBytes();

  /**
   * <pre>
   * Variables which are passed into the child ThreadRuns. These assignments are
   * the same for all spawned threads.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableAssignment&gt; variables = 2;</code>
   */
  int getVariablesCount();
  /**
   * <pre>
   * Variables which are passed into the child ThreadRuns. These assignments are
   * the same for all spawned threads.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableAssignment&gt; variables = 2;</code>
   */
  boolean containsVariables(
      java.lang.String key);
  /**
   * Use {@link #getVariablesMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableAssignment>
  getVariables();
  /**
   * <pre>
   * Variables which are passed into the child ThreadRuns. These assignments are
   * the same for all spawned threads.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableAssignment&gt; variables = 2;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableAssignment>
  getVariablesMap();
  /**
   * <pre>
   * Variables which are passed into the child ThreadRuns. These assignments are
   * the same for all spawned threads.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableAssignment&gt; variables = 2;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.VariableAssignment getVariablesOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.VariableAssignment defaultValue);
  /**
   * <pre>
   * Variables which are passed into the child ThreadRuns. These assignments are
   * the same for all spawned threads.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableAssignment&gt; variables = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getVariablesOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * Assignment that resolves to a JSON_ARR. For each element in the list, a child
   * ThreadRun is started. The reserved `INPUT` variable for each Child is set to the
   * corresponding item in the list.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment iterable = 3;</code>
   * @return Whether the iterable field is set.
   */
  boolean hasIterable();
  /**
   * <pre>
   * Assignment that resolves to a JSON_ARR. For each element in the list, a child
   * ThreadRun is started. The reserved `INPUT` variable for each Child is set to the
   * corresponding item in the list.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment iterable = 3;</code>
   * @return The iterable.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getIterable();
  /**
   * <pre>
   * Assignment that resolves to a JSON_ARR. For each element in the list, a child
   * ThreadRun is started. The reserved `INPUT` variable for each Child is set to the
   * corresponding item in the list.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment iterable = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getIterableOrBuilder();
}

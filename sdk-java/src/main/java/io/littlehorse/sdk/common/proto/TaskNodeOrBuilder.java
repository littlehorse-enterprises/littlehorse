// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

public interface TaskNodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskNode)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   * @return Whether the taskDefId field is set.
   */
  boolean hasTaskDefId();
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   * @return The taskDefId.
   */
  io.littlehorse.sdk.common.proto.TaskDefId getTaskDefId();
  /**
   * <code>.littlehorse.TaskDefId task_def_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.TaskDefIdOrBuilder getTaskDefIdOrBuilder();

  /**
   * <code>int32 timeout_seconds = 2;</code>
   * @return The timeoutSeconds.
   */
  int getTimeoutSeconds();

  /**
   * <code>int32 retries = 3;</code>
   * @return The retries.
   */
  int getRetries();

  /**
   * <code>repeated .littlehorse.VariableAssignment variables = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.VariableAssignment> 
      getVariablesList();
  /**
   * <code>repeated .littlehorse.VariableAssignment variables = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getVariables(int index);
  /**
   * <code>repeated .littlehorse.VariableAssignment variables = 4;</code>
   */
  int getVariablesCount();
  /**
   * <code>repeated .littlehorse.VariableAssignment variables = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder> 
      getVariablesOrBuilderList();
  /**
   * <code>repeated .littlehorse.VariableAssignment variables = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getVariablesOrBuilder(
      int index);
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: task_run.proto

package io.littlehorse.sdk.common.proto;

public interface TaskNodeReferenceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskNodeReference)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the NodeRun which caused this TASK to be scheduled.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   * @return Whether the nodeRunId field is set.
   */
  boolean hasNodeRunId();
  /**
   * <pre>
   * The ID of the NodeRun which caused this TASK to be scheduled.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   * @return The nodeRunId.
   */
  io.littlehorse.sdk.common.proto.NodeRunId getNodeRunId();
  /**
   * <pre>
   * The ID of the NodeRun which caused this TASK to be scheduled.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getNodeRunIdOrBuilder();
}

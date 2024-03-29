// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: node_run.proto

package io.littlehorse.sdk.common.proto;

public interface TaskNodeRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskNodeRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the TaskRun. Note that if the ThreadRun was halted when it arrived
   * at this TASK Node, then the task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.TaskRunId task_run_id = 1;</code>
   * @return Whether the taskRunId field is set.
   */
  boolean hasTaskRunId();
  /**
   * <pre>
   * The ID of the TaskRun. Note that if the ThreadRun was halted when it arrived
   * at this TASK Node, then the task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.TaskRunId task_run_id = 1;</code>
   * @return The taskRunId.
   */
  io.littlehorse.sdk.common.proto.TaskRunId getTaskRunId();
  /**
   * <pre>
   * The ID of the TaskRun. Note that if the ThreadRun was halted when it arrived
   * at this TASK Node, then the task_run_id will be unset.
   * </pre>
   *
   * <code>optional .littlehorse.TaskRunId task_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.TaskRunIdOrBuilder getTaskRunIdOrBuilder();
}

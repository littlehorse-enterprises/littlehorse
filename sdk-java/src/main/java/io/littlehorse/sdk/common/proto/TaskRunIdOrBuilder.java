// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: object_id.proto

package io.littlehorse.sdk.common.proto;

public interface TaskRunIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.TaskRunId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * WfRunId for this TaskRun. Note that every TaskRun is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <pre>
   * WfRunId for this TaskRun. Note that every TaskRun is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <pre>
   * WfRunId for this TaskRun. Note that every TaskRun is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();

  /**
   * <pre>
   * Unique identifier for this TaskRun. Unique among the WfRun.
   * </pre>
   *
   * <code>string task_guid = 2;</code>
   * @return The taskGuid.
   */
  java.lang.String getTaskGuid();
  /**
   * <pre>
   * Unique identifier for this TaskRun. Unique among the WfRun.
   * </pre>
   *
   * <code>string task_guid = 2;</code>
   * @return The bytes for taskGuid.
   */
  com.google.protobuf.ByteString
      getTaskGuidBytes();
}

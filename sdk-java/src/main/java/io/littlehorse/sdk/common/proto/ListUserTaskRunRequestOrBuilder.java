// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface ListUserTaskRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ListUserTaskRunRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The WfRun for which to list UserTaskRuns
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <pre>
   * The WfRun for which to list UserTaskRuns
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <pre>
   * The WfRun for which to list UserTaskRuns
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ListTaskRunsRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ListTaskRunsRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The WfRun for which to list TaskRun's
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <pre>
   * The WfRun for which to list TaskRun's
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <pre>
   * The WfRun for which to list TaskRun's
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();
}

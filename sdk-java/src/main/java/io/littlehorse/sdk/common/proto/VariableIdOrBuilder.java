// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: object_id.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface VariableIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.VariableId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * WfRunId for the variable. Note that every Variable is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <pre>
   * WfRunId for the variable. Note that every Variable is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <pre>
   * WfRunId for the variable. Note that every Variable is associated with
   * a WfRun.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();

  /**
   * <pre>
   * Each Variable is owned by a specific ThreadRun inside the WfRun it belongs
   * to. This is that ThreadRun's number.
   * </pre>
   *
   * <code>int32 thread_run_number = 2;</code>
   * @return The threadRunNumber.
   */
  int getThreadRunNumber();

  /**
   * <pre>
   * The name of the variable.
   * </pre>
   *
   * <code>string name = 3;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the variable.
   * </pre>
   *
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();
}

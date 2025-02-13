// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_spec.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface ThreadRetentionPolicyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ThreadRetentionPolicy)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Delete associated ThreadRun's X seconds after they terminate, regardless
   * of status.
   * </pre>
   *
   * <code>int64 seconds_after_thread_termination = 1;</code>
   * @return Whether the secondsAfterThreadTermination field is set.
   */
  boolean hasSecondsAfterThreadTermination();
  /**
   * <pre>
   * Delete associated ThreadRun's X seconds after they terminate, regardless
   * of status.
   * </pre>
   *
   * <code>int64 seconds_after_thread_termination = 1;</code>
   * @return The secondsAfterThreadTermination.
   */
  long getSecondsAfterThreadTermination();

  io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.ThreadGcPolicyCase getThreadGcPolicyCase();
}

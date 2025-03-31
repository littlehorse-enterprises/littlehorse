// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_run.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface PendingFailureHandlerOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PendingFailureHandler)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ThreadRun that failed.
   * </pre>
   *
   * <code>int32 failed_thread_run = 1;</code>
   * @return The failedThreadRun.
   */
  int getFailedThreadRun();

  /**
   * <pre>
   * The name of the ThreadSpec to run to handle the failure.
   * </pre>
   *
   * <code>string handler_spec_name = 2;</code>
   * @return The handlerSpecName.
   */
  java.lang.String getHandlerSpecName();
  /**
   * <pre>
   * The name of the ThreadSpec to run to handle the failure.
   * </pre>
   *
   * <code>string handler_spec_name = 2;</code>
   * @return The bytes for handlerSpecName.
   */
  com.google.protobuf.ByteString
      getHandlerSpecNameBytes();
}

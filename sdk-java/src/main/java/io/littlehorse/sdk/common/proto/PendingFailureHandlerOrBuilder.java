// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_run.proto

package io.littlehorse.sdk.common.proto;

public interface PendingFailureHandlerOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PendingFailureHandler)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 failed_thread_run = 1;</code>
   * @return The failedThreadRun.
   */
  int getFailedThreadRun();

  /**
   * <code>string handler_spec_name = 2;</code>
   * @return The handlerSpecName.
   */
  java.lang.String getHandlerSpecName();
  /**
   * <code>string handler_spec_name = 2;</code>
   * @return The bytes for handlerSpecName.
   */
  com.google.protobuf.ByteString
      getHandlerSpecNameBytes();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: interactive_query.proto

package io.littlehorse.common.proto;

public interface InternalWaitForWfEventRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.InternalWaitForWfEventRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   * @return Whether the request field is set.
   */
  boolean hasRequest();
  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   * @return The request.
   */
  io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest getRequest();
  /**
   * <pre>
   * For now, we just pass the raw input from the external server. That's all we need to know.
   * </pre>
   *
   * <code>.littlehorse.AwaitWorkflowEventRequest request = 1;</code>
   */
  io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequestOrBuilder getRequestOrBuilder();
}

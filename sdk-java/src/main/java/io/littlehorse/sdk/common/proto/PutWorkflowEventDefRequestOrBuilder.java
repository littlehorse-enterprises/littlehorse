// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface PutWorkflowEventDefRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutWorkflowEventDefRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the resulting WorkflowEventDef.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the resulting WorkflowEventDef.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * The type of 'content' thrown with a WorkflowEvent based on this WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * The type of 'content' thrown with a WorkflowEvent based on this WorkflowEventDef.
   * </pre>
   *
   * <code>.littlehorse.VariableType type = 2;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.VariableType getType();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: external_event.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface ExternalEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExternalEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the ExternalEvent. This contains WfRunId, ExternalEventDefId,
   * and a unique guid which can be used for idempotency of the `PutExternalEvent`
   * rpc call.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The ID of the ExternalEvent. This contains WfRunId, ExternalEventDefId,
   * and a unique guid which can be used for idempotency of the `PutExternalEvent`
   * rpc call.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.ExternalEventId getId();
  /**
   * <pre>
   * The ID of the ExternalEvent. This contains WfRunId, ExternalEventDefId,
   * and a unique guid which can be used for idempotency of the `PutExternalEvent`
   * rpc call.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * The time the ExternalEvent was registered with LittleHorse.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * The time the ExternalEvent was registered with LittleHorse.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * The time the ExternalEvent was registered with LittleHorse.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <pre>
   * The payload of this ExternalEvent.
   * </pre>
   *
   * <code>.littlehorse.VariableValue content = 3;</code>
   * @return Whether the content field is set.
   */
  boolean hasContent();
  /**
   * <pre>
   * The payload of this ExternalEvent.
   * </pre>
   *
   * <code>.littlehorse.VariableValue content = 3;</code>
   * @return The content.
   */
  io.littlehorse.sdk.common.proto.VariableValue getContent();
  /**
   * <pre>
   * The payload of this ExternalEvent.
   * </pre>
   *
   * <code>.littlehorse.VariableValue content = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValueOrBuilder getContentOrBuilder();

  /**
   * <pre>
   * If the ExternalEvent was claimed by a specific ThreadRun (via Interrupt or
   * EXTERNAL_EVENT Node), this is set to the number of the relevant ThreadRun.
   * </pre>
   *
   * <code>optional int32 thread_run_number = 4;</code>
   * @return Whether the threadRunNumber field is set.
   */
  boolean hasThreadRunNumber();
  /**
   * <pre>
   * If the ExternalEvent was claimed by a specific ThreadRun (via Interrupt or
   * EXTERNAL_EVENT Node), this is set to the number of the relevant ThreadRun.
   * </pre>
   *
   * <code>optional int32 thread_run_number = 4;</code>
   * @return The threadRunNumber.
   */
  int getThreadRunNumber();

  /**
   * <pre>
   * If the ExternalEvent was claimed by a specific ThreadRun (via EXTERNAL_EVENT
   * Node; note that in the case of an Interrupt the node_run_position will never
   * be set), this is set to the number of the relevant NodeRun.
   * </pre>
   *
   * <code>optional int32 node_run_position = 5;</code>
   * @return Whether the nodeRunPosition field is set.
   */
  boolean hasNodeRunPosition();
  /**
   * <pre>
   * If the ExternalEvent was claimed by a specific ThreadRun (via EXTERNAL_EVENT
   * Node; note that in the case of an Interrupt the node_run_position will never
   * be set), this is set to the number of the relevant NodeRun.
   * </pre>
   *
   * <code>optional int32 node_run_position = 5;</code>
   * @return The nodeRunPosition.
   */
  int getNodeRunPosition();

  /**
   * <pre>
   * Whether the ExternalEvent has been claimed by a WfRun.
   * </pre>
   *
   * <code>bool claimed = 6;</code>
   * @return The claimed.
   */
  boolean getClaimed();
}

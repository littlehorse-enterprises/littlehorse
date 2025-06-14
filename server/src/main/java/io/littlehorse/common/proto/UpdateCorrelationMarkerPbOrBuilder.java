// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface UpdateCorrelationMarkerPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UpdateCorrelationMarkerPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string correlation_key = 1;</code>
   * @return The correlationKey.
   */
  java.lang.String getCorrelationKey();
  /**
   * <code>string correlation_key = 1;</code>
   * @return The bytes for correlationKey.
   */
  com.google.protobuf.ByteString
      getCorrelationKeyBytes();

  /**
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   * @return Whether the externalEventDefId field is set.
   */
  boolean hasExternalEventDefId();
  /**
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   * @return The externalEventDefId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId();
  /**
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 2;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder();

  /**
   * <code>.littlehorse.NodeRunId waiting_node_run = 3;</code>
   * @return Whether the waitingNodeRun field is set.
   */
  boolean hasWaitingNodeRun();
  /**
   * <code>.littlehorse.NodeRunId waiting_node_run = 3;</code>
   * @return The waitingNodeRun.
   */
  io.littlehorse.sdk.common.proto.NodeRunId getWaitingNodeRun();
  /**
   * <code>.littlehorse.NodeRunId waiting_node_run = 3;</code>
   */
  io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getWaitingNodeRunOrBuilder();

  /**
   * <code>.littlehorse.UpdateCorrelationMarkerPb.CorrelationUpdateAction action = 4;</code>
   * @return The enum numeric value on the wire for action.
   */
  int getActionValue();
  /**
   * <code>.littlehorse.UpdateCorrelationMarkerPb.CorrelationUpdateAction action = 4;</code>
   * @return The action.
   */
  io.littlehorse.common.proto.UpdateCorrelationMarkerPb.CorrelationUpdateAction getAction();
}

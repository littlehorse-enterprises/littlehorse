// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

public interface ExternalEventNodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExternalEventNode)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the ExternalEventDef that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return Whether the externalEventDefId field is set.
   */
  boolean hasExternalEventDefId();
  /**
   * <pre>
   * The ID of the ExternalEventDef that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   * @return The externalEventDefId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId();
  /**
   * <pre>
   * The ID of the ExternalEventDef that we are waiting for.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder();

  /**
   * <pre>
   * Determines the maximum amount of time that the NodeRun will wait for the
   * ExternalEvent to arrive.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment timeout_seconds = 2;</code>
   * @return Whether the timeoutSeconds field is set.
   */
  boolean hasTimeoutSeconds();
  /**
   * <pre>
   * Determines the maximum amount of time that the NodeRun will wait for the
   * ExternalEvent to arrive.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment timeout_seconds = 2;</code>
   * @return The timeoutSeconds.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getTimeoutSeconds();
  /**
   * <pre>
   * Determines the maximum amount of time that the NodeRun will wait for the
   * ExternalEvent to arrive.
   * </pre>
   *
   * <code>.littlehorse.VariableAssignment timeout_seconds = 2;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getTimeoutSecondsOrBuilder();

  /**
   * <pre>
   * If set, it will be possible to complete this ExternalEventNode with a CorrelatedEvent
   * using the correlation key provided here.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment correlation_key = 3;</code>
   * @return Whether the correlationKey field is set.
   */
  boolean hasCorrelationKey();
  /**
   * <pre>
   * If set, it will be possible to complete this ExternalEventNode with a CorrelatedEvent
   * using the correlation key provided here.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment correlation_key = 3;</code>
   * @return The correlationKey.
   */
  io.littlehorse.sdk.common.proto.VariableAssignment getCorrelationKey();
  /**
   * <pre>
   * If set, it will be possible to complete this ExternalEventNode with a CorrelatedEvent
   * using the correlation key provided here.
   * </pre>
   *
   * <code>optional .littlehorse.VariableAssignment correlation_key = 3;</code>
   */
  io.littlehorse.sdk.common.proto.VariableAssignmentOrBuilder getCorrelationKeyOrBuilder();

  /**
   * <pre>
   * Specifies whether the correlation key is sensitive data which should be masked.
   * Ignored if correlation_key is not set.
   * </pre>
   *
   * <code>bool mask_correlation_key = 4;</code>
   * @return The maskCorrelationKey.
   */
  boolean getMaskCorrelationKey();
}

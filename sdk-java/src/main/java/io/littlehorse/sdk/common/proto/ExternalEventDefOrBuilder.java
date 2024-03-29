// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: external_event.proto

package io.littlehorse.sdk.common.proto;

public interface ExternalEventDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExternalEventDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The id of the ExternalEventDef.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The id of the ExternalEventDef.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getId();
  /**
   * <pre>
   * The id of the ExternalEventDef.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * When the ExternalEventDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * When the ExternalEventDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * When the ExternalEventDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <pre>
   * The retention policy for ExternalEvent's of this ExternalEventDef. This applies to the
   * ExternalEvent **only before** it is matched with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventRetentionPolicy retention_policy = 3;</code>
   * @return Whether the retentionPolicy field is set.
   */
  boolean hasRetentionPolicy();
  /**
   * <pre>
   * The retention policy for ExternalEvent's of this ExternalEventDef. This applies to the
   * ExternalEvent **only before** it is matched with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventRetentionPolicy retention_policy = 3;</code>
   * @return The retentionPolicy.
   */
  io.littlehorse.sdk.common.proto.ExternalEventRetentionPolicy getRetentionPolicy();
  /**
   * <pre>
   * The retention policy for ExternalEvent's of this ExternalEventDef. This applies to the
   * ExternalEvent **only before** it is matched with a WfRun.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventRetentionPolicy retention_policy = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventRetentionPolicyOrBuilder getRetentionPolicyOrBuilder();
}

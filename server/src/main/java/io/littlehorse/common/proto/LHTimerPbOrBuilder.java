// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

public interface LHTimerPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.LHTimerPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return Whether the maturationTime field is set.
   */
  boolean hasMaturationTime();
  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   * @return The maturationTime.
   */
  com.google.protobuf.Timestamp getMaturationTime();
  /**
   * <code>.google.protobuf.Timestamp maturation_time = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getMaturationTimeOrBuilder();

  /**
   * <code>string key = 2;</code>
   * @return The key.
   */
  java.lang.String getKey();
  /**
   * <code>string key = 2;</code>
   * @return The bytes for key.
   */
  com.google.protobuf.ByteString
      getKeyBytes();

  /**
   * <code>string topic = 3;</code>
   * @return The topic.
   */
  java.lang.String getTopic();
  /**
   * <code>string topic = 3;</code>
   * @return The bytes for topic.
   */
  com.google.protobuf.ByteString
      getTopicBytes();

  /**
   * <code>bytes payload = 4;</code>
   * @return The payload.
   */
  com.google.protobuf.ByteString getPayload();

  /**
   * <pre>
   * default if not set
   * </pre>
   *
   * <code>optional .littlehorse.TenantId tenant_id = 5;</code>
   * @return Whether the tenantId field is set.
   */
  boolean hasTenantId();
  /**
   * <pre>
   * default if not set
   * </pre>
   *
   * <code>optional .littlehorse.TenantId tenant_id = 5;</code>
   * @return The tenantId.
   */
  io.littlehorse.sdk.common.proto.TenantId getTenantId();
  /**
   * <pre>
   * default if not set
   * </pre>
   *
   * <code>optional .littlehorse.TenantId tenant_id = 5;</code>
   */
  io.littlehorse.sdk.common.proto.TenantIdOrBuilder getTenantIdOrBuilder();

  /**
   * <pre>
   * anonymous if not set
   * </pre>
   *
   * <code>optional .littlehorse.PrincipalId principal_id = 6;</code>
   * @return Whether the principalId field is set.
   */
  boolean hasPrincipalId();
  /**
   * <pre>
   * anonymous if not set
   * </pre>
   *
   * <code>optional .littlehorse.PrincipalId principal_id = 6;</code>
   * @return The principalId.
   */
  io.littlehorse.sdk.common.proto.PrincipalId getPrincipalId();
  /**
   * <pre>
   * anonymous if not set
   * </pre>
   *
   * <code>optional .littlehorse.PrincipalId principal_id = 6;</code>
   */
  io.littlehorse.sdk.common.proto.PrincipalIdOrBuilder getPrincipalIdOrBuilder();
}

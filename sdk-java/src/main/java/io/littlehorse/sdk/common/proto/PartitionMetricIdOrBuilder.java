// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package io.littlehorse.sdk.common.proto;

public interface PartitionMetricIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PartitionMetricId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.MetricId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <code>.littlehorse.MetricId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.MetricId getId();
  /**
   * <code>.littlehorse.MetricId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.MetricIdOrBuilder getIdOrBuilder();

  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   * @return Whether the tenantId field is set.
   */
  boolean hasTenantId();
  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   * @return The tenantId.
   */
  io.littlehorse.sdk.common.proto.TenantId getTenantId();
  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   */
  io.littlehorse.sdk.common.proto.TenantIdOrBuilder getTenantIdOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface AggregateWfMetricsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.AggregateWfMetrics)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return Whether the wfSpecId field is set.
   */
  boolean hasWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return The wfSpecId.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder();

  /**
   * <code>string tenant_id = 2;</code>
   * @return The tenantId.
   */
  java.lang.String getTenantId();
  /**
   * <code>string tenant_id = 2;</code>
   * @return The bytes for tenantId.
   */
  com.google.protobuf.ByteString
      getTenantIdBytes();

  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  java.util.List<io.littlehorse.common.proto.WfMetricUpdate> 
      getMetricUpdatesList();
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  io.littlehorse.common.proto.WfMetricUpdate getMetricUpdates(int index);
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  int getMetricUpdatesCount();
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  java.util.List<? extends io.littlehorse.common.proto.WfMetricUpdateOrBuilder> 
      getMetricUpdatesOrBuilderList();
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  io.littlehorse.common.proto.WfMetricUpdateOrBuilder getMetricUpdatesOrBuilder(
      int index);
}

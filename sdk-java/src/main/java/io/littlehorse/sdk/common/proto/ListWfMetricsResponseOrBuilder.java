// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface ListWfMetricsResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ListWfMetricsResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * List of WfSpec Metrics Windows
   * </pre>
   *
   * <code>repeated .littlehorse.WfSpecMetrics results = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.WfSpecMetrics> 
      getResultsList();
  /**
   * <pre>
   * List of WfSpec Metrics Windows
   * </pre>
   *
   * <code>repeated .littlehorse.WfSpecMetrics results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecMetrics getResults(int index);
  /**
   * <pre>
   * List of WfSpec Metrics Windows
   * </pre>
   *
   * <code>repeated .littlehorse.WfSpecMetrics results = 1;</code>
   */
  int getResultsCount();
  /**
   * <pre>
   * List of WfSpec Metrics Windows
   * </pre>
   *
   * <code>repeated .littlehorse.WfSpecMetrics results = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.WfSpecMetricsOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <pre>
   * List of WfSpec Metrics Windows
   * </pre>
   *
   * <code>repeated .littlehorse.WfSpecMetrics results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecMetricsOrBuilder getResultsOrBuilder(
      int index);
}

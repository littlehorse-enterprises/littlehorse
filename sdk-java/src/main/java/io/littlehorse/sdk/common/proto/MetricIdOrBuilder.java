// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package io.littlehorse.sdk.common.proto;

public interface MetricIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.MetricId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.MeasurableObject measurable = 1;</code>
   * @return The enum numeric value on the wire for measurable.
   */
  int getMeasurableValue();
  /**
   * <code>.littlehorse.MeasurableObject measurable = 1;</code>
   * @return The measurable.
   */
  io.littlehorse.sdk.common.proto.MeasurableObject getMeasurable();

  /**
   * <code>.littlehorse.MetricType type = 2;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.littlehorse.MetricType type = 2;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.MetricType getType();
}

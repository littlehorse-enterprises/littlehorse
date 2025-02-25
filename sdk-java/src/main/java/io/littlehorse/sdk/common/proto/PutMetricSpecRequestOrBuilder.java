// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface PutMetricSpecRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutMetricSpecRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Defines how the metric will be computed and collected
   * </pre>
   *
   * <code>.littlehorse.MetricType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * Defines how the metric will be computed and collected
   * </pre>
   *
   * <code>.littlehorse.MetricType type = 1;</code>
   * @return The type.
   */
  io.littlehorse.sdk.common.proto.MetricType getType();

  /**
   * <pre>
   * Refers to a specific LittleHorse object
   * </pre>
   *
   * <code>.littlehorse.MeasurableObject object = 2;</code>
   * @return Whether the object field is set.
   */
  boolean hasObject();
  /**
   * <pre>
   * Refers to a specific LittleHorse object
   * </pre>
   *
   * <code>.littlehorse.MeasurableObject object = 2;</code>
   * @return The enum numeric value on the wire for object.
   */
  int getObjectValue();
  /**
   * <pre>
   * Refers to a specific LittleHorse object
   * </pre>
   *
   * <code>.littlehorse.MeasurableObject object = 2;</code>
   * @return The object.
   */
  io.littlehorse.sdk.common.proto.MeasurableObject getObject();

  /**
   * <pre>
   * Refers to a specific node
   * </pre>
   *
   * <code>.littlehorse.NodeReference node = 3;</code>
   * @return Whether the node field is set.
   */
  boolean hasNode();
  /**
   * <pre>
   * Refers to a specific node
   * </pre>
   *
   * <code>.littlehorse.NodeReference node = 3;</code>
   * @return The node.
   */
  io.littlehorse.sdk.common.proto.NodeReference getNode();
  /**
   * <pre>
   * Refers to a specific node
   * </pre>
   *
   * <code>.littlehorse.NodeReference node = 3;</code>
   */
  io.littlehorse.sdk.common.proto.NodeReferenceOrBuilder getNodeOrBuilder();

  /**
   * <pre>
   * Refers to a specific WfSpec
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   * @return Whether the wfSpecId field is set.
   */
  boolean hasWfSpecId();
  /**
   * <pre>
   * Refers to a specific WfSpec
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   * @return The wfSpecId.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId();
  /**
   * <pre>
   * Refers to a specific WfSpec
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 4;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder();

  /**
   * <pre>
   * Refers to a specific ThreadSpec within  a WfSpec
   * </pre>
   *
   * <code>.littlehorse.ThreadSpecReference thread_spec = 5;</code>
   * @return Whether the threadSpec field is set.
   */
  boolean hasThreadSpec();
  /**
   * <pre>
   * Refers to a specific ThreadSpec within  a WfSpec
   * </pre>
   *
   * <code>.littlehorse.ThreadSpecReference thread_spec = 5;</code>
   * @return The threadSpec.
   */
  io.littlehorse.sdk.common.proto.ThreadSpecReference getThreadSpec();
  /**
   * <pre>
   * Refers to a specific ThreadSpec within  a WfSpec
   * </pre>
   *
   * <code>.littlehorse.ThreadSpecReference thread_spec = 5;</code>
   */
  io.littlehorse.sdk.common.proto.ThreadSpecReferenceOrBuilder getThreadSpecOrBuilder();

  /**
   * <pre>
   * Defines a length for every window recorded for this MetricSpec
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 6;</code>
   * @return Whether the windowLength field is set.
   */
  boolean hasWindowLength();
  /**
   * <pre>
   * Defines a length for every window recorded for this MetricSpec
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 6;</code>
   * @return The windowLength.
   */
  com.google.protobuf.Duration getWindowLength();
  /**
   * <pre>
   * Defines a length for every window recorded for this MetricSpec
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 6;</code>
   */
  com.google.protobuf.DurationOrBuilder getWindowLengthOrBuilder();

  io.littlehorse.sdk.common.proto.PutMetricSpecRequest.ReferenceCase getReferenceCase();
}

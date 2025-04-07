// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: object_id.proto

package io.littlehorse.sdk.common.proto;

public interface MetricSpecIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.MetricSpecId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.MeasurableObject object = 1;</code>
   * @return Whether the object field is set.
   */
  boolean hasObject();
  /**
   * <code>.littlehorse.MeasurableObject object = 1;</code>
   * @return The enum numeric value on the wire for object.
   */
  int getObjectValue();
  /**
   * <code>.littlehorse.MeasurableObject object = 1;</code>
   * @return The object.
   */
  io.littlehorse.sdk.common.proto.MeasurableObject getObject();

  /**
   * <code>.littlehorse.NodeReference node = 2;</code>
   * @return Whether the node field is set.
   */
  boolean hasNode();
  /**
   * <code>.littlehorse.NodeReference node = 2;</code>
   * @return The node.
   */
  io.littlehorse.sdk.common.proto.NodeReference getNode();
  /**
   * <code>.littlehorse.NodeReference node = 2;</code>
   */
  io.littlehorse.sdk.common.proto.NodeReferenceOrBuilder getNodeOrBuilder();

  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return Whether the wfSpecId field is set.
   */
  boolean hasWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   * @return The wfSpecId.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId();
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 3;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder();

  /**
   * <code>.littlehorse.ThreadSpecReference thread_spec = 4;</code>
   * @return Whether the threadSpec field is set.
   */
  boolean hasThreadSpec();
  /**
   * <code>.littlehorse.ThreadSpecReference thread_spec = 4;</code>
   * @return The threadSpec.
   */
  io.littlehorse.sdk.common.proto.ThreadSpecReference getThreadSpec();
  /**
   * <code>.littlehorse.ThreadSpecReference thread_spec = 4;</code>
   */
  io.littlehorse.sdk.common.proto.ThreadSpecReferenceOrBuilder getThreadSpecOrBuilder();

  io.littlehorse.sdk.common.proto.MetricSpecId.ReferenceCase getReferenceCase();
}

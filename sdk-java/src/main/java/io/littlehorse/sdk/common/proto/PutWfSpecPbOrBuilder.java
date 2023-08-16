// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface PutWfSpecPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutWfSpecPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>map&lt;string, .littlehorse.ThreadSpec&gt; thread_specs = 5;</code>
   */
  int getThreadSpecsCount();
  /**
   * <code>map&lt;string, .littlehorse.ThreadSpec&gt; thread_specs = 5;</code>
   */
  boolean containsThreadSpecs(
      java.lang.String key);
  /**
   * Use {@link #getThreadSpecsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.ThreadSpec>
  getThreadSpecs();
  /**
   * <code>map&lt;string, .littlehorse.ThreadSpec&gt; thread_specs = 5;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.ThreadSpec>
  getThreadSpecsMap();
  /**
   * <code>map&lt;string, .littlehorse.ThreadSpec&gt; thread_specs = 5;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.ThreadSpec getThreadSpecsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.ThreadSpec defaultValue);
  /**
   * <code>map&lt;string, .littlehorse.ThreadSpec&gt; thread_specs = 5;</code>
   */
  io.littlehorse.sdk.common.proto.ThreadSpec getThreadSpecsOrThrow(
      java.lang.String key);

  /**
   * <code>string entrypoint_thread_name = 6;</code>
   * @return The entrypointThreadName.
   */
  java.lang.String getEntrypointThreadName();
  /**
   * <code>string entrypoint_thread_name = 6;</code>
   * @return The bytes for entrypointThreadName.
   */
  com.google.protobuf.ByteString
      getEntrypointThreadNameBytes();

  /**
   * <code>optional int32 retention_hours = 7;</code>
   * @return Whether the retentionHours field is set.
   */
  boolean hasRetentionHours();
  /**
   * <code>optional int32 retention_hours = 7;</code>
   * @return The retentionHours.
   */
  int getRetentionHours();
}

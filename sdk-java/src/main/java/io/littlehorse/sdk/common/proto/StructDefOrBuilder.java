// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: struct_def.proto

package io.littlehorse.sdk.common.proto;

public interface StructDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.StructDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The id of the `Schema`. This includes the version.
   * </pre>
   *
   * <code>.littlehorse.StructDefId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The id of the `Schema`. This includes the version.
   * </pre>
   *
   * <code>.littlehorse.StructDefId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.StructDefId getId();
  /**
   * <pre>
   * The id of the `Schema`. This includes the version.
   * </pre>
   *
   * <code>.littlehorse.StructDefId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.StructDefIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * Optionally description of the schema.
   * </pre>
   *
   * <code>optional string description = 2;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <pre>
   * Optionally description of the schema.
   * </pre>
   *
   * <code>optional string description = 2;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <pre>
   * Optionally description of the schema.
   * </pre>
   *
   * <code>optional string description = 2;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <pre>
   * When the StructDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * When the StructDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * When the StructDef was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <pre>
   * The `StructDef` defines the actual structure of any `Struct` using this `InlineStructDeff`.
   * </pre>
   *
   * <code>.littlehorse.InlineStructDef struct_def = 4;</code>
   * @return Whether the structDef field is set.
   */
  boolean hasStructDef();
  /**
   * <pre>
   * The `StructDef` defines the actual structure of any `Struct` using this `InlineStructDeff`.
   * </pre>
   *
   * <code>.littlehorse.InlineStructDef struct_def = 4;</code>
   * @return The structDef.
   */
  io.littlehorse.sdk.common.proto.InlineStructDef getStructDef();
  /**
   * <pre>
   * The `StructDef` defines the actual structure of any `Struct` using this `InlineStructDeff`.
   * </pre>
   *
   * <code>.littlehorse.InlineStructDef struct_def = 4;</code>
   */
  io.littlehorse.sdk.common.proto.InlineStructDefOrBuilder getStructDefOrBuilder();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

public interface StoredGetablePbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.StoredGetablePb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.TagsCachePb index_cache = 1;</code>
   * @return Whether the indexCache field is set.
   */
  boolean hasIndexCache();
  /**
   * <code>.littlehorse.TagsCachePb index_cache = 1;</code>
   * @return The indexCache.
   */
  io.littlehorse.common.proto.TagsCachePb getIndexCache();
  /**
   * <code>.littlehorse.TagsCachePb index_cache = 1;</code>
   */
  io.littlehorse.common.proto.TagsCachePbOrBuilder getIndexCacheOrBuilder();

  /**
   * <code>bytes getable_payload = 2;</code>
   * @return The getablePayload.
   */
  com.google.protobuf.ByteString getGetablePayload();

  /**
   * <code>.littlehorse.GetableClassEnum type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.littlehorse.GetableClassEnum type = 3;</code>
   * @return The type.
   */
  io.littlehorse.common.proto.GetableClassEnum getType();
}

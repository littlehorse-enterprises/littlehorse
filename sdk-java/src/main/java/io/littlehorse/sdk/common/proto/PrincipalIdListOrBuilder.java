// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface PrincipalIdListOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PrincipalIdList)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The resulting object id's.
   * </pre>
   *
   * <code>repeated .littlehorse.PrincipalId results = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.PrincipalId> 
      getResultsList();
  /**
   * <pre>
   * The resulting object id's.
   * </pre>
   *
   * <code>repeated .littlehorse.PrincipalId results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.PrincipalId getResults(int index);
  /**
   * <pre>
   * The resulting object id's.
   * </pre>
   *
   * <code>repeated .littlehorse.PrincipalId results = 1;</code>
   */
  int getResultsCount();
  /**
   * <pre>
   * The resulting object id's.
   * </pre>
   *
   * <code>repeated .littlehorse.PrincipalId results = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.PrincipalIdOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <pre>
   * The resulting object id's.
   * </pre>
   *
   * <code>repeated .littlehorse.PrincipalId results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.PrincipalIdOrBuilder getResultsOrBuilder(
      int index);

  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 2;</code>
   * @return Whether the bookmark field is set.
   */
  boolean hasBookmark();
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 2;</code>
   * @return The bookmark.
   */
  com.google.protobuf.ByteString getBookmark();
}

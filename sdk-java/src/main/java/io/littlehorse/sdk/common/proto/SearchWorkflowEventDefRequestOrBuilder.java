// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface SearchWorkflowEventDefRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchWorkflowEventDefRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 1;</code>
   * @return Whether the bookmark field is set.
   */
  boolean hasBookmark();
  /**
   * <pre>
   * Bookmark for cursor-based pagination; pass if applicable.
   * </pre>
   *
   * <code>optional bytes bookmark = 1;</code>
   * @return The bookmark.
   */
  com.google.protobuf.ByteString getBookmark();

  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 2;</code>
   * @return Whether the limit field is set.
   */
  boolean hasLimit();
  /**
   * <pre>
   * Maximum results to return in one request.
   * </pre>
   *
   * <code>optional int32 limit = 2;</code>
   * @return The limit.
   */
  int getLimit();

  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return Whether the prefix field is set.
   */
  boolean hasPrefix();
  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return The prefix.
   */
  java.lang.String getPrefix();
  /**
   * <pre>
   * Optionally search only for WorkflowEventDef's whose name starts with this prefix.
   * </pre>
   *
   * <code>optional string prefix = 3;</code>
   * @return The bytes for prefix.
   */
  com.google.protobuf.ByteString
      getPrefixBytes();
}

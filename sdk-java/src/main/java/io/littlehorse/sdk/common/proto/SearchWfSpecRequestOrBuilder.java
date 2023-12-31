// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SearchWfSpecRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchWfSpecRequest)
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
   * Return WfSpec's with a specific name.
   * </pre>
   *
   * <code>string name = 3;</code>
   * @return Whether the name field is set.
   */
  boolean hasName();
  /**
   * <pre>
   * Return WfSpec's with a specific name.
   * </pre>
   *
   * <code>string name = 3;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * Return WfSpec's with a specific name.
   * </pre>
   *
   * <code>string name = 3;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * Return WfSpec's with a specific prefix.
   * </pre>
   *
   * <code>string prefix = 4;</code>
   * @return Whether the prefix field is set.
   */
  boolean hasPrefix();
  /**
   * <pre>
   * Return WfSpec's with a specific prefix.
   * </pre>
   *
   * <code>string prefix = 4;</code>
   * @return The prefix.
   */
  java.lang.String getPrefix();
  /**
   * <pre>
   * Return WfSpec's with a specific prefix.
   * </pre>
   *
   * <code>string prefix = 4;</code>
   * @return The bytes for prefix.
   */
  com.google.protobuf.ByteString
      getPrefixBytes();

  /**
   * <pre>
   * Return all WfSpec's that make use of a given TaskDef.
   * </pre>
   *
   * <code>string task_def_name = 5;</code>
   * @return Whether the taskDefName field is set.
   */
  boolean hasTaskDefName();
  /**
   * <pre>
   * Return all WfSpec's that make use of a given TaskDef.
   * </pre>
   *
   * <code>string task_def_name = 5;</code>
   * @return The taskDefName.
   */
  java.lang.String getTaskDefName();
  /**
   * <pre>
   * Return all WfSpec's that make use of a given TaskDef.
   * </pre>
   *
   * <code>string task_def_name = 5;</code>
   * @return The bytes for taskDefName.
   */
  com.google.protobuf.ByteString
      getTaskDefNameBytes();

  io.littlehorse.sdk.common.proto.SearchWfSpecRequest.WfSpecCriteriaCase getWfSpecCriteriaCase();
}

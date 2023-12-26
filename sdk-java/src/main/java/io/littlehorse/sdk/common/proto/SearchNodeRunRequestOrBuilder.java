// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SearchNodeRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchNodeRunRequest)
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
   * Only return NodeRun's created after this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return Whether the earliestStart field is set.
   */
  boolean hasEarliestStart();
  /**
   * <pre>
   * Only return NodeRun's created after this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return The earliestStart.
   */
  com.google.protobuf.Timestamp getEarliestStart();
  /**
   * <pre>
   * Only return NodeRun's created after this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEarliestStartOrBuilder();

  /**
   * <pre>
   * Only return NodeRun's created before this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return Whether the latestStart field is set.
   */
  boolean hasLatestStart();
  /**
   * <pre>
   * Only return NodeRun's created before this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return The latestStart.
   */
  com.google.protobuf.Timestamp getLatestStart();
  /**
   * <pre>
   * Only return NodeRun's created before this time.
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestStartOrBuilder();

  /**
   * <pre>
   * Specifies the type of NodeRun to search for.
   * </pre>
   *
   * <code>.littlehorse.SearchNodeRunRequest.NodeType node_type = 5;</code>
   * @return The enum numeric value on the wire for nodeType.
   */
  int getNodeTypeValue();
  /**
   * <pre>
   * Specifies the type of NodeRun to search for.
   * </pre>
   *
   * <code>.littlehorse.SearchNodeRunRequest.NodeType node_type = 5;</code>
   * @return The nodeType.
   */
  io.littlehorse.sdk.common.proto.SearchNodeRunRequest.NodeType getNodeType();

  /**
   * <pre>
   * Specifies the status of NodeRun to search for.
   * </pre>
   *
   * <code>.littlehorse.LHStatus status = 6;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <pre>
   * Specifies the status of NodeRun to search for.
   * </pre>
   *
   * <code>.littlehorse.LHStatus status = 6;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.LHStatus getStatus();
}

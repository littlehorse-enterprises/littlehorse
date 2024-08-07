// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SearchExternalEventRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchExternalEventRequest)
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
   * Specifies to return only ExternalEvents created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return Whether the earliestStart field is set.
   */
  boolean hasEarliestStart();
  /**
   * <pre>
   * Specifies to return only ExternalEvents created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return The earliestStart.
   */
  com.google.protobuf.Timestamp getEarliestStart();
  /**
   * <pre>
   * Specifies to return only ExternalEvents created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEarliestStartOrBuilder();

  /**
   * <pre>
   * Specifies to return only ExternalEvents created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return Whether the latestStart field is set.
   */
  boolean hasLatestStart();
  /**
   * <pre>
   * Specifies to return only ExternalEvents created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return The latestStart.
   */
  com.google.protobuf.Timestamp getLatestStart();
  /**
   * <pre>
   * Specifies to return only ExternalEvents created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestStartOrBuilder();

  /**
   * <pre>
   * Search for ExternalEvents by their ExternalEventDefId
   *
   * * Note: If ExternalEventDefId is not provided or does not exist,
   *         gRPC status code 'INVALID_ARGUMENT' will be returned.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 5;</code>
   * @return Whether the externalEventDefId field is set.
   */
  boolean hasExternalEventDefId();
  /**
   * <pre>
   * Search for ExternalEvents by their ExternalEventDefId
   *
   * * Note: If ExternalEventDefId is not provided or does not exist,
   *         gRPC status code 'INVALID_ARGUMENT' will be returned.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 5;</code>
   * @return The externalEventDefId.
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getExternalEventDefId();
  /**
   * <pre>
   * Search for ExternalEvents by their ExternalEventDefId
   *
   * * Note: If ExternalEventDefId is not provided or does not exist,
   *         gRPC status code 'INVALID_ARGUMENT' will be returned.
   * </pre>
   *
   * <code>.littlehorse.ExternalEventDefId external_event_def_id = 5;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getExternalEventDefIdOrBuilder();

  /**
   * <pre>
   * Optionally search for only ExternalEvents that are claimed or not.
   * </pre>
   *
   * <code>optional bool is_claimed = 6;</code>
   * @return Whether the isClaimed field is set.
   */
  boolean hasIsClaimed();
  /**
   * <pre>
   * Optionally search for only ExternalEvents that are claimed or not.
   * </pre>
   *
   * <code>optional bool is_claimed = 6;</code>
   * @return The isClaimed.
   */
  boolean getIsClaimed();
}

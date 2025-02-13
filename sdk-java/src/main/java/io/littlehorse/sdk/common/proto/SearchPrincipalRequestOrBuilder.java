// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface SearchPrincipalRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchPrincipalRequest)
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
   * Specifies to return only Principals's created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return Whether the earliestStart field is set.
   */
  boolean hasEarliestStart();
  /**
   * <pre>
   * Specifies to return only Principals's created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   * @return The earliestStart.
   */
  com.google.protobuf.Timestamp getEarliestStart();
  /**
   * <pre>
   * Specifies to return only Principals's created after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 3;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEarliestStartOrBuilder();

  /**
   * <pre>
   * Specifies to return only Principals's created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return Whether the latestStart field is set.
   */
  boolean hasLatestStart();
  /**
   * <pre>
   * Specifies to return only Principals's created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   * @return The latestStart.
   */
  com.google.protobuf.Timestamp getLatestStart();
  /**
   * <pre>
   * Specifies to return only Principals's created before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 4;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestStartOrBuilder();

  /**
   * <pre>
   * List only Principals that are admins
   * </pre>
   *
   * <code>bool isAdmin = 5;</code>
   * @return Whether the isAdmin field is set.
   */
  boolean hasIsAdmin();
  /**
   * <pre>
   * List only Principals that are admins
   * </pre>
   *
   * <code>bool isAdmin = 5;</code>
   * @return The isAdmin.
   */
  boolean getIsAdmin();

  /**
   * <pre>
   * List Principals associated with this Tenant ID
   * </pre>
   *
   * <code>string tenantId = 6;</code>
   * @return Whether the tenantId field is set.
   */
  boolean hasTenantId();
  /**
   * <pre>
   * List Principals associated with this Tenant ID
   * </pre>
   *
   * <code>string tenantId = 6;</code>
   * @return The tenantId.
   */
  java.lang.String getTenantId();
  /**
   * <pre>
   * List Principals associated with this Tenant ID
   * </pre>
   *
   * <code>string tenantId = 6;</code>
   * @return The bytes for tenantId.
   */
  com.google.protobuf.ByteString
      getTenantIdBytes();

  io.littlehorse.sdk.common.proto.SearchPrincipalRequest.PrincipalCriteriaCase getPrincipalCriteriaCase();
}

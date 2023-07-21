// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

public interface InternalScanPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.InternalScanPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.ScanResultTypePb result_type = 1;</code>
   * @return The enum numeric value on the wire for resultType.
   */
  int getResultTypeValue();
  /**
   * <code>.littlehorse.ScanResultTypePb result_type = 1;</code>
   * @return The resultType.
   */
  io.littlehorse.common.proto.ScanResultTypePb getResultType();

  /**
   * <code>int32 limit = 2;</code>
   * @return The limit.
   */
  int getLimit();

  /**
   * <code>optional .littlehorse.BookmarkPb bookmark = 3;</code>
   * @return Whether the bookmark field is set.
   */
  boolean hasBookmark();
  /**
   * <code>optional .littlehorse.BookmarkPb bookmark = 3;</code>
   * @return The bookmark.
   */
  io.littlehorse.common.proto.BookmarkPb getBookmark();
  /**
   * <code>optional .littlehorse.BookmarkPb bookmark = 3;</code>
   */
  io.littlehorse.common.proto.BookmarkPbOrBuilder getBookmarkOrBuilder();

  /**
   * <code>.littlehorse.GetableClassEnumPb object_type = 4;</code>
   * @return The enum numeric value on the wire for objectType.
   */
  int getObjectTypeValue();
  /**
   * <code>.littlehorse.GetableClassEnumPb object_type = 4;</code>
   * @return The objectType.
   */
  io.littlehorse.common.proto.GetableClassEnumPb getObjectType();

  /**
   * <code>string store_name = 5;</code>
   * @return The storeName.
   */
  java.lang.String getStoreName();
  /**
   * <code>string store_name = 5;</code>
   * @return The bytes for storeName.
   */
  com.google.protobuf.ByteString
      getStoreNameBytes();

  /**
   * <pre>
   * If this is set: Remote Tag Scan
   * Else: Local Tag Scan
   * </pre>
   *
   * <code>optional string partition_key = 6;</code>
   * @return Whether the partitionKey field is set.
   */
  boolean hasPartitionKey();
  /**
   * <pre>
   * If this is set: Remote Tag Scan
   * Else: Local Tag Scan
   * </pre>
   *
   * <code>optional string partition_key = 6;</code>
   * @return The partitionKey.
   */
  java.lang.String getPartitionKey();
  /**
   * <pre>
   * If this is set: Remote Tag Scan
   * Else: Local Tag Scan
   * </pre>
   *
   * <code>optional string partition_key = 6;</code>
   * @return The bytes for partitionKey.
   */
  com.google.protobuf.ByteString
      getPartitionKeyBytes();

  /**
   * <code>.littlehorse.InternalScanPb.BoundedObjectIdScanPb bounded_object_id_scan = 7;</code>
   * @return Whether the boundedObjectIdScan field is set.
   */
  boolean hasBoundedObjectIdScan();
  /**
   * <code>.littlehorse.InternalScanPb.BoundedObjectIdScanPb bounded_object_id_scan = 7;</code>
   * @return The boundedObjectIdScan.
   */
  io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPb getBoundedObjectIdScan();
  /**
   * <code>.littlehorse.InternalScanPb.BoundedObjectIdScanPb bounded_object_id_scan = 7;</code>
   */
  io.littlehorse.common.proto.InternalScanPb.BoundedObjectIdScanPbOrBuilder getBoundedObjectIdScanOrBuilder();

  /**
   * <code>.littlehorse.InternalScanPb.TagScanPb tag_scan = 8;</code>
   * @return Whether the tagScan field is set.
   */
  boolean hasTagScan();
  /**
   * <code>.littlehorse.InternalScanPb.TagScanPb tag_scan = 8;</code>
   * @return The tagScan.
   */
  io.littlehorse.common.proto.InternalScanPb.TagScanPb getTagScan();
  /**
   * <code>.littlehorse.InternalScanPb.TagScanPb tag_scan = 8;</code>
   */
  io.littlehorse.common.proto.InternalScanPb.TagScanPbOrBuilder getTagScanOrBuilder();

  public io.littlehorse.common.proto.InternalScanPb.ScanBoundaryCase getScanBoundaryCase();
}

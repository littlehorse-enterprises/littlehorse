// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: interactive_query.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

public interface BookmarkPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.BookmarkPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  int getInProgressPartitionsCount();
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  boolean containsInProgressPartitions(
      int key);
  /**
   * Use {@link #getInProgressPartitionsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
  getInProgressPartitions();
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
  getInProgressPartitionsMap();
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrDefault(
      int key,
      /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb defaultValue);
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrThrow(
      int key);

  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @return A list containing the completedPartitions.
   */
  java.util.List<java.lang.Integer> getCompletedPartitionsList();
  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @return The count of completedPartitions.
   */
  int getCompletedPartitionsCount();
  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @param index The index of the element to return.
   * @return The completedPartitions at the given index.
   */
  int getCompletedPartitions(int index);
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: interactive_query.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.common.proto;

public interface InternalScanResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.InternalScanResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated bytes results = 1;</code>
   * @return A list containing the results.
   */
  java.util.List<com.google.protobuf.ByteString> getResultsList();
  /**
   * <code>repeated bytes results = 1;</code>
   * @return The count of results.
   */
  int getResultsCount();
  /**
   * <code>repeated bytes results = 1;</code>
   * @param index The index of the element to return.
   * @return The results at the given index.
   */
  com.google.protobuf.ByteString getResults(int index);

  /**
   * <code>.littlehorse.BookmarkPb updated_bookmark = 2;</code>
   * @return Whether the updatedBookmark field is set.
   */
  boolean hasUpdatedBookmark();
  /**
   * <code>.littlehorse.BookmarkPb updated_bookmark = 2;</code>
   * @return The updatedBookmark.
   */
  io.littlehorse.common.proto.BookmarkPb getUpdatedBookmark();
  /**
   * <code>.littlehorse.BookmarkPb updated_bookmark = 2;</code>
   */
  io.littlehorse.common.proto.BookmarkPbOrBuilder getUpdatedBookmarkOrBuilder();
}

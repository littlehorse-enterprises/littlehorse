// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface ExternalEventDefIdListOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ExternalEventDefIdList)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .littlehorse.ExternalEventDefId results = 1;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.ExternalEventDefId> 
      getResultsList();
  /**
   * <code>repeated .littlehorse.ExternalEventDefId results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefId getResults(int index);
  /**
   * <code>repeated .littlehorse.ExternalEventDefId results = 1;</code>
   */
  int getResultsCount();
  /**
   * <code>repeated .littlehorse.ExternalEventDefId results = 1;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <code>repeated .littlehorse.ExternalEventDefId results = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ExternalEventDefIdOrBuilder getResultsOrBuilder(
      int index);

  /**
   * <code>optional bytes bookmark = 2;</code>
   * @return Whether the bookmark field is set.
   */
  boolean hasBookmark();
  /**
   * <code>optional bytes bookmark = 2;</code>
   * @return The bookmark.
   */
  com.google.protobuf.ByteString getBookmark();
}

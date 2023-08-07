// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SearchUserTaskRunPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchUserTaskRunPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional bytes bookmark = 1;</code>
   * @return Whether the bookmark field is set.
   */
  boolean hasBookmark();
  /**
   * <code>optional bytes bookmark = 1;</code>
   * @return The bookmark.
   */
  com.google.protobuf.ByteString getBookmark();

  /**
   * <code>optional int32 limit = 2;</code>
   * @return Whether the limit field is set.
   */
  boolean hasLimit();
  /**
   * <code>optional int32 limit = 2;</code>
   * @return The limit.
   */
  int getLimit();

  /**
   * <code>optional .littlehorse.UserTaskRunStatusPb status = 3;</code>
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   * <code>optional .littlehorse.UserTaskRunStatusPb status = 3;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <code>optional .littlehorse.UserTaskRunStatusPb status = 3;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunStatusPb getStatus();

  /**
   * <code>optional string user_task_def_name = 4;</code>
   * @return Whether the userTaskDefName field is set.
   */
  boolean hasUserTaskDefName();
  /**
   * <code>optional string user_task_def_name = 4;</code>
   * @return The userTaskDefName.
   */
  java.lang.String getUserTaskDefName();
  /**
   * <code>optional string user_task_def_name = 4;</code>
   * @return The bytes for userTaskDefName.
   */
  com.google.protobuf.ByteString
      getUserTaskDefNameBytes();

  /**
   * <code>string user_id = 5;</code>
   * @return Whether the userId field is set.
   */
  boolean hasUserId();
  /**
   * <code>string user_id = 5;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <code>string user_id = 5;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <code>string user_group = 6;</code>
   * @return Whether the userGroup field is set.
   */
  boolean hasUserGroup();
  /**
   * <code>string user_group = 6;</code>
   * @return The userGroup.
   */
  java.lang.String getUserGroup();
  /**
   * <code>string user_group = 6;</code>
   * @return The bytes for userGroup.
   */
  com.google.protobuf.ByteString
      getUserGroupBytes();

  /**
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   * @return Whether the earliestStart field is set.
   */
  boolean hasEarliestStart();
  /**
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   * @return The earliestStart.
   */
  com.google.protobuf.Timestamp getEarliestStart();
  /**
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEarliestStartOrBuilder();

  /**
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   * @return Whether the latestStart field is set.
   */
  boolean hasLatestStart();
  /**
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   * @return The latestStart.
   */
  com.google.protobuf.Timestamp getLatestStart();
  /**
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestStartOrBuilder();

  io.littlehorse.sdk.common.proto.SearchUserTaskRunPb.TaskOwnerCase getTaskOwnerCase();
}

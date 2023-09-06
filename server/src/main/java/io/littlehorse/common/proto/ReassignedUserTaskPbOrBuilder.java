// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface ReassignedUserTaskPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ReassignedUserTaskPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string user_id = 1;</code>
   * @return Whether the userId field is set.
   */
  boolean hasUserId();
  /**
   * <code>string user_id = 1;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <code>string user_id = 1;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <code>string user_group = 2;</code>
   * @return Whether the userGroup field is set.
   */
  boolean hasUserGroup();
  /**
   * <code>string user_group = 2;</code>
   * @return The userGroup.
   */
  java.lang.String getUserGroup();
  /**
   * <code>string user_group = 2;</code>
   * @return The bytes for userGroup.
   */
  com.google.protobuf.ByteString
      getUserGroupBytes();

  /**
   * <code>int32 delay_in_seconds = 3;</code>
   * @return The delayInSeconds.
   */
  int getDelayInSeconds();

  /**
   * <code>.littlehorse.NodeRunId source = 4;</code>
   * @return Whether the source field is set.
   */
  boolean hasSource();
  /**
   * <code>.littlehorse.NodeRunId source = 4;</code>
   * @return The source.
   */
  io.littlehorse.sdk.common.proto.NodeRunId getSource();
  /**
   * <code>.littlehorse.NodeRunId source = 4;</code>
   */
  io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getSourceOrBuilder();

  public io.littlehorse.common.proto.ReassignedUserTaskPb.AssignToCase getAssignToCase();
}

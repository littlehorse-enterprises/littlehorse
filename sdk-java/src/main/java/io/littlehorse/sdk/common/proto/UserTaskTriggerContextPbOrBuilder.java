// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskTriggerContextPbOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskTriggerContextPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.UserGroupPb user_group = 1;</code>
   * @return Whether the userGroup field is set.
   */
  boolean hasUserGroup();
  /**
   * <code>.littlehorse.UserGroupPb user_group = 1;</code>
   * @return The userGroup.
   */
  io.littlehorse.sdk.common.proto.UserGroupPb getUserGroup();
  /**
   * <code>.littlehorse.UserGroupPb user_group = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserGroupPbOrBuilder getUserGroupOrBuilder();

  /**
   * <code>.littlehorse.UserPb user = 2;</code>
   * @return Whether the user field is set.
   */
  boolean hasUser();
  /**
   * <code>.littlehorse.UserPb user = 2;</code>
   * @return The user.
   */
  io.littlehorse.sdk.common.proto.UserPb getUser();
  /**
   * <code>.littlehorse.UserPb user = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserPbOrBuilder getUserOrBuilder();

  public io.littlehorse.sdk.common.proto.UserTaskTriggerContextPb.OwnerCase getOwnerCase();
}

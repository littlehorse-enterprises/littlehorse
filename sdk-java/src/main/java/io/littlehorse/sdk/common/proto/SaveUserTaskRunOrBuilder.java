// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SaveUserTaskRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SaveUserTaskRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.UserTaskResult result = 1;</code>
   * @return Whether the result field is set.
   */
  boolean hasResult();
  /**
   * <code>.littlehorse.UserTaskResult result = 1;</code>
   * @return The result.
   */
  io.littlehorse.sdk.common.proto.UserTaskResult getResult();
  /**
   * <code>.littlehorse.UserTaskResult result = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskResultOrBuilder getResultOrBuilder();

  /**
   * <code>string user_id = 2;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <code>string user_id = 2;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <code>repeated .littlehorse.UserTaskFieldResult results = 3;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.UserTaskFieldResult> 
      getResultsList();
  /**
   * <code>repeated .littlehorse.UserTaskFieldResult results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskFieldResult getResults(int index);
  /**
   * <code>repeated .littlehorse.UserTaskFieldResult results = 3;</code>
   */
  int getResultsCount();
  /**
   * <code>repeated .littlehorse.UserTaskFieldResult results = 3;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.UserTaskFieldResultOrBuilder> 
      getResultsOrBuilderList();
  /**
   * <code>repeated .littlehorse.UserTaskFieldResult results = 3;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskFieldResultOrBuilder getResultsOrBuilder(
      int index);
}

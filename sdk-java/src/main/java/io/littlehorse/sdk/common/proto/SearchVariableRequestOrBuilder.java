// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface SearchVariableRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchVariableRequest)
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
   * <code>string wf_run_id = 3;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <code>string wf_run_id = 3;</code>
   * @return The wfRunId.
   */
  java.lang.String getWfRunId();
  /**
   * <code>string wf_run_id = 3;</code>
   * @return The bytes for wfRunId.
   */
  com.google.protobuf.ByteString
      getWfRunIdBytes();

  /**
   * <code>.littlehorse.SearchVariableRequest.NameAndValueRequest value = 4;</code>
   * @return Whether the value field is set.
   */
  boolean hasValue();
  /**
   * <code>.littlehorse.SearchVariableRequest.NameAndValueRequest value = 4;</code>
   * @return The value.
   */
  io.littlehorse.sdk.common.proto.SearchVariableRequest.NameAndValueRequest getValue();
  /**
   * <code>.littlehorse.SearchVariableRequest.NameAndValueRequest value = 4;</code>
   */
  io.littlehorse.sdk.common.proto.SearchVariableRequest.NameAndValueRequestOrBuilder getValueOrBuilder();

  io.littlehorse.sdk.common.proto.SearchVariableRequest.VariableCriteriaCase getVariableCriteriaCase();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface SearchWfRunRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.SearchWfRunRequest)
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
   * The WfSpec whose WfRun's we are searching for. This is required.
   * </pre>
   *
   * <code>string wf_spec_name = 3;</code>
   * @return The wfSpecName.
   */
  java.lang.String getWfSpecName();
  /**
   * <pre>
   * The WfSpec whose WfRun's we are searching for. This is required.
   * </pre>
   *
   * <code>string wf_spec_name = 3;</code>
   * @return The bytes for wfSpecName.
   */
  com.google.protobuf.ByteString
      getWfSpecNameBytes();

  /**
   * <pre>
   * Specifies to return only WfRun's from this WfSpec Major Version.
   * </pre>
   *
   * <code>optional int32 wf_spec_major_version = 4;</code>
   * @return Whether the wfSpecMajorVersion field is set.
   */
  boolean hasWfSpecMajorVersion();
  /**
   * <pre>
   * Specifies to return only WfRun's from this WfSpec Major Version.
   * </pre>
   *
   * <code>optional int32 wf_spec_major_version = 4;</code>
   * @return The wfSpecMajorVersion.
   */
  int getWfSpecMajorVersion();

  /**
   * <pre>
   * Specifies to return only WfRun's from this WfSpec Revision. Can only be set if
   * wf_spec_major_version is also set.
   * </pre>
   *
   * <code>optional int32 wf_spec_revision = 5;</code>
   * @return Whether the wfSpecRevision field is set.
   */
  boolean hasWfSpecRevision();
  /**
   * <pre>
   * Specifies to return only WfRun's from this WfSpec Revision. Can only be set if
   * wf_spec_major_version is also set.
   * </pre>
   *
   * <code>optional int32 wf_spec_revision = 5;</code>
   * @return The wfSpecRevision.
   */
  int getWfSpecRevision();

  /**
   * <pre>
   * Specifies to return only WfRun's matching this status.
   * </pre>
   *
   * <code>optional .littlehorse.LHStatus status = 6;</code>
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   * <pre>
   * Specifies to return only WfRun's matching this status.
   * </pre>
   *
   * <code>optional .littlehorse.LHStatus status = 6;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <pre>
   * Specifies to return only WfRun's matching this status.
   * </pre>
   *
   * <code>optional .littlehorse.LHStatus status = 6;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.LHStatus getStatus();

  /**
   * <pre>
   * Specifies to return only WfRun's that started after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   * @return Whether the earliestStart field is set.
   */
  boolean hasEarliestStart();
  /**
   * <pre>
   * Specifies to return only WfRun's that started after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   * @return The earliestStart.
   */
  com.google.protobuf.Timestamp getEarliestStart();
  /**
   * <pre>
   * Specifies to return only WfRun's that started after this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp earliest_start = 7;</code>
   */
  com.google.protobuf.TimestampOrBuilder getEarliestStartOrBuilder();

  /**
   * <pre>
   * Specifies to return only WfRun's that started before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   * @return Whether the latestStart field is set.
   */
  boolean hasLatestStart();
  /**
   * <pre>
   * Specifies to return only WfRun's that started before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   * @return The latestStart.
   */
  com.google.protobuf.Timestamp getLatestStart();
  /**
   * <pre>
   * Specifies to return only WfRun's that started before this time
   * </pre>
   *
   * <code>optional .google.protobuf.Timestamp latest_start = 8;</code>
   */
  com.google.protobuf.TimestampOrBuilder getLatestStartOrBuilder();

  /**
   * <pre>
   * Allows filtering WfRun's based on the value of the Variables. This ONLY
   * works for the Variables in the entrypiont threadrun (that is, variables
   * where the threadRunNumber == 0).
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMatch variable_filters = 9;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.VariableMatch> 
      getVariableFiltersList();
  /**
   * <pre>
   * Allows filtering WfRun's based on the value of the Variables. This ONLY
   * works for the Variables in the entrypiont threadrun (that is, variables
   * where the threadRunNumber == 0).
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMatch variable_filters = 9;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMatch getVariableFilters(int index);
  /**
   * <pre>
   * Allows filtering WfRun's based on the value of the Variables. This ONLY
   * works for the Variables in the entrypiont threadrun (that is, variables
   * where the threadRunNumber == 0).
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMatch variable_filters = 9;</code>
   */
  int getVariableFiltersCount();
  /**
   * <pre>
   * Allows filtering WfRun's based on the value of the Variables. This ONLY
   * works for the Variables in the entrypiont threadrun (that is, variables
   * where the threadRunNumber == 0).
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMatch variable_filters = 9;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.VariableMatchOrBuilder> 
      getVariableFiltersOrBuilderList();
  /**
   * <pre>
   * Allows filtering WfRun's based on the value of the Variables. This ONLY
   * works for the Variables in the entrypiont threadrun (that is, variables
   * where the threadRunNumber == 0).
   * </pre>
   *
   * <code>repeated .littlehorse.VariableMatch variable_filters = 9;</code>
   */
  io.littlehorse.sdk.common.proto.VariableMatchOrBuilder getVariableFiltersOrBuilder(
      int index);
}

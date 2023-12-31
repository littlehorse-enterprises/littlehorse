// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

public interface RunWfRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.RunWfRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the WfSpec to run.
   * </pre>
   *
   * <code>string wf_spec_name = 1;</code>
   * @return The wfSpecName.
   */
  java.lang.String getWfSpecName();
  /**
   * <pre>
   * The name of the WfSpec to run.
   * </pre>
   *
   * <code>string wf_spec_name = 1;</code>
   * @return The bytes for wfSpecName.
   */
  com.google.protobuf.ByteString
      getWfSpecNameBytes();

  /**
   * <pre>
   * Optionally specify the major version of the WfSpec to run. This guarantees that
   * the "signature" of the WfSpec (i.e. the required input variables, and searchable
   * variables) will not change for this app.
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return Whether the majorVersion field is set.
   */
  boolean hasMajorVersion();
  /**
   * <pre>
   * Optionally specify the major version of the WfSpec to run. This guarantees that
   * the "signature" of the WfSpec (i.e. the required input variables, and searchable
   * variables) will not change for this app.
   * </pre>
   *
   * <code>optional int32 major_version = 2;</code>
   * @return The majorVersion.
   */
  int getMajorVersion();

  /**
   * <pre>
   * Optionally specify the specific revision of the WfSpec to run. It is not recommended
   * to use this in practice, as the WfSpec logic should be de-coupled from the applications
   * that run WfRun's.
   * </pre>
   *
   * <code>optional int32 revision = 3;</code>
   * @return Whether the revision field is set.
   */
  boolean hasRevision();
  /**
   * <pre>
   * Optionally specify the specific revision of the WfSpec to run. It is not recommended
   * to use this in practice, as the WfSpec logic should be de-coupled from the applications
   * that run WfRun's.
   * </pre>
   *
   * <code>optional int32 revision = 3;</code>
   * @return The revision.
   */
  int getRevision();

  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 4;</code>
   */
  int getVariablesCount();
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 4;</code>
   */
  boolean containsVariables(
      java.lang.String key);
  /**
   * Use {@link #getVariablesMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getVariables();
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 4;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getVariablesMap();
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 4;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.VariableValue getVariablesOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.VariableValue defaultValue);
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 4;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValue getVariablesOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * You can optionally specify the ID of this WfRun. This is a recommended best practice
   * as it also makes your request idempotent and allows you to easily find the WfRun at
   * a later time.
   * </pre>
   *
   * <code>optional string id = 5;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * You can optionally specify the ID of this WfRun. This is a recommended best practice
   * as it also makes your request idempotent and allows you to easily find the WfRun at
   * a later time.
   * </pre>
   *
   * <code>optional string id = 5;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <pre>
   * You can optionally specify the ID of this WfRun. This is a recommended best practice
   * as it also makes your request idempotent and allows you to easily find the WfRun at
   * a later time.
   * </pre>
   *
   * <code>optional string id = 5;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   * @return Whether the parentWfRunId field is set.
   */
  boolean hasParentWfRunId();
  /**
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   * @return The parentWfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getParentWfRunId();
  /**
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getParentWfRunIdOrBuilder();
}

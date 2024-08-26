// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface ScheduleWfRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ScheduleWfRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.ScheduledWfRunId scheduled_id = 1;</code>
   * @return Whether the scheduledId field is set.
   */
  boolean hasScheduledId();
  /**
   * <code>.littlehorse.ScheduledWfRunId scheduled_id = 1;</code>
   * @return The scheduledId.
   */
  io.littlehorse.sdk.common.proto.ScheduledWfRunId getScheduledId();
  /**
   * <code>.littlehorse.ScheduledWfRunId scheduled_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.ScheduledWfRunIdOrBuilder getScheduledIdOrBuilder();

  /**
   * <pre>
   * The name of the WfSpec to run.
   * </pre>
   *
   * <code>string wf_spec_name = 2;</code>
   * @return The wfSpecName.
   */
  java.lang.String getWfSpecName();
  /**
   * <pre>
   * The name of the WfSpec to run.
   * </pre>
   *
   * <code>string wf_spec_name = 2;</code>
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
   * <code>optional int32 major_version = 3;</code>
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
   * <code>optional int32 major_version = 3;</code>
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
   * <code>optional int32 revision = 4;</code>
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
   * <code>optional int32 revision = 4;</code>
   * @return The revision.
   */
  int getRevision();

  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 5;</code>
   */
  int getVariablesCount();
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 5;</code>
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
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 5;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getVariablesMap();
  /**
   * <pre>
   * A map from Variable Name to Values for those variables. The provided variables are
   * passed as input to the Entrypoint ThreadRun.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 5;</code>
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
   * <code>map&lt;string, .littlehorse.VariableValue&gt; variables = 5;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValue getVariablesOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * Parent WfRunId associated with all the generated WfRuns
   * </pre>
   *
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   * @return Whether the parentWfRunId field is set.
   */
  boolean hasParentWfRunId();
  /**
   * <pre>
   * Parent WfRunId associated with all the generated WfRuns
   * </pre>
   *
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   * @return The parentWfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getParentWfRunId();
  /**
   * <pre>
   * Parent WfRunId associated with all the generated WfRuns
   * </pre>
   *
   * <code>optional .littlehorse.WfRunId parent_wf_run_id = 6;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getParentWfRunIdOrBuilder();

  /**
   * <pre>
   * UNIX expression used to specify the schedule for executing WfRuns
   * </pre>
   *
   * <code>string cron_expression = 7;</code>
   * @return The cronExpression.
   */
  java.lang.String getCronExpression();
  /**
   * <pre>
   * UNIX expression used to specify the schedule for executing WfRuns
   * </pre>
   *
   * <code>string cron_expression = 7;</code>
   * @return The bytes for cronExpression.
   */
  com.google.protobuf.ByteString
      getCronExpressionBytes();
}

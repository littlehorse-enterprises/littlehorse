// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: object_id.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface WorkflowEventIdOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.WorkflowEventId)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The Id of the WfRun that threw the event.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return Whether the wfRunId field is set.
   */
  boolean hasWfRunId();
  /**
   * <pre>
   * The Id of the WfRun that threw the event.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   * @return The wfRunId.
   */
  io.littlehorse.sdk.common.proto.WfRunId getWfRunId();
  /**
   * <pre>
   * The Id of the WfRun that threw the event.
   * </pre>
   *
   * <code>.littlehorse.WfRunId wf_run_id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfRunIdOrBuilder getWfRunIdOrBuilder();

  /**
   * <pre>
   * The ID of the WorkflowEventDef that this WorkflowEvent is a member of.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId workflow_event_def_id = 2;</code>
   * @return Whether the workflowEventDefId field is set.
   */
  boolean hasWorkflowEventDefId();
  /**
   * <pre>
   * The ID of the WorkflowEventDef that this WorkflowEvent is a member of.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId workflow_event_def_id = 2;</code>
   * @return The workflowEventDefId.
   */
  io.littlehorse.sdk.common.proto.WorkflowEventDefId getWorkflowEventDefId();
  /**
   * <pre>
   * The ID of the WorkflowEventDef that this WorkflowEvent is a member of.
   * </pre>
   *
   * <code>.littlehorse.WorkflowEventDefId workflow_event_def_id = 2;</code>
   */
  io.littlehorse.sdk.common.proto.WorkflowEventDefIdOrBuilder getWorkflowEventDefIdOrBuilder();

  /**
   * <pre>
   * A sequence number that makes the WorkflowEventId unique among all WorkflowEvent's of the
   * same type thrown by the WfRun. This field starts at zero and is incremented every
   * time a WorkflowEvent of the same type is thrown by the same WfRun.
   * </pre>
   *
   * <code>int32 number = 3;</code>
   * @return The number.
   */
  int getNumber();
}

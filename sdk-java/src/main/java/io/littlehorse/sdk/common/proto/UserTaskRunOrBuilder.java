// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskRunOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskRun)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the UserTaskRun.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The ID of the UserTaskRun.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunId getId();
  /**
   * <pre>
   * The ID of the UserTaskRun.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskRunIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * The ID of the UserTaskDef that this UserTaskRun comes from.
   * </pre>
   *
   * <code>.littlehorse.UserTaskDefId user_task_def_id = 2;</code>
   * @return Whether the userTaskDefId field is set.
   */
  boolean hasUserTaskDefId();
  /**
   * <pre>
   * The ID of the UserTaskDef that this UserTaskRun comes from.
   * </pre>
   *
   * <code>.littlehorse.UserTaskDefId user_task_def_id = 2;</code>
   * @return The userTaskDefId.
   */
  io.littlehorse.sdk.common.proto.UserTaskDefId getUserTaskDefId();
  /**
   * <pre>
   * The ID of the UserTaskDef that this UserTaskRun comes from.
   * </pre>
   *
   * <code>.littlehorse.UserTaskDefId user_task_def_id = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskDefIdOrBuilder getUserTaskDefIdOrBuilder();

  /**
   * <pre>
   * Denotes the UserTaskRun as belonging to a specific User Group.
   *
   * The `user_group` does not refer to a group that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user/group identity and ensure
   * that the user_group does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return Whether the userGroup field is set.
   */
  boolean hasUserGroup();
  /**
   * <pre>
   * Denotes the UserTaskRun as belonging to a specific User Group.
   *
   * The `user_group` does not refer to a group that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user/group identity and ensure
   * that the user_group does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return The userGroup.
   */
  java.lang.String getUserGroup();
  /**
   * <pre>
   * Denotes the UserTaskRun as belonging to a specific User Group.
   *
   * The `user_group` does not refer to a group that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user/group identity and ensure
   * that the user_group does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_group = 3;</code>
   * @return The bytes for userGroup.
   */
  com.google.protobuf.ByteString
      getUserGroupBytes();

  /**
   * <pre>
   * Denotes the UserTaskRun as assigned to a specific User ID. If this is set, then
   * the UserTaskRun is either in the ASSIGNED, DONE, or CANCELLED status.
   *
   * The `user_id` does not refer to a User that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user identity and ensure
   * that the user_id does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return Whether the userId field is set.
   */
  boolean hasUserId();
  /**
   * <pre>
   * Denotes the UserTaskRun as assigned to a specific User ID. If this is set, then
   * the UserTaskRun is either in the ASSIGNED, DONE, or CANCELLED status.
   *
   * The `user_id` does not refer to a User that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user identity and ensure
   * that the user_id does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return The userId.
   */
  java.lang.String getUserId();
  /**
   * <pre>
   * Denotes the UserTaskRun as assigned to a specific User ID. If this is set, then
   * the UserTaskRun is either in the ASSIGNED, DONE, or CANCELLED status.
   *
   * The `user_id` does not refer to a User that is stored in LittleHorse; rather, it
   * is the responsibility of the application to keep track of user identity and ensure
   * that the user_id does indeed exist.
   *
   * Either `user_id` or `user_group` or both are set at any time.
   * </pre>
   *
   * <code>optional string user_id = 4;</code>
   * @return The bytes for userId.
   */
  com.google.protobuf.ByteString
      getUserIdBytes();

  /**
   * <pre>
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 6;</code>
   */
  int getResultsCount();
  /**
   * <pre>
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 6;</code>
   */
  boolean containsResults(
      java.lang.String key);
  /**
   * Use {@link #getResultsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getResults();
  /**
   * <pre>
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 6;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.VariableValue>
  getResultsMap();
  /**
   * <pre>
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 6;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.VariableValue getResultsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.VariableValue defaultValue);
  /**
   * <pre>
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.VariableValue&gt; results = 6;</code>
   */
  io.littlehorse.sdk.common.proto.VariableValue getResultsOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * Status of the UserTaskRun. Can be UNASSIGNED, ASSIGNED, DONE, or CANCELLED.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunStatus status = 7;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <pre>
   * Status of the UserTaskRun. Can be UNASSIGNED, ASSIGNED, DONE, or CANCELLED.
   * </pre>
   *
   * <code>.littlehorse.UserTaskRunStatus status = 7;</code>
   * @return The status.
   */
  io.littlehorse.sdk.common.proto.UserTaskRunStatus getStatus();

  /**
   * <pre>
   * A list of events that have happened. Used for auditing information.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskEvent events = 8;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.UserTaskEvent> 
      getEventsList();
  /**
   * <pre>
   * A list of events that have happened. Used for auditing information.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskEvent events = 8;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEvent getEvents(int index);
  /**
   * <pre>
   * A list of events that have happened. Used for auditing information.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskEvent events = 8;</code>
   */
  int getEventsCount();
  /**
   * <pre>
   * A list of events that have happened. Used for auditing information.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskEvent events = 8;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.UserTaskEventOrBuilder> 
      getEventsOrBuilderList();
  /**
   * <pre>
   * A list of events that have happened. Used for auditing information.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskEvent events = 8;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskEventOrBuilder getEventsOrBuilder(
      int index);

  /**
   * <pre>
   * Notes about this UserTaskRun that are **specific to the WfRun**. These notes
   * are set by the WfSpec based on variables inside the specific `WfRun` and are
   * intended to be displayed on the User Task Manager application. They do not
   * affect WfRun execution.
   * </pre>
   *
   * <code>optional string notes = 9;</code>
   * @return Whether the notes field is set.
   */
  boolean hasNotes();
  /**
   * <pre>
   * Notes about this UserTaskRun that are **specific to the WfRun**. These notes
   * are set by the WfSpec based on variables inside the specific `WfRun` and are
   * intended to be displayed on the User Task Manager application. They do not
   * affect WfRun execution.
   * </pre>
   *
   * <code>optional string notes = 9;</code>
   * @return The notes.
   */
  java.lang.String getNotes();
  /**
   * <pre>
   * Notes about this UserTaskRun that are **specific to the WfRun**. These notes
   * are set by the WfSpec based on variables inside the specific `WfRun` and are
   * intended to be displayed on the User Task Manager application. They do not
   * affect WfRun execution.
   * </pre>
   *
   * <code>optional string notes = 9;</code>
   * @return The bytes for notes.
   */
  com.google.protobuf.ByteString
      getNotesBytes();

  /**
   * <pre>
   * The time that the UserTaskRun was created/scheduled.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp scheduled_time = 10;</code>
   * @return Whether the scheduledTime field is set.
   */
  boolean hasScheduledTime();
  /**
   * <pre>
   * The time that the UserTaskRun was created/scheduled.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp scheduled_time = 10;</code>
   * @return The scheduledTime.
   */
  com.google.protobuf.Timestamp getScheduledTime();
  /**
   * <pre>
   * The time that the UserTaskRun was created/scheduled.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp scheduled_time = 10;</code>
   */
  com.google.protobuf.TimestampOrBuilder getScheduledTimeOrBuilder();

  /**
   * <pre>
   * The NodeRun with which the UserTaskRun is associated.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 11;</code>
   * @return Whether the nodeRunId field is set.
   */
  boolean hasNodeRunId();
  /**
   * <pre>
   * The NodeRun with which the UserTaskRun is associated.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 11;</code>
   * @return The nodeRunId.
   */
  io.littlehorse.sdk.common.proto.NodeRunId getNodeRunId();
  /**
   * <pre>
   * The NodeRun with which the UserTaskRun is associated.
   * </pre>
   *
   * <code>.littlehorse.NodeRunId node_run_id = 11;</code>
   */
  io.littlehorse.sdk.common.proto.NodeRunIdOrBuilder getNodeRunIdOrBuilder();

  /**
   * <pre>
   * Current observed epoch of the UserTaskRun, related to the number of times it has been
   * updated or re-assigned. Used internally to implement automated reassignment and reminder
   * tasks.
   * </pre>
   *
   * <code>int32 epoch = 12;</code>
   * @return The epoch.
   */
  int getEpoch();
}

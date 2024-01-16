// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user_tasks.proto

package io.littlehorse.sdk.common.proto;

public interface UserTaskDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.UserTaskDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the `UserTaskDef`
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the `UserTaskDef`
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * The version of the `UserTaskDef`. Only simple versioning is supported.
   * </pre>
   *
   * <code>int32 version = 2;</code>
   * @return The version.
   */
  int getVersion();

  /**
   * <pre>
   * Metadata field that does not impact WfRun execution. Useful for providing
   * context on the UserTaskRun, for example when displaying it on a general-purpose
   * task manager application.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <pre>
   * Metadata field that does not impact WfRun execution. Useful for providing
   * context on the UserTaskRun, for example when displaying it on a general-purpose
   * task manager application.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <pre>
   * Metadata field that does not impact WfRun execution. Useful for providing
   * context on the UserTaskRun, for example when displaying it on a general-purpose
   * task manager application.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <pre>
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.UserTaskField> 
      getFieldsList();
  /**
   * <pre>
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 4;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskField getFields(int index);
  /**
   * <pre>
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 4;</code>
   */
  int getFieldsCount();
  /**
   * <pre>
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.UserTaskFieldOrBuilder> 
      getFieldsOrBuilderList();
  /**
   * <pre>
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 4;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskFieldOrBuilder getFieldsOrBuilder(
      int index);

  /**
   * <pre>
   * The time the UserTaskRun was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 5;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * The time the UserTaskRun was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 5;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * The time the UserTaskRun was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 5;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();
}

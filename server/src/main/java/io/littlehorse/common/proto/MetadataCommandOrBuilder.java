// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

public interface MetadataCommandOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.MetadataCommand)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <code>.google.protobuf.Timestamp time = 1;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();

  /**
   * <code>optional string command_id = 2;</code>
   * @return Whether the commandId field is set.
   */
  boolean hasCommandId();
  /**
   * <code>optional string command_id = 2;</code>
   * @return The commandId.
   */
  java.lang.String getCommandId();
  /**
   * <code>optional string command_id = 2;</code>
   * @return The bytes for commandId.
   */
  com.google.protobuf.ByteString
      getCommandIdBytes();

  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 4;</code>
   * @return Whether the putWfSpec field is set.
   */
  boolean hasPutWfSpec();
  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 4;</code>
   * @return The putWfSpec.
   */
  io.littlehorse.sdk.common.proto.PutWfSpecRequest getPutWfSpec();
  /**
   * <code>.littlehorse.PutWfSpecRequest put_wf_spec = 4;</code>
   */
  io.littlehorse.sdk.common.proto.PutWfSpecRequestOrBuilder getPutWfSpecOrBuilder();

  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 5;</code>
   * @return Whether the putTaskDef field is set.
   */
  boolean hasPutTaskDef();
  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 5;</code>
   * @return The putTaskDef.
   */
  io.littlehorse.sdk.common.proto.PutTaskDefRequest getPutTaskDef();
  /**
   * <code>.littlehorse.PutTaskDefRequest put_task_def = 5;</code>
   */
  io.littlehorse.sdk.common.proto.PutTaskDefRequestOrBuilder getPutTaskDefOrBuilder();

  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 6;</code>
   * @return Whether the putExternalEventDef field is set.
   */
  boolean hasPutExternalEventDef();
  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 6;</code>
   * @return The putExternalEventDef.
   */
  io.littlehorse.sdk.common.proto.PutExternalEventDefRequest getPutExternalEventDef();
  /**
   * <code>.littlehorse.PutExternalEventDefRequest put_external_event_def = 6;</code>
   */
  io.littlehorse.sdk.common.proto.PutExternalEventDefRequestOrBuilder getPutExternalEventDefOrBuilder();

  /**
   * <code>.littlehorse.DeleteWfSpecRequest delete_wf_spec = 7;</code>
   * @return Whether the deleteWfSpec field is set.
   */
  boolean hasDeleteWfSpec();
  /**
   * <code>.littlehorse.DeleteWfSpecRequest delete_wf_spec = 7;</code>
   * @return The deleteWfSpec.
   */
  io.littlehorse.sdk.common.proto.DeleteWfSpecRequest getDeleteWfSpec();
  /**
   * <code>.littlehorse.DeleteWfSpecRequest delete_wf_spec = 7;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteWfSpecRequestOrBuilder getDeleteWfSpecOrBuilder();

  /**
   * <code>.littlehorse.DeleteTaskDefRequest delete_task_def = 8;</code>
   * @return Whether the deleteTaskDef field is set.
   */
  boolean hasDeleteTaskDef();
  /**
   * <code>.littlehorse.DeleteTaskDefRequest delete_task_def = 8;</code>
   * @return The deleteTaskDef.
   */
  io.littlehorse.sdk.common.proto.DeleteTaskDefRequest getDeleteTaskDef();
  /**
   * <code>.littlehorse.DeleteTaskDefRequest delete_task_def = 8;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteTaskDefRequestOrBuilder getDeleteTaskDefOrBuilder();

  /**
   * <code>.littlehorse.DeleteExternalEventDefRequest delete_external_event_def = 9;</code>
   * @return Whether the deleteExternalEventDef field is set.
   */
  boolean hasDeleteExternalEventDef();
  /**
   * <code>.littlehorse.DeleteExternalEventDefRequest delete_external_event_def = 9;</code>
   * @return The deleteExternalEventDef.
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest getDeleteExternalEventDef();
  /**
   * <code>.littlehorse.DeleteExternalEventDefRequest delete_external_event_def = 9;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequestOrBuilder getDeleteExternalEventDefOrBuilder();

  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 10;</code>
   * @return Whether the putUserTaskDef field is set.
   */
  boolean hasPutUserTaskDef();
  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 10;</code>
   * @return The putUserTaskDef.
   */
  io.littlehorse.sdk.common.proto.PutUserTaskDefRequest getPutUserTaskDef();
  /**
   * <code>.littlehorse.PutUserTaskDefRequest put_user_task_def = 10;</code>
   */
  io.littlehorse.sdk.common.proto.PutUserTaskDefRequestOrBuilder getPutUserTaskDefOrBuilder();

  /**
   * <code>.littlehorse.DeleteUserTaskDefRequest delete_user_task_def = 11;</code>
   * @return Whether the deleteUserTaskDef field is set.
   */
  boolean hasDeleteUserTaskDef();
  /**
   * <code>.littlehorse.DeleteUserTaskDefRequest delete_user_task_def = 11;</code>
   * @return The deleteUserTaskDef.
   */
  io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest getDeleteUserTaskDef();
  /**
   * <code>.littlehorse.DeleteUserTaskDefRequest delete_user_task_def = 11;</code>
   */
  io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequestOrBuilder getDeleteUserTaskDefOrBuilder();

  io.littlehorse.common.proto.MetadataCommand.MetadataCommandCase getMetadataCommandCase();
}

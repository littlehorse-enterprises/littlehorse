// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface PutUserTaskDefRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutUserTaskDefRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The name of the UserTaskDef to create.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * The name of the UserTaskDef to create.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <pre>
   * The fields that should be presented to the user on the screen in order to fill
   * out the User Task. Note that these fields also define a data contract for the
   * result of the UserTaskDef.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 2;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.UserTaskField> 
      getFieldsList();
  /**
   * <pre>
   * The fields that should be presented to the user on the screen in order to fill
   * out the User Task. Note that these fields also define a data contract for the
   * result of the UserTaskDef.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskField getFields(int index);
  /**
   * <pre>
   * The fields that should be presented to the user on the screen in order to fill
   * out the User Task. Note that these fields also define a data contract for the
   * result of the UserTaskDef.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 2;</code>
   */
  int getFieldsCount();
  /**
   * <pre>
   * The fields that should be presented to the user on the screen in order to fill
   * out the User Task. Note that these fields also define a data contract for the
   * result of the UserTaskDef.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 2;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.UserTaskFieldOrBuilder> 
      getFieldsOrBuilderList();
  /**
   * <pre>
   * The fields that should be presented to the user on the screen in order to fill
   * out the User Task. Note that these fields also define a data contract for the
   * result of the UserTaskDef.
   * </pre>
   *
   * <code>repeated .littlehorse.UserTaskField fields = 2;</code>
   */
  io.littlehorse.sdk.common.proto.UserTaskFieldOrBuilder getFieldsOrBuilder(
      int index);

  /**
   * <pre>
   * Optional metadata field to store user-defined data. Does not impact workflow
   * execution. Fine to store up to a few KB of text here.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <pre>
   * Optional metadata field to store user-defined data. Does not impact workflow
   * execution. Fine to store up to a few KB of text here.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <pre>
   * Optional metadata field to store user-defined data. Does not impact workflow
   * execution. Fine to store up to a few KB of text here.
   * </pre>
   *
   * <code>optional string description = 3;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();
}

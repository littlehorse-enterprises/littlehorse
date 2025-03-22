// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: wf_spec.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface ThreadVarDefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.ThreadVarDef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Is the actual VariableDefinition containing name and type.
   * </pre>
   *
   * <code>.littlehorse.VariableDef var_def = 1;</code>
   * @return Whether the varDef field is set.
   */
  boolean hasVarDef();
  /**
   * <pre>
   * Is the actual VariableDefinition containing name and type.
   * </pre>
   *
   * <code>.littlehorse.VariableDef var_def = 1;</code>
   * @return The varDef.
   */
  io.littlehorse.sdk.common.proto.VariableDef getVarDef();
  /**
   * <pre>
   * Is the actual VariableDefinition containing name and type.
   * </pre>
   *
   * <code>.littlehorse.VariableDef var_def = 1;</code>
   */
  io.littlehorse.sdk.common.proto.VariableDefOrBuilder getVarDefOrBuilder();

  /**
   * <pre>
   * Whether the variable is required as input to the threadRun.
   * </pre>
   *
   * <code>bool required = 2;</code>
   * @return The required.
   */
  boolean getRequired();

  /**
   * <pre>
   * Whether this variable has an index configured.
   * </pre>
   *
   * <code>bool searchable = 3;</code>
   * @return The searchable.
   */
  boolean getSearchable();

  /**
   * <pre>
   * Valid for JSON_OBJ and JSON_ARR variables only. List of JSON fields
   * to index.
   * </pre>
   *
   * <code>repeated .littlehorse.JsonIndex json_indexes = 4;</code>
   */
  java.util.List<io.littlehorse.sdk.common.proto.JsonIndex> 
      getJsonIndexesList();
  /**
   * <pre>
   * Valid for JSON_OBJ and JSON_ARR variables only. List of JSON fields
   * to index.
   * </pre>
   *
   * <code>repeated .littlehorse.JsonIndex json_indexes = 4;</code>
   */
  io.littlehorse.sdk.common.proto.JsonIndex getJsonIndexes(int index);
  /**
   * <pre>
   * Valid for JSON_OBJ and JSON_ARR variables only. List of JSON fields
   * to index.
   * </pre>
   *
   * <code>repeated .littlehorse.JsonIndex json_indexes = 4;</code>
   */
  int getJsonIndexesCount();
  /**
   * <pre>
   * Valid for JSON_OBJ and JSON_ARR variables only. List of JSON fields
   * to index.
   * </pre>
   *
   * <code>repeated .littlehorse.JsonIndex json_indexes = 4;</code>
   */
  java.util.List<? extends io.littlehorse.sdk.common.proto.JsonIndexOrBuilder> 
      getJsonIndexesOrBuilderList();
  /**
   * <pre>
   * Valid for JSON_OBJ and JSON_ARR variables only. List of JSON fields
   * to index.
   * </pre>
   *
   * <code>repeated .littlehorse.JsonIndex json_indexes = 4;</code>
   */
  io.littlehorse.sdk.common.proto.JsonIndexOrBuilder getJsonIndexesOrBuilder(
      int index);

  /**
   * <pre>
   * The Access Level of this variable.
   * </pre>
   *
   * <code>.littlehorse.WfRunVariableAccessLevel access_level = 5;</code>
   * @return The enum numeric value on the wire for accessLevel.
   */
  int getAccessLevelValue();
  /**
   * <pre>
   * The Access Level of this variable.
   * </pre>
   *
   * <code>.littlehorse.WfRunVariableAccessLevel access_level = 5;</code>
   * @return The accessLevel.
   */
  io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel getAccessLevel();
}

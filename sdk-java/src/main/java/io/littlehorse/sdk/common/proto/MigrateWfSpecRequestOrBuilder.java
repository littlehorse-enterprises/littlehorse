// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: service.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.sdk.common.proto;

public interface MigrateWfSpecRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.MigrateWfSpecRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.littlehorse.WfSpecId old_wf_spec = 1;</code>
   * @return Whether the oldWfSpec field is set.
   */
  boolean hasOldWfSpec();
  /**
   * <code>.littlehorse.WfSpecId old_wf_spec = 1;</code>
   * @return The oldWfSpec.
   */
  io.littlehorse.sdk.common.proto.WfSpecId getOldWfSpec();
  /**
   * <code>.littlehorse.WfSpecId old_wf_spec = 1;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getOldWfSpecOrBuilder();

  /**
   * <code>.littlehorse.WfSpecVersionMigration migration = 2;</code>
   * @return Whether the migration field is set.
   */
  boolean hasMigration();
  /**
   * <code>.littlehorse.WfSpecVersionMigration migration = 2;</code>
   * @return The migration.
   */
  io.littlehorse.sdk.common.proto.WfSpecVersionMigration getMigration();
  /**
   * <code>.littlehorse.WfSpecVersionMigration migration = 2;</code>
   */
  io.littlehorse.sdk.common.proto.WfSpecVersionMigrationOrBuilder getMigrationOrBuilder();
}

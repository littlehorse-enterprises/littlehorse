// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.common.proto;

public interface PutPrincipalRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutPrincipalRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  java.util.List<io.littlehorse.common.proto.ServerACL>
      getAclsList();
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  io.littlehorse.common.proto.ServerACL getAcls(int index);
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  int getAclsCount();
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  java.util.List<? extends io.littlehorse.common.proto.ServerACLOrBuilder>
      getAclsOrBuilderList();
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  io.littlehorse.common.proto.ServerACLOrBuilder getAclsOrBuilder(
      int index);

  /**
   * <code>optional string tenant_id = 3;</code>
   * @return Whether the tenantId field is set.
   */
  boolean hasTenantId();
  /**
   * <code>optional string tenant_id = 3;</code>
   * @return The tenantId.
   */
  java.lang.String getTenantId();
  /**
   * <code>optional string tenant_id = 3;</code>
   * @return The bytes for tenantId.
   */
  com.google.protobuf.ByteString
      getTenantIdBytes();

  /**
   * <code>bool overwrite = 5;</code>
   * @return The overwrite.
   */
  boolean getOverwrite();
}

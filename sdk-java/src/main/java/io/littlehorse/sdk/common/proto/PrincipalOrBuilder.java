// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: acls.proto
// Protobuf Java Version: 4.29.3

package io.littlehorse.sdk.common.proto;

public interface PrincipalOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.Principal)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the Principal. In OAuth for human users, this is the user_id. In
   * OAuth for machine clients, this is the Client ID.
   *
   * mTLS for Principal identification is not yet implemented.
   * </pre>
   *
   * <code>.littlehorse.PrincipalId id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <pre>
   * The ID of the Principal. In OAuth for human users, this is the user_id. In
   * OAuth for machine clients, this is the Client ID.
   *
   * mTLS for Principal identification is not yet implemented.
   * </pre>
   *
   * <code>.littlehorse.PrincipalId id = 1;</code>
   * @return The id.
   */
  io.littlehorse.sdk.common.proto.PrincipalId getId();
  /**
   * <pre>
   * The ID of the Principal. In OAuth for human users, this is the user_id. In
   * OAuth for machine clients, this is the Client ID.
   *
   * mTLS for Principal identification is not yet implemented.
   * </pre>
   *
   * <code>.littlehorse.PrincipalId id = 1;</code>
   */
  io.littlehorse.sdk.common.proto.PrincipalIdOrBuilder getIdOrBuilder();

  /**
   * <pre>
   * The time at which the Principal was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return Whether the createdAt field is set.
   */
  boolean hasCreatedAt();
  /**
   * <pre>
   * The time at which the Principal was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   * @return The createdAt.
   */
  com.google.protobuf.Timestamp getCreatedAt();
  /**
   * <pre>
   * The time at which the Principal was created.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp created_at = 2;</code>
   */
  com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder();

  /**
   * <pre>
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 3;</code>
   */
  int getPerTenantAclsCount();
  /**
   * <pre>
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 3;</code>
   */
  boolean containsPerTenantAcls(
      java.lang.String key);
  /**
   * Use {@link #getPerTenantAclsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.ServerACLs>
  getPerTenantAcls();
  /**
   * <pre>
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 3;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.ServerACLs>
  getPerTenantAclsMap();
  /**
   * <pre>
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 3;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.ServerACLs getPerTenantAclsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.ServerACLs defaultValue);
  /**
   * <pre>
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ServerACLs getPerTenantAclsOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * Sets permissions that this Principal has *for any Tenant* in the LH Cluster.
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 4;</code>
   * @return Whether the globalAcls field is set.
   */
  boolean hasGlobalAcls();
  /**
   * <pre>
   * Sets permissions that this Principal has *for any Tenant* in the LH Cluster.
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 4;</code>
   * @return The globalAcls.
   */
  io.littlehorse.sdk.common.proto.ServerACLs getGlobalAcls();
  /**
   * <pre>
   * Sets permissions that this Principal has *for any Tenant* in the LH Cluster.
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 4;</code>
   */
  io.littlehorse.sdk.common.proto.ServerACLsOrBuilder getGlobalAclsOrBuilder();
}

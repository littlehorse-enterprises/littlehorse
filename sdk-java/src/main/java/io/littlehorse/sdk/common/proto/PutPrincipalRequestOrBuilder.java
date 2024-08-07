// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.sdk.common.proto;

public interface PutPrincipalRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:littlehorse.PutPrincipalRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The ID of the Principal that we are creating.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <pre>
   * The ID of the Principal that we are creating.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <pre>
   * The per-tenant ACL's for the Principal
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 2;</code>
   */
  int getPerTenantAclsCount();
  /**
   * <pre>
   * The per-tenant ACL's for the Principal
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 2;</code>
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
   * The per-tenant ACL's for the Principal
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 2;</code>
   */
  java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.ServerACLs>
  getPerTenantAclsMap();
  /**
   * <pre>
   * The per-tenant ACL's for the Principal
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 2;</code>
   */
  /* nullable */
io.littlehorse.sdk.common.proto.ServerACLs getPerTenantAclsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.ServerACLs defaultValue);
  /**
   * <pre>
   * The per-tenant ACL's for the Principal
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.ServerACLs&gt; per_tenant_acls = 2;</code>
   */
  io.littlehorse.sdk.common.proto.ServerACLs getPerTenantAclsOrThrow(
      java.lang.String key);

  /**
   * <pre>
   * The ACL's for the principal in all tenants
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 3;</code>
   * @return Whether the globalAcls field is set.
   */
  boolean hasGlobalAcls();
  /**
   * <pre>
   * The ACL's for the principal in all tenants
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 3;</code>
   * @return The globalAcls.
   */
  io.littlehorse.sdk.common.proto.ServerACLs getGlobalAcls();
  /**
   * <pre>
   * The ACL's for the principal in all tenants
   * </pre>
   *
   * <code>.littlehorse.ServerACLs global_acls = 3;</code>
   */
  io.littlehorse.sdk.common.proto.ServerACLsOrBuilder getGlobalAclsOrBuilder();

  /**
   * <pre>
   * If this is set to false and a `Principal` with the same `id` already exists *and*
   * has different ACL's configured, then the RPC throws `ALREADY_EXISTS`.
   *
   * If this is set to `true`, then the RPC will override hte
   * </pre>
   *
   * <code>bool overwrite = 5;</code>
   * @return The overwrite.
   */
  boolean getOverwrite();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: acls.proto

package io.littlehorse.common.proto;

/**
 * <pre>
 * This is a GlobalGetable.
 * </pre>
 *
 * Protobuf type {@code littlehorse.Principal}
 */
public final class Principal extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.Principal)
    PrincipalOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Principal.newBuilder() to construct.
  private Principal(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Principal() {
    id_ = "";
    acls_ = java.util.Collections.emptyList();
    tenantId_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Principal();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.Acls.internal_static_littlehorse_Principal_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.Acls.internal_static_littlehorse_Principal_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.Principal.class, io.littlehorse.common.proto.Principal.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object id_ = "";
  /**
   * <pre>
   * Principals are agnostic of the Authentication protocol that you use. In OAuth,
   * the id is retrieved by looking at the claims on the request. In mTLS, the
   * id is retrived by looking at the Subject Name of the client certificate.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Principals are agnostic of the Authentication protocol that you use. In OAuth,
   * the id is retrieved by looking at the claims on the request. In mTLS, the
   * id is retrived by looking at the Subject Name of the client certificate.
   * </pre>
   *
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ACLS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.ServerACL> acls_;
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.ServerACL> getAclsList() {
    return acls_;
  }
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.ServerACLOrBuilder> 
      getAclsOrBuilderList() {
    return acls_;
  }
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  @java.lang.Override
  public int getAclsCount() {
    return acls_.size();
  }
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.ServerACL getAcls(int index) {
    return acls_.get(index);
  }
  /**
   * <code>repeated .littlehorse.ServerACL acls = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.ServerACLOrBuilder getAclsOrBuilder(
      int index) {
    return acls_.get(index);
  }

  public static final int TENANT_ID_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private com.google.protobuf.LazyStringArrayList tenantId_ =
      com.google.protobuf.LazyStringArrayList.emptyList();
  /**
   * <pre>
   * Used for multi-tenancy of the LittleHorse Server.
   *
   * NOTE: the principal id (field 1) MUST be unique across all tenants. The
   * way multi-tenancy works is that the
   * </pre>
   *
   * <code>repeated string tenant_id = 3;</code>
   * @return A list containing the tenantId.
   */
  public com.google.protobuf.ProtocolStringList
      getTenantIdList() {
    return tenantId_;
  }
  /**
   * <pre>
   * Used for multi-tenancy of the LittleHorse Server.
   *
   * NOTE: the principal id (field 1) MUST be unique across all tenants. The
   * way multi-tenancy works is that the
   * </pre>
   *
   * <code>repeated string tenant_id = 3;</code>
   * @return The count of tenantId.
   */
  public int getTenantIdCount() {
    return tenantId_.size();
  }
  /**
   * <pre>
   * Used for multi-tenancy of the LittleHorse Server.
   *
   * NOTE: the principal id (field 1) MUST be unique across all tenants. The
   * way multi-tenancy works is that the
   * </pre>
   *
   * <code>repeated string tenant_id = 3;</code>
   * @param index The index of the element to return.
   * @return The tenantId at the given index.
   */
  public java.lang.String getTenantId(int index) {
    return tenantId_.get(index);
  }
  /**
   * <pre>
   * Used for multi-tenancy of the LittleHorse Server.
   *
   * NOTE: the principal id (field 1) MUST be unique across all tenants. The
   * way multi-tenancy works is that the
   * </pre>
   *
   * <code>repeated string tenant_id = 3;</code>
   * @param index The index of the value to return.
   * @return The bytes of the tenantId at the given index.
   */
  public com.google.protobuf.ByteString
      getTenantIdBytes(int index) {
    return tenantId_.getByteString(index);
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(id_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, id_);
    }
    for (int i = 0; i < acls_.size(); i++) {
      output.writeMessage(2, acls_.get(i));
    }
    for (int i = 0; i < tenantId_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, tenantId_.getRaw(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(id_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, id_);
    }
    for (int i = 0; i < acls_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, acls_.get(i));
    }
    {
      int dataSize = 0;
      for (int i = 0; i < tenantId_.size(); i++) {
        dataSize += computeStringSizeNoTag(tenantId_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getTenantIdList().size();
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.common.proto.Principal)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.Principal other = (io.littlehorse.common.proto.Principal) obj;

    if (!getId()
        .equals(other.getId())) return false;
    if (!getAclsList()
        .equals(other.getAclsList())) return false;
    if (!getTenantIdList()
        .equals(other.getTenantIdList())) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + getId().hashCode();
    if (getAclsCount() > 0) {
      hash = (37 * hash) + ACLS_FIELD_NUMBER;
      hash = (53 * hash) + getAclsList().hashCode();
    }
    if (getTenantIdCount() > 0) {
      hash = (37 * hash) + TENANT_ID_FIELD_NUMBER;
      hash = (53 * hash) + getTenantIdList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.Principal parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.Principal parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.Principal parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.Principal parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.littlehorse.common.proto.Principal prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * This is a GlobalGetable.
   * </pre>
   *
   * Protobuf type {@code littlehorse.Principal}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.Principal)
      io.littlehorse.common.proto.PrincipalOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.Acls.internal_static_littlehorse_Principal_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.Acls.internal_static_littlehorse_Principal_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.Principal.class, io.littlehorse.common.proto.Principal.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.Principal.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      id_ = "";
      if (aclsBuilder_ == null) {
        acls_ = java.util.Collections.emptyList();
      } else {
        acls_ = null;
        aclsBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000002);
      tenantId_ =
          com.google.protobuf.LazyStringArrayList.emptyList();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.Acls.internal_static_littlehorse_Principal_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.Principal getDefaultInstanceForType() {
      return io.littlehorse.common.proto.Principal.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.Principal build() {
      io.littlehorse.common.proto.Principal result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.Principal buildPartial() {
      io.littlehorse.common.proto.Principal result = new io.littlehorse.common.proto.Principal(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.Principal result) {
      if (aclsBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          acls_ = java.util.Collections.unmodifiableList(acls_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.acls_ = acls_;
      } else {
        result.acls_ = aclsBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.Principal result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = id_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        tenantId_.makeImmutable();
        result.tenantId_ = tenantId_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.Principal) {
        return mergeFrom((io.littlehorse.common.proto.Principal)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.Principal other) {
      if (other == io.littlehorse.common.proto.Principal.getDefaultInstance()) return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (aclsBuilder_ == null) {
        if (!other.acls_.isEmpty()) {
          if (acls_.isEmpty()) {
            acls_ = other.acls_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureAclsIsMutable();
            acls_.addAll(other.acls_);
          }
          onChanged();
        }
      } else {
        if (!other.acls_.isEmpty()) {
          if (aclsBuilder_.isEmpty()) {
            aclsBuilder_.dispose();
            aclsBuilder_ = null;
            acls_ = other.acls_;
            bitField0_ = (bitField0_ & ~0x00000002);
            aclsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getAclsFieldBuilder() : null;
          } else {
            aclsBuilder_.addAllMessages(other.acls_);
          }
        }
      }
      if (!other.tenantId_.isEmpty()) {
        if (tenantId_.isEmpty()) {
          tenantId_ = other.tenantId_;
          bitField0_ |= 0x00000004;
        } else {
          ensureTenantIdIsMutable();
          tenantId_.addAll(other.tenantId_);
        }
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              id_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              io.littlehorse.common.proto.ServerACL m =
                  input.readMessage(
                      io.littlehorse.common.proto.ServerACL.parser(),
                      extensionRegistry);
              if (aclsBuilder_ == null) {
                ensureAclsIsMutable();
                acls_.add(m);
              } else {
                aclsBuilder_.addMessage(m);
              }
              break;
            } // case 18
            case 26: {
              java.lang.String s = input.readStringRequireUtf8();
              ensureTenantIdIsMutable();
              tenantId_.add(s);
              break;
            } // case 26
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private java.lang.Object id_ = "";
    /**
     * <pre>
     * Principals are agnostic of the Authentication protocol that you use. In OAuth,
     * the id is retrieved by looking at the claims on the request. In mTLS, the
     * id is retrived by looking at the Subject Name of the client certificate.
     * </pre>
     *
     * <code>string id = 1;</code>
     * @return The id.
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Principals are agnostic of the Authentication protocol that you use. In OAuth,
     * the id is retrieved by looking at the claims on the request. In mTLS, the
     * id is retrived by looking at the Subject Name of the client certificate.
     * </pre>
     *
     * <code>string id = 1;</code>
     * @return The bytes for id.
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Principals are agnostic of the Authentication protocol that you use. In OAuth,
     * the id is retrieved by looking at the claims on the request. In mTLS, the
     * id is retrived by looking at the Subject Name of the client certificate.
     * </pre>
     *
     * <code>string id = 1;</code>
     * @param value The id to set.
     * @return This builder for chaining.
     */
    public Builder setId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      id_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Principals are agnostic of the Authentication protocol that you use. In OAuth,
     * the id is retrieved by looking at the claims on the request. In mTLS, the
     * id is retrived by looking at the Subject Name of the client certificate.
     * </pre>
     *
     * <code>string id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearId() {
      id_ = getDefaultInstance().getId();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Principals are agnostic of the Authentication protocol that you use. In OAuth,
     * the id is retrieved by looking at the claims on the request. In mTLS, the
     * id is retrived by looking at the Subject Name of the client certificate.
     * </pre>
     *
     * <code>string id = 1;</code>
     * @param value The bytes for id to set.
     * @return This builder for chaining.
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      id_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.util.List<io.littlehorse.common.proto.ServerACL> acls_ =
      java.util.Collections.emptyList();
    private void ensureAclsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        acls_ = new java.util.ArrayList<io.littlehorse.common.proto.ServerACL>(acls_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.ServerACL, io.littlehorse.common.proto.ServerACL.Builder, io.littlehorse.common.proto.ServerACLOrBuilder> aclsBuilder_;

    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.ServerACL> getAclsList() {
      if (aclsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(acls_);
      } else {
        return aclsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public int getAclsCount() {
      if (aclsBuilder_ == null) {
        return acls_.size();
      } else {
        return aclsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public io.littlehorse.common.proto.ServerACL getAcls(int index) {
      if (aclsBuilder_ == null) {
        return acls_.get(index);
      } else {
        return aclsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder setAcls(
        int index, io.littlehorse.common.proto.ServerACL value) {
      if (aclsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAclsIsMutable();
        acls_.set(index, value);
        onChanged();
      } else {
        aclsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder setAcls(
        int index, io.littlehorse.common.proto.ServerACL.Builder builderForValue) {
      if (aclsBuilder_ == null) {
        ensureAclsIsMutable();
        acls_.set(index, builderForValue.build());
        onChanged();
      } else {
        aclsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder addAcls(io.littlehorse.common.proto.ServerACL value) {
      if (aclsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAclsIsMutable();
        acls_.add(value);
        onChanged();
      } else {
        aclsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder addAcls(
        int index, io.littlehorse.common.proto.ServerACL value) {
      if (aclsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAclsIsMutable();
        acls_.add(index, value);
        onChanged();
      } else {
        aclsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder addAcls(
        io.littlehorse.common.proto.ServerACL.Builder builderForValue) {
      if (aclsBuilder_ == null) {
        ensureAclsIsMutable();
        acls_.add(builderForValue.build());
        onChanged();
      } else {
        aclsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder addAcls(
        int index, io.littlehorse.common.proto.ServerACL.Builder builderForValue) {
      if (aclsBuilder_ == null) {
        ensureAclsIsMutable();
        acls_.add(index, builderForValue.build());
        onChanged();
      } else {
        aclsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder addAllAcls(
        java.lang.Iterable<? extends io.littlehorse.common.proto.ServerACL> values) {
      if (aclsBuilder_ == null) {
        ensureAclsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, acls_);
        onChanged();
      } else {
        aclsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder clearAcls() {
      if (aclsBuilder_ == null) {
        acls_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        aclsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public Builder removeAcls(int index) {
      if (aclsBuilder_ == null) {
        ensureAclsIsMutable();
        acls_.remove(index);
        onChanged();
      } else {
        aclsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public io.littlehorse.common.proto.ServerACL.Builder getAclsBuilder(
        int index) {
      return getAclsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public io.littlehorse.common.proto.ServerACLOrBuilder getAclsOrBuilder(
        int index) {
      if (aclsBuilder_ == null) {
        return acls_.get(index);  } else {
        return aclsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.ServerACLOrBuilder> 
         getAclsOrBuilderList() {
      if (aclsBuilder_ != null) {
        return aclsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(acls_);
      }
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public io.littlehorse.common.proto.ServerACL.Builder addAclsBuilder() {
      return getAclsFieldBuilder().addBuilder(
          io.littlehorse.common.proto.ServerACL.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public io.littlehorse.common.proto.ServerACL.Builder addAclsBuilder(
        int index) {
      return getAclsFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.ServerACL.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.ServerACL acls = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.ServerACL.Builder> 
         getAclsBuilderList() {
      return getAclsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.ServerACL, io.littlehorse.common.proto.ServerACL.Builder, io.littlehorse.common.proto.ServerACLOrBuilder> 
        getAclsFieldBuilder() {
      if (aclsBuilder_ == null) {
        aclsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.common.proto.ServerACL, io.littlehorse.common.proto.ServerACL.Builder, io.littlehorse.common.proto.ServerACLOrBuilder>(
                acls_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        acls_ = null;
      }
      return aclsBuilder_;
    }

    private com.google.protobuf.LazyStringArrayList tenantId_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
    private void ensureTenantIdIsMutable() {
      if (!tenantId_.isModifiable()) {
        tenantId_ = new com.google.protobuf.LazyStringArrayList(tenantId_);
      }
      bitField0_ |= 0x00000004;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @return A list containing the tenantId.
     */
    public com.google.protobuf.ProtocolStringList
        getTenantIdList() {
      tenantId_.makeImmutable();
      return tenantId_;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @return The count of tenantId.
     */
    public int getTenantIdCount() {
      return tenantId_.size();
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param index The index of the element to return.
     * @return The tenantId at the given index.
     */
    public java.lang.String getTenantId(int index) {
      return tenantId_.get(index);
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param index The index of the value to return.
     * @return The bytes of the tenantId at the given index.
     */
    public com.google.protobuf.ByteString
        getTenantIdBytes(int index) {
      return tenantId_.getByteString(index);
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param index The index to set the value at.
     * @param value The tenantId to set.
     * @return This builder for chaining.
     */
    public Builder setTenantId(
        int index, java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureTenantIdIsMutable();
      tenantId_.set(index, value);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param value The tenantId to add.
     * @return This builder for chaining.
     */
    public Builder addTenantId(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      ensureTenantIdIsMutable();
      tenantId_.add(value);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param values The tenantId to add.
     * @return This builder for chaining.
     */
    public Builder addAllTenantId(
        java.lang.Iterable<java.lang.String> values) {
      ensureTenantIdIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, tenantId_);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearTenantId() {
      tenantId_ =
        com.google.protobuf.LazyStringArrayList.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Used for multi-tenancy of the LittleHorse Server.
     *
     * NOTE: the principal id (field 1) MUST be unique across all tenants. The
     * way multi-tenancy works is that the
     * </pre>
     *
     * <code>repeated string tenant_id = 3;</code>
     * @param value The bytes of the tenantId to add.
     * @return This builder for chaining.
     */
    public Builder addTenantIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      ensureTenantIdIsMutable();
      tenantId_.add(value);
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:littlehorse.Principal)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.Principal)
  private static final io.littlehorse.common.proto.Principal DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.Principal();
  }

  public static io.littlehorse.common.proto.Principal getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Principal>
      PARSER = new com.google.protobuf.AbstractParser<Principal>() {
    @java.lang.Override
    public Principal parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<Principal> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Principal> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.Principal getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


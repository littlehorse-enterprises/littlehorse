// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: wf_spec.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * EXPERIMENTAL: Specification for how to migrate a ThreadRun of a specific ThreadSpec
 * from one WfSpec to another WfSpec version.
 * </pre>
 *
 * Protobuf type {@code littlehorse.ThreadSpecMigration}
 */
public final class ThreadSpecMigration extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ThreadSpecMigration)
    ThreadSpecMigrationOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ThreadSpecMigration.newBuilder() to construct.
  private ThreadSpecMigration(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ThreadSpecMigration() {
    newThreadSpecName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ThreadSpecMigration();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 2:
        return internalGetNodeMigrations();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ThreadSpecMigration.class, io.littlehorse.sdk.common.proto.ThreadSpecMigration.Builder.class);
  }

  public static final int NEW_THREAD_SPEC_NAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object newThreadSpecName_ = "";
  /**
   * <pre>
   * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
   * migrate to.
   * </pre>
   *
   * <code>string new_thread_spec_name = 1;</code>
   * @return The newThreadSpecName.
   */
  @java.lang.Override
  public java.lang.String getNewThreadSpecName() {
    java.lang.Object ref = newThreadSpecName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      newThreadSpecName_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
   * migrate to.
   * </pre>
   *
   * <code>string new_thread_spec_name = 1;</code>
   * @return The bytes for newThreadSpecName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNewThreadSpecNameBytes() {
    java.lang.Object ref = newThreadSpecName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      newThreadSpecName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int NODE_MIGRATIONS_FIELD_NUMBER = 2;
  private static final class NodeMigrationsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>newDefaultInstance(
                io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_NodeMigrationsEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                io.littlehorse.sdk.common.proto.NodeMigration.getDefaultInstance());
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> nodeMigrations_;
  private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
  internalGetNodeMigrations() {
    if (nodeMigrations_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          NodeMigrationsDefaultEntryHolder.defaultEntry);
    }
    return nodeMigrations_;
  }
  public int getNodeMigrationsCount() {
    return internalGetNodeMigrations().getMap().size();
  }
  /**
   * <pre>
   * Map from name of the nodes on the current ThreadSpec to the migration
   * to perform on it to move it to a new WfSpec.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
   */
  @java.lang.Override
  public boolean containsNodeMigrations(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetNodeMigrations().getMap().containsKey(key);
  }
  /**
   * Use {@link #getNodeMigrationsMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> getNodeMigrations() {
    return getNodeMigrationsMap();
  }
  /**
   * <pre>
   * Map from name of the nodes on the current ThreadSpec to the migration
   * to perform on it to move it to a new WfSpec.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> getNodeMigrationsMap() {
    return internalGetNodeMigrations().getMap();
  }
  /**
   * <pre>
   * Map from name of the nodes on the current ThreadSpec to the migration
   * to perform on it to move it to a new WfSpec.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
   */
  @java.lang.Override
  public /* nullable */
io.littlehorse.sdk.common.proto.NodeMigration getNodeMigrationsOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.sdk.common.proto.NodeMigration defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> map =
        internalGetNodeMigrations().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <pre>
   * Map from name of the nodes on the current ThreadSpec to the migration
   * to perform on it to move it to a new WfSpec.
   * </pre>
   *
   * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.NodeMigration getNodeMigrationsOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> map =
        internalGetNodeMigrations().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(newThreadSpecName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, newThreadSpecName_);
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetNodeMigrations(),
        NodeMigrationsDefaultEntryHolder.defaultEntry,
        2);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(newThreadSpecName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, newThreadSpecName_);
    }
    for (java.util.Map.Entry<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> entry
         : internalGetNodeMigrations().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
      nodeMigrations__ = NodeMigrationsDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, nodeMigrations__);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ThreadSpecMigration)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ThreadSpecMigration other = (io.littlehorse.sdk.common.proto.ThreadSpecMigration) obj;

    if (!getNewThreadSpecName()
        .equals(other.getNewThreadSpecName())) return false;
    if (!internalGetNodeMigrations().equals(
        other.internalGetNodeMigrations())) return false;
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
    hash = (37 * hash) + NEW_THREAD_SPEC_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getNewThreadSpecName().hashCode();
    if (!internalGetNodeMigrations().getMap().isEmpty()) {
      hash = (37 * hash) + NODE_MIGRATIONS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetNodeMigrations().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ThreadSpecMigration prototype) {
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
   * EXPERIMENTAL: Specification for how to migrate a ThreadRun of a specific ThreadSpec
   * from one WfSpec to another WfSpec version.
   * </pre>
   *
   * Protobuf type {@code littlehorse.ThreadSpecMigration}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ThreadSpecMigration)
      io.littlehorse.sdk.common.proto.ThreadSpecMigrationOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 2:
          return internalGetNodeMigrations();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 2:
          return internalGetMutableNodeMigrations();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ThreadSpecMigration.class, io.littlehorse.sdk.common.proto.ThreadSpecMigration.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ThreadSpecMigration.newBuilder()
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
      newThreadSpecName_ = "";
      internalGetMutableNodeMigrations().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.WfSpecOuterClass.internal_static_littlehorse_ThreadSpecMigration_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadSpecMigration getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ThreadSpecMigration.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadSpecMigration build() {
      io.littlehorse.sdk.common.proto.ThreadSpecMigration result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ThreadSpecMigration buildPartial() {
      io.littlehorse.sdk.common.proto.ThreadSpecMigration result = new io.littlehorse.sdk.common.proto.ThreadSpecMigration(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ThreadSpecMigration result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.newThreadSpecName_ = newThreadSpecName_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.nodeMigrations_ = internalGetNodeMigrations();
        result.nodeMigrations_.makeImmutable();
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
      if (other instanceof io.littlehorse.sdk.common.proto.ThreadSpecMigration) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ThreadSpecMigration)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ThreadSpecMigration other) {
      if (other == io.littlehorse.sdk.common.proto.ThreadSpecMigration.getDefaultInstance()) return this;
      if (!other.getNewThreadSpecName().isEmpty()) {
        newThreadSpecName_ = other.newThreadSpecName_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      internalGetMutableNodeMigrations().mergeFrom(
          other.internalGetNodeMigrations());
      bitField0_ |= 0x00000002;
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
              newThreadSpecName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
              nodeMigrations__ = input.readMessage(
                  NodeMigrationsDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableNodeMigrations().getMutableMap().put(
                  nodeMigrations__.getKey(), nodeMigrations__.getValue());
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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

    private java.lang.Object newThreadSpecName_ = "";
    /**
     * <pre>
     * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
     * migrate to.
     * </pre>
     *
     * <code>string new_thread_spec_name = 1;</code>
     * @return The newThreadSpecName.
     */
    public java.lang.String getNewThreadSpecName() {
      java.lang.Object ref = newThreadSpecName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        newThreadSpecName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
     * migrate to.
     * </pre>
     *
     * <code>string new_thread_spec_name = 1;</code>
     * @return The bytes for newThreadSpecName.
     */
    public com.google.protobuf.ByteString
        getNewThreadSpecNameBytes() {
      java.lang.Object ref = newThreadSpecName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        newThreadSpecName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
     * migrate to.
     * </pre>
     *
     * <code>string new_thread_spec_name = 1;</code>
     * @param value The newThreadSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setNewThreadSpecName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      newThreadSpecName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
     * migrate to.
     * </pre>
     *
     * <code>string new_thread_spec_name = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearNewThreadSpecName() {
      newThreadSpecName_ = getDefaultInstance().getNewThreadSpecName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The name of the ThreadSpec in the new WfSpec that this ThreadSpec should
     * migrate to.
     * </pre>
     *
     * <code>string new_thread_spec_name = 1;</code>
     * @param value The bytes for newThreadSpecName to set.
     * @return This builder for chaining.
     */
    public Builder setNewThreadSpecNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      newThreadSpecName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
        java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> nodeMigrations_;
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
        internalGetNodeMigrations() {
      if (nodeMigrations_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            NodeMigrationsDefaultEntryHolder.defaultEntry);
      }
      return nodeMigrations_;
    }
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
        internalGetMutableNodeMigrations() {
      if (nodeMigrations_ == null) {
        nodeMigrations_ = com.google.protobuf.MapField.newMapField(
            NodeMigrationsDefaultEntryHolder.defaultEntry);
      }
      if (!nodeMigrations_.isMutable()) {
        nodeMigrations_ = nodeMigrations_.copy();
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return nodeMigrations_;
    }
    public int getNodeMigrationsCount() {
      return internalGetNodeMigrations().getMap().size();
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    @java.lang.Override
    public boolean containsNodeMigrations(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetNodeMigrations().getMap().containsKey(key);
    }
    /**
     * Use {@link #getNodeMigrationsMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> getNodeMigrations() {
      return getNodeMigrationsMap();
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> getNodeMigrationsMap() {
      return internalGetNodeMigrations().getMap();
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    @java.lang.Override
    public /* nullable */
io.littlehorse.sdk.common.proto.NodeMigration getNodeMigrationsOrDefault(
        java.lang.String key,
        /* nullable */
io.littlehorse.sdk.common.proto.NodeMigration defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> map =
          internalGetNodeMigrations().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.NodeMigration getNodeMigrationsOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> map =
          internalGetNodeMigrations().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearNodeMigrations() {
      bitField0_ = (bitField0_ & ~0x00000002);
      internalGetMutableNodeMigrations().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    public Builder removeNodeMigrations(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableNodeMigrations().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration>
        getMutableNodeMigrations() {
      bitField0_ |= 0x00000002;
      return internalGetMutableNodeMigrations().getMutableMap();
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    public Builder putNodeMigrations(
        java.lang.String key,
        io.littlehorse.sdk.common.proto.NodeMigration value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableNodeMigrations().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000002;
      return this;
    }
    /**
     * <pre>
     * Map from name of the nodes on the current ThreadSpec to the migration
     * to perform on it to move it to a new WfSpec.
     * </pre>
     *
     * <code>map&lt;string, .littlehorse.NodeMigration&gt; node_migrations = 2;</code>
     */
    public Builder putAllNodeMigrations(
        java.util.Map<java.lang.String, io.littlehorse.sdk.common.proto.NodeMigration> values) {
      internalGetMutableNodeMigrations().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000002;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ThreadSpecMigration)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ThreadSpecMigration)
  private static final io.littlehorse.sdk.common.proto.ThreadSpecMigration DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ThreadSpecMigration();
  }

  public static io.littlehorse.sdk.common.proto.ThreadSpecMigration getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ThreadSpecMigration>
      PARSER = new com.google.protobuf.AbstractParser<ThreadSpecMigration>() {
    @java.lang.Override
    public ThreadSpecMigration parsePartialFrom(
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

  public static com.google.protobuf.Parser<ThreadSpecMigration> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ThreadSpecMigration> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ThreadSpecMigration getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


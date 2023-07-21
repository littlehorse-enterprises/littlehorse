// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.TagChangesToBroadcastPb}
 */
public final class TagChangesToBroadcastPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.TagChangesToBroadcastPb)
    TagChangesToBroadcastPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use TagChangesToBroadcastPb.newBuilder() to construct.
  private TagChangesToBroadcastPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private TagChangesToBroadcastPb() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new TagChangesToBroadcastPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 1:
        return internalGetChangelog();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.TagChangesToBroadcastPb.class, io.littlehorse.common.proto.TagChangesToBroadcastPb.Builder.class);
  }

  public static final int CHANGELOG_FIELD_NUMBER = 1;
  private static final class ChangelogDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>newDefaultInstance(
                io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_ChangelogEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                io.littlehorse.common.proto.DiscreteTagLocalCounterPb.getDefaultInstance());
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> changelog_;
  private com.google.protobuf.MapField<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
  internalGetChangelog() {
    if (changelog_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          ChangelogDefaultEntryHolder.defaultEntry);
    }
    return changelog_;
  }
  public int getChangelogCount() {
    return internalGetChangelog().getMap().size();
  }
  /**
   * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
   */
  @java.lang.Override
  public boolean containsChangelog(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    return internalGetChangelog().getMap().containsKey(key);
  }
  /**
   * Use {@link #getChangelogMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> getChangelog() {
    return getChangelogMap();
  }
  /**
   * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> getChangelogMap() {
    return internalGetChangelog().getMap();
  }
  /**
   * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
   */
  @java.lang.Override
  public /* nullable */
io.littlehorse.common.proto.DiscreteTagLocalCounterPb getChangelogOrDefault(
      java.lang.String key,
      /* nullable */
io.littlehorse.common.proto.DiscreteTagLocalCounterPb defaultValue) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> map =
        internalGetChangelog().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.DiscreteTagLocalCounterPb getChangelogOrThrow(
      java.lang.String key) {
    if (key == null) { throw new NullPointerException("map key"); }
    java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> map =
        internalGetChangelog().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int PARTITION_FIELD_NUMBER = 2;
  private int partition_ = 0;
  /**
   * <code>int32 partition = 2;</code>
   * @return The partition.
   */
  @java.lang.Override
  public int getPartition() {
    return partition_;
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
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetChangelog(),
        ChangelogDefaultEntryHolder.defaultEntry,
        1);
    if (partition_ != 0) {
      output.writeInt32(2, partition_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> entry
         : internalGetChangelog().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
      changelog__ = ChangelogDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, changelog__);
    }
    if (partition_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, partition_);
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
    if (!(obj instanceof io.littlehorse.common.proto.TagChangesToBroadcastPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.TagChangesToBroadcastPb other = (io.littlehorse.common.proto.TagChangesToBroadcastPb) obj;

    if (!internalGetChangelog().equals(
        other.internalGetChangelog())) return false;
    if (getPartition()
        != other.getPartition()) return false;
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
    if (!internalGetChangelog().getMap().isEmpty()) {
      hash = (37 * hash) + CHANGELOG_FIELD_NUMBER;
      hash = (53 * hash) + internalGetChangelog().hashCode();
    }
    hash = (37 * hash) + PARTITION_FIELD_NUMBER;
    hash = (53 * hash) + getPartition();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.TagChangesToBroadcastPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.TagChangesToBroadcastPb prototype) {
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
   * Protobuf type {@code littlehorse.TagChangesToBroadcastPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.TagChangesToBroadcastPb)
      io.littlehorse.common.proto.TagChangesToBroadcastPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetChangelog();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetMutableChangelog();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.TagChangesToBroadcastPb.class, io.littlehorse.common.proto.TagChangesToBroadcastPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.TagChangesToBroadcastPb.newBuilder()
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
      internalGetMutableChangelog().clear();
      partition_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_TagChangesToBroadcastPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TagChangesToBroadcastPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.TagChangesToBroadcastPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TagChangesToBroadcastPb build() {
      io.littlehorse.common.proto.TagChangesToBroadcastPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.TagChangesToBroadcastPb buildPartial() {
      io.littlehorse.common.proto.TagChangesToBroadcastPb result = new io.littlehorse.common.proto.TagChangesToBroadcastPb(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.common.proto.TagChangesToBroadcastPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.changelog_ = internalGetChangelog();
        result.changelog_.makeImmutable();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.partition_ = partition_;
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
      if (other instanceof io.littlehorse.common.proto.TagChangesToBroadcastPb) {
        return mergeFrom((io.littlehorse.common.proto.TagChangesToBroadcastPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.TagChangesToBroadcastPb other) {
      if (other == io.littlehorse.common.proto.TagChangesToBroadcastPb.getDefaultInstance()) return this;
      internalGetMutableChangelog().mergeFrom(
          other.internalGetChangelog());
      bitField0_ |= 0x00000001;
      if (other.getPartition() != 0) {
        setPartition(other.getPartition());
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
              com.google.protobuf.MapEntry<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
              changelog__ = input.readMessage(
                  ChangelogDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableChangelog().getMutableMap().put(
                  changelog__.getKey(), changelog__.getValue());
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              partition_ = input.readInt32();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
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

    private com.google.protobuf.MapField<
        java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> changelog_;
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
        internalGetChangelog() {
      if (changelog_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ChangelogDefaultEntryHolder.defaultEntry);
      }
      return changelog_;
    }
    private com.google.protobuf.MapField<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
        internalGetMutableChangelog() {
      if (changelog_ == null) {
        changelog_ = com.google.protobuf.MapField.newMapField(
            ChangelogDefaultEntryHolder.defaultEntry);
      }
      if (!changelog_.isMutable()) {
        changelog_ = changelog_.copy();
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return changelog_;
    }
    public int getChangelogCount() {
      return internalGetChangelog().getMap().size();
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    @java.lang.Override
    public boolean containsChangelog(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      return internalGetChangelog().getMap().containsKey(key);
    }
    /**
     * Use {@link #getChangelogMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> getChangelog() {
      return getChangelogMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> getChangelogMap() {
      return internalGetChangelog().getMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    @java.lang.Override
    public /* nullable */
io.littlehorse.common.proto.DiscreteTagLocalCounterPb getChangelogOrDefault(
        java.lang.String key,
        /* nullable */
io.littlehorse.common.proto.DiscreteTagLocalCounterPb defaultValue) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> map =
          internalGetChangelog().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    @java.lang.Override
    public io.littlehorse.common.proto.DiscreteTagLocalCounterPb getChangelogOrThrow(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> map =
          internalGetChangelog().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearChangelog() {
      bitField0_ = (bitField0_ & ~0x00000001);
      internalGetMutableChangelog().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    public Builder removeChangelog(
        java.lang.String key) {
      if (key == null) { throw new NullPointerException("map key"); }
      internalGetMutableChangelog().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb>
        getMutableChangelog() {
      bitField0_ |= 0x00000001;
      return internalGetMutableChangelog().getMutableMap();
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    public Builder putChangelog(
        java.lang.String key,
        io.littlehorse.common.proto.DiscreteTagLocalCounterPb value) {
      if (key == null) { throw new NullPointerException("map key"); }
      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableChangelog().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>map&lt;string, .littlehorse.DiscreteTagLocalCounterPb&gt; changelog = 1;</code>
     */
    public Builder putAllChangelog(
        java.util.Map<java.lang.String, io.littlehorse.common.proto.DiscreteTagLocalCounterPb> values) {
      internalGetMutableChangelog().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000001;
      return this;
    }

    private int partition_ ;
    /**
     * <code>int32 partition = 2;</code>
     * @return The partition.
     */
    @java.lang.Override
    public int getPartition() {
      return partition_;
    }
    /**
     * <code>int32 partition = 2;</code>
     * @param value The partition to set.
     * @return This builder for chaining.
     */
    public Builder setPartition(int value) {
      
      partition_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>int32 partition = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPartition() {
      bitField0_ = (bitField0_ & ~0x00000002);
      partition_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.TagChangesToBroadcastPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.TagChangesToBroadcastPb)
  private static final io.littlehorse.common.proto.TagChangesToBroadcastPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.TagChangesToBroadcastPb();
  }

  public static io.littlehorse.common.proto.TagChangesToBroadcastPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<TagChangesToBroadcastPb>
      PARSER = new com.google.protobuf.AbstractParser<TagChangesToBroadcastPb>() {
    @java.lang.Override
    public TagChangesToBroadcastPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<TagChangesToBroadcastPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<TagChangesToBroadcastPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.TagChangesToBroadcastPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


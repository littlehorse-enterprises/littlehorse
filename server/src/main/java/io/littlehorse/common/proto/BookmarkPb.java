// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: internal_server.proto

package io.littlehorse.common.proto;

/**
 * <pre>
 * Used for paginated responses
 * </pre>
 *
 * Protobuf type {@code littlehorse.BookmarkPb}
 */
public final class BookmarkPb extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.BookmarkPb)
    BookmarkPbOrBuilder {
private static final long serialVersionUID = 0L;
  // Use BookmarkPb.newBuilder() to construct.
  private BookmarkPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private BookmarkPb() {
    completedPartitions_ = emptyIntList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new BookmarkPb();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 1:
        return internalGetInProgressPartitions();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.BookmarkPb.class, io.littlehorse.common.proto.BookmarkPb.Builder.class);
  }

  public static final int IN_PROGRESS_PARTITIONS_FIELD_NUMBER = 1;
  private static final class InProgressPartitionsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>newDefaultInstance(
                io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_InProgressPartitionsEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.INT32,
                0,
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                io.littlehorse.common.proto.PartitionBookmarkPb.getDefaultInstance());
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> inProgressPartitions_;
  private com.google.protobuf.MapField<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
  internalGetInProgressPartitions() {
    if (inProgressPartitions_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          InProgressPartitionsDefaultEntryHolder.defaultEntry);
    }
    return inProgressPartitions_;
  }
  public int getInProgressPartitionsCount() {
    return internalGetInProgressPartitions().getMap().size();
  }
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  @java.lang.Override
  public boolean containsInProgressPartitions(
      int key) {

    return internalGetInProgressPartitions().getMap().containsKey(key);
  }
  /**
   * Use {@link #getInProgressPartitionsMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> getInProgressPartitions() {
    return getInProgressPartitionsMap();
  }
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> getInProgressPartitionsMap() {
    return internalGetInProgressPartitions().getMap();
  }
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  @java.lang.Override
  public /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrDefault(
      int key,
      /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb defaultValue) {

    java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> map =
        internalGetInProgressPartitions().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrThrow(
      int key) {

    java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> map =
        internalGetInProgressPartitions().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int COMPLETED_PARTITIONS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private com.google.protobuf.Internal.IntList completedPartitions_;
  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @return A list containing the completedPartitions.
   */
  @java.lang.Override
  public java.util.List<java.lang.Integer>
      getCompletedPartitionsList() {
    return completedPartitions_;
  }
  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @return The count of completedPartitions.
   */
  public int getCompletedPartitionsCount() {
    return completedPartitions_.size();
  }
  /**
   * <code>repeated int32 completed_partitions = 2;</code>
   * @param index The index of the element to return.
   * @return The completedPartitions at the given index.
   */
  public int getCompletedPartitions(int index) {
    return completedPartitions_.getInt(index);
  }
  private int completedPartitionsMemoizedSerializedSize = -1;

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
    getSerializedSize();
    com.google.protobuf.GeneratedMessageV3
      .serializeIntegerMapTo(
        output,
        internalGetInProgressPartitions(),
        InProgressPartitionsDefaultEntryHolder.defaultEntry,
        1);
    if (getCompletedPartitionsList().size() > 0) {
      output.writeUInt32NoTag(18);
      output.writeUInt32NoTag(completedPartitionsMemoizedSerializedSize);
    }
    for (int i = 0; i < completedPartitions_.size(); i++) {
      output.writeInt32NoTag(completedPartitions_.getInt(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> entry
         : internalGetInProgressPartitions().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
      inProgressPartitions__ = InProgressPartitionsDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, inProgressPartitions__);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < completedPartitions_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt32SizeNoTag(completedPartitions_.getInt(i));
      }
      size += dataSize;
      if (!getCompletedPartitionsList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      completedPartitionsMemoizedSerializedSize = dataSize;
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
    if (!(obj instanceof io.littlehorse.common.proto.BookmarkPb)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.BookmarkPb other = (io.littlehorse.common.proto.BookmarkPb) obj;

    if (!internalGetInProgressPartitions().equals(
        other.internalGetInProgressPartitions())) return false;
    if (!getCompletedPartitionsList()
        .equals(other.getCompletedPartitionsList())) return false;
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
    if (!internalGetInProgressPartitions().getMap().isEmpty()) {
      hash = (37 * hash) + IN_PROGRESS_PARTITIONS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetInProgressPartitions().hashCode();
    }
    if (getCompletedPartitionsCount() > 0) {
      hash = (37 * hash) + COMPLETED_PARTITIONS_FIELD_NUMBER;
      hash = (53 * hash) + getCompletedPartitionsList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.BookmarkPb parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.BookmarkPb parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.BookmarkPb parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.BookmarkPb prototype) {
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
   * Used for paginated responses
   * </pre>
   *
   * Protobuf type {@code littlehorse.BookmarkPb}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.BookmarkPb)
      io.littlehorse.common.proto.BookmarkPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetInProgressPartitions();
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
          return internalGetMutableInProgressPartitions();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.BookmarkPb.class, io.littlehorse.common.proto.BookmarkPb.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.BookmarkPb.newBuilder()
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
      internalGetMutableInProgressPartitions().clear();
      completedPartitions_ = emptyIntList();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.InternalServer.internal_static_littlehorse_BookmarkPb_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.BookmarkPb getDefaultInstanceForType() {
      return io.littlehorse.common.proto.BookmarkPb.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.BookmarkPb build() {
      io.littlehorse.common.proto.BookmarkPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.BookmarkPb buildPartial() {
      io.littlehorse.common.proto.BookmarkPb result = new io.littlehorse.common.proto.BookmarkPb(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.BookmarkPb result) {
      if (((bitField0_ & 0x00000002) != 0)) {
        completedPartitions_.makeImmutable();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.completedPartitions_ = completedPartitions_;
    }

    private void buildPartial0(io.littlehorse.common.proto.BookmarkPb result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.inProgressPartitions_ = internalGetInProgressPartitions();
        result.inProgressPartitions_.makeImmutable();
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
      if (other instanceof io.littlehorse.common.proto.BookmarkPb) {
        return mergeFrom((io.littlehorse.common.proto.BookmarkPb)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.BookmarkPb other) {
      if (other == io.littlehorse.common.proto.BookmarkPb.getDefaultInstance()) return this;
      internalGetMutableInProgressPartitions().mergeFrom(
          other.internalGetInProgressPartitions());
      bitField0_ |= 0x00000001;
      if (!other.completedPartitions_.isEmpty()) {
        if (completedPartitions_.isEmpty()) {
          completedPartitions_ = other.completedPartitions_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureCompletedPartitionsIsMutable();
          completedPartitions_.addAll(other.completedPartitions_);
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
              com.google.protobuf.MapEntry<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
              inProgressPartitions__ = input.readMessage(
                  InProgressPartitionsDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableInProgressPartitions().getMutableMap().put(
                  inProgressPartitions__.getKey(), inProgressPartitions__.getValue());
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              int v = input.readInt32();
              ensureCompletedPartitionsIsMutable();
              completedPartitions_.addInt(v);
              break;
            } // case 16
            case 18: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              ensureCompletedPartitionsIsMutable();
              while (input.getBytesUntilLimit() > 0) {
                completedPartitions_.addInt(input.readInt32());
              }
              input.popLimit(limit);
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

    private com.google.protobuf.MapField<
        java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> inProgressPartitions_;
    private com.google.protobuf.MapField<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
        internalGetInProgressPartitions() {
      if (inProgressPartitions_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            InProgressPartitionsDefaultEntryHolder.defaultEntry);
      }
      return inProgressPartitions_;
    }
    private com.google.protobuf.MapField<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
        internalGetMutableInProgressPartitions() {
      if (inProgressPartitions_ == null) {
        inProgressPartitions_ = com.google.protobuf.MapField.newMapField(
            InProgressPartitionsDefaultEntryHolder.defaultEntry);
      }
      if (!inProgressPartitions_.isMutable()) {
        inProgressPartitions_ = inProgressPartitions_.copy();
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return inProgressPartitions_;
    }
    public int getInProgressPartitionsCount() {
      return internalGetInProgressPartitions().getMap().size();
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    @java.lang.Override
    public boolean containsInProgressPartitions(
        int key) {

      return internalGetInProgressPartitions().getMap().containsKey(key);
    }
    /**
     * Use {@link #getInProgressPartitionsMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> getInProgressPartitions() {
      return getInProgressPartitionsMap();
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> getInProgressPartitionsMap() {
      return internalGetInProgressPartitions().getMap();
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    @java.lang.Override
    public /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrDefault(
        int key,
        /* nullable */
io.littlehorse.common.proto.PartitionBookmarkPb defaultValue) {

      java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> map =
          internalGetInProgressPartitions().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    @java.lang.Override
    public io.littlehorse.common.proto.PartitionBookmarkPb getInProgressPartitionsOrThrow(
        int key) {

      java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> map =
          internalGetInProgressPartitions().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearInProgressPartitions() {
      bitField0_ = (bitField0_ & ~0x00000001);
      internalGetMutableInProgressPartitions().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    public Builder removeInProgressPartitions(
        int key) {

      internalGetMutableInProgressPartitions().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb>
        getMutableInProgressPartitions() {
      bitField0_ |= 0x00000001;
      return internalGetMutableInProgressPartitions().getMutableMap();
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    public Builder putInProgressPartitions(
        int key,
        io.littlehorse.common.proto.PartitionBookmarkPb value) {

      if (value == null) { throw new NullPointerException("map value"); }
      internalGetMutableInProgressPartitions().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>map&lt;int32, .littlehorse.PartitionBookmarkPb&gt; in_progress_partitions = 1;</code>
     */
    public Builder putAllInProgressPartitions(
        java.util.Map<java.lang.Integer, io.littlehorse.common.proto.PartitionBookmarkPb> values) {
      internalGetMutableInProgressPartitions().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000001;
      return this;
    }

    private com.google.protobuf.Internal.IntList completedPartitions_ = emptyIntList();
    private void ensureCompletedPartitionsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        completedPartitions_ = mutableCopy(completedPartitions_);
        bitField0_ |= 0x00000002;
      }
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @return A list containing the completedPartitions.
     */
    public java.util.List<java.lang.Integer>
        getCompletedPartitionsList() {
      return ((bitField0_ & 0x00000002) != 0) ?
               java.util.Collections.unmodifiableList(completedPartitions_) : completedPartitions_;
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @return The count of completedPartitions.
     */
    public int getCompletedPartitionsCount() {
      return completedPartitions_.size();
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @param index The index of the element to return.
     * @return The completedPartitions at the given index.
     */
    public int getCompletedPartitions(int index) {
      return completedPartitions_.getInt(index);
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @param index The index to set the value at.
     * @param value The completedPartitions to set.
     * @return This builder for chaining.
     */
    public Builder setCompletedPartitions(
        int index, int value) {

      ensureCompletedPartitionsIsMutable();
      completedPartitions_.setInt(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @param value The completedPartitions to add.
     * @return This builder for chaining.
     */
    public Builder addCompletedPartitions(int value) {

      ensureCompletedPartitionsIsMutable();
      completedPartitions_.addInt(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @param values The completedPartitions to add.
     * @return This builder for chaining.
     */
    public Builder addAllCompletedPartitions(
        java.lang.Iterable<? extends java.lang.Integer> values) {
      ensureCompletedPartitionsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, completedPartitions_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 completed_partitions = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCompletedPartitions() {
      completedPartitions_ = emptyIntList();
      bitField0_ = (bitField0_ & ~0x00000002);
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


    // @@protoc_insertion_point(builder_scope:littlehorse.BookmarkPb)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.BookmarkPb)
  private static final io.littlehorse.common.proto.BookmarkPb DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.BookmarkPb();
  }

  public static io.littlehorse.common.proto.BookmarkPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<BookmarkPb>
      PARSER = new com.google.protobuf.AbstractParser<BookmarkPb>() {
    @java.lang.Override
    public BookmarkPb parsePartialFrom(
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

  public static com.google.protobuf.Parser<BookmarkPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<BookmarkPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.BookmarkPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


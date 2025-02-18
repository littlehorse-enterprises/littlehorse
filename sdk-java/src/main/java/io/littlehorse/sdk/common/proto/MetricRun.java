// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: metrics.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.MetricRun}
 */
public final class MetricRun extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.MetricRun)
    MetricRunOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MetricRun.newBuilder() to construct.
  private MetricRun(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MetricRun() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MetricRun();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 5:
        return internalGetValuePerPartition();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.MetricRun.class, io.littlehorse.sdk.common.proto.MetricRun.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.MetricRunId id_;
  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return id_ != null;
  }
  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricRunId getId() {
    return id_ == null ? io.littlehorse.sdk.common.proto.MetricRunId.getDefaultInstance() : id_;
  }
  /**
   * <code>.littlehorse.MetricRunId id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder getIdOrBuilder() {
    return id_ == null ? io.littlehorse.sdk.common.proto.MetricRunId.getDefaultInstance() : id_;
  }

  public static final int VALUE_FIELD_NUMBER = 2;
  private double value_ = 0D;
  /**
   * <code>double value = 2;</code>
   * @return The value.
   */
  @java.lang.Override
  public double getValue() {
    return value_;
  }

  public static final int CREATED_AT_FIELD_NUMBER = 4;
  private com.google.protobuf.Timestamp createdAt_;
  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   * @return Whether the createdAt field is set.
   */
  @java.lang.Override
  public boolean hasCreatedAt() {
    return createdAt_ != null;
  }
  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   * @return The createdAt.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getCreatedAt() {
    return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
  }
  /**
   * <code>.google.protobuf.Timestamp created_at = 4;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder() {
    return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
  }

  public static final int VALUE_PER_PARTITION_FIELD_NUMBER = 5;
  private static final class ValuePerPartitionDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        java.lang.Integer, java.lang.Double> defaultEntry =
            com.google.protobuf.MapEntry
            .<java.lang.Integer, java.lang.Double>newDefaultInstance(
                io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_ValuePerPartitionEntry_descriptor, 
                com.google.protobuf.WireFormat.FieldType.INT32,
                0,
                com.google.protobuf.WireFormat.FieldType.DOUBLE,
                0D);
  }
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      java.lang.Integer, java.lang.Double> valuePerPartition_;
  private com.google.protobuf.MapField<java.lang.Integer, java.lang.Double>
  internalGetValuePerPartition() {
    if (valuePerPartition_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          ValuePerPartitionDefaultEntryHolder.defaultEntry);
    }
    return valuePerPartition_;
  }
  public int getValuePerPartitionCount() {
    return internalGetValuePerPartition().getMap().size();
  }
  /**
   * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
   */
  @java.lang.Override
  public boolean containsValuePerPartition(
      int key) {

    return internalGetValuePerPartition().getMap().containsKey(key);
  }
  /**
   * Use {@link #getValuePerPartitionMap()} instead.
   */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.Integer, java.lang.Double> getValuePerPartition() {
    return getValuePerPartitionMap();
  }
  /**
   * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
   */
  @java.lang.Override
  public java.util.Map<java.lang.Integer, java.lang.Double> getValuePerPartitionMap() {
    return internalGetValuePerPartition().getMap();
  }
  /**
   * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
   */
  @java.lang.Override
  public double getValuePerPartitionOrDefault(
      int key,
      double defaultValue) {

    java.util.Map<java.lang.Integer, java.lang.Double> map =
        internalGetValuePerPartition().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
   */
  @java.lang.Override
  public double getValuePerPartitionOrThrow(
      int key) {

    java.util.Map<java.lang.Integer, java.lang.Double> map =
        internalGetValuePerPartition().getMap();
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
    if (id_ != null) {
      output.writeMessage(1, getId());
    }
    if (java.lang.Double.doubleToRawLongBits(value_) != 0) {
      output.writeDouble(2, value_);
    }
    if (createdAt_ != null) {
      output.writeMessage(4, getCreatedAt());
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeIntegerMapTo(
        output,
        internalGetValuePerPartition(),
        ValuePerPartitionDefaultEntryHolder.defaultEntry,
        5);
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (id_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getId());
    }
    if (java.lang.Double.doubleToRawLongBits(value_) != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(2, value_);
    }
    if (createdAt_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getCreatedAt());
    }
    for (java.util.Map.Entry<java.lang.Integer, java.lang.Double> entry
         : internalGetValuePerPartition().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.Integer, java.lang.Double>
      valuePerPartition__ = ValuePerPartitionDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(5, valuePerPartition__);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.MetricRun)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.MetricRun other = (io.littlehorse.sdk.common.proto.MetricRun) obj;

    if (hasId() != other.hasId()) return false;
    if (hasId()) {
      if (!getId()
          .equals(other.getId())) return false;
    }
    if (java.lang.Double.doubleToLongBits(getValue())
        != java.lang.Double.doubleToLongBits(
            other.getValue())) return false;
    if (hasCreatedAt() != other.hasCreatedAt()) return false;
    if (hasCreatedAt()) {
      if (!getCreatedAt()
          .equals(other.getCreatedAt())) return false;
    }
    if (!internalGetValuePerPartition().equals(
        other.internalGetValuePerPartition())) return false;
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
    if (hasId()) {
      hash = (37 * hash) + ID_FIELD_NUMBER;
      hash = (53 * hash) + getId().hashCode();
    }
    hash = (37 * hash) + VALUE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getValue()));
    if (hasCreatedAt()) {
      hash = (37 * hash) + CREATED_AT_FIELD_NUMBER;
      hash = (53 * hash) + getCreatedAt().hashCode();
    }
    if (!internalGetValuePerPartition().getMap().isEmpty()) {
      hash = (37 * hash) + VALUE_PER_PARTITION_FIELD_NUMBER;
      hash = (53 * hash) + internalGetValuePerPartition().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.MetricRun parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.MetricRun parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.MetricRun parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.MetricRun prototype) {
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
   * Protobuf type {@code littlehorse.MetricRun}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.MetricRun)
      io.littlehorse.sdk.common.proto.MetricRunOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 5:
          return internalGetValuePerPartition();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 5:
          return internalGetMutableValuePerPartition();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.MetricRun.class, io.littlehorse.sdk.common.proto.MetricRun.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.MetricRun.newBuilder()
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
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      value_ = 0D;
      createdAt_ = null;
      if (createdAtBuilder_ != null) {
        createdAtBuilder_.dispose();
        createdAtBuilder_ = null;
      }
      internalGetMutableValuePerPartition().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Metrics.internal_static_littlehorse_MetricRun_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricRun getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.MetricRun.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricRun build() {
      io.littlehorse.sdk.common.proto.MetricRun result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricRun buildPartial() {
      io.littlehorse.sdk.common.proto.MetricRun result = new io.littlehorse.sdk.common.proto.MetricRun(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.MetricRun result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = idBuilder_ == null
            ? id_
            : idBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.value_ = value_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.createdAt_ = createdAtBuilder_ == null
            ? createdAt_
            : createdAtBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.valuePerPartition_ = internalGetValuePerPartition();
        result.valuePerPartition_.makeImmutable();
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
      if (other instanceof io.littlehorse.sdk.common.proto.MetricRun) {
        return mergeFrom((io.littlehorse.sdk.common.proto.MetricRun)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.MetricRun other) {
      if (other == io.littlehorse.sdk.common.proto.MetricRun.getDefaultInstance()) return this;
      if (other.hasId()) {
        mergeId(other.getId());
      }
      if (other.getValue() != 0D) {
        setValue(other.getValue());
      }
      if (other.hasCreatedAt()) {
        mergeCreatedAt(other.getCreatedAt());
      }
      internalGetMutableValuePerPartition().mergeFrom(
          other.internalGetValuePerPartition());
      bitField0_ |= 0x00000008;
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
              input.readMessage(
                  getIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 17: {
              value_ = input.readDouble();
              bitField0_ |= 0x00000002;
              break;
            } // case 17
            case 34: {
              input.readMessage(
                  getCreatedAtFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
              break;
            } // case 34
            case 42: {
              com.google.protobuf.MapEntry<java.lang.Integer, java.lang.Double>
              valuePerPartition__ = input.readMessage(
                  ValuePerPartitionDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableValuePerPartition().getMutableMap().put(
                  valuePerPartition__.getKey(), valuePerPartition__.getValue());
              bitField0_ |= 0x00000008;
              break;
            } // case 42
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

    private io.littlehorse.sdk.common.proto.MetricRunId id_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.MetricRunId, io.littlehorse.sdk.common.proto.MetricRunId.Builder, io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder> idBuilder_;
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     * @return The id.
     */
    public io.littlehorse.sdk.common.proto.MetricRunId getId() {
      if (idBuilder_ == null) {
        return id_ == null ? io.littlehorse.sdk.common.proto.MetricRunId.getDefaultInstance() : id_;
      } else {
        return idBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public Builder setId(io.littlehorse.sdk.common.proto.MetricRunId value) {
      if (idBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        id_ = value;
      } else {
        idBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public Builder setId(
        io.littlehorse.sdk.common.proto.MetricRunId.Builder builderForValue) {
      if (idBuilder_ == null) {
        id_ = builderForValue.build();
      } else {
        idBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public Builder mergeId(io.littlehorse.sdk.common.proto.MetricRunId value) {
      if (idBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          id_ != null &&
          id_ != io.littlehorse.sdk.common.proto.MetricRunId.getDefaultInstance()) {
          getIdBuilder().mergeFrom(value);
        } else {
          id_ = value;
        }
      } else {
        idBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public Builder clearId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      id_ = null;
      if (idBuilder_ != null) {
        idBuilder_.dispose();
        idBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.MetricRunId.Builder getIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder getIdOrBuilder() {
      if (idBuilder_ != null) {
        return idBuilder_.getMessageOrBuilder();
      } else {
        return id_ == null ?
            io.littlehorse.sdk.common.proto.MetricRunId.getDefaultInstance() : id_;
      }
    }
    /**
     * <code>.littlehorse.MetricRunId id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.MetricRunId, io.littlehorse.sdk.common.proto.MetricRunId.Builder, io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder> 
        getIdFieldBuilder() {
      if (idBuilder_ == null) {
        idBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.MetricRunId, io.littlehorse.sdk.common.proto.MetricRunId.Builder, io.littlehorse.sdk.common.proto.MetricRunIdOrBuilder>(
                getId(),
                getParentForChildren(),
                isClean());
        id_ = null;
      }
      return idBuilder_;
    }

    private double value_ ;
    /**
     * <code>double value = 2;</code>
     * @return The value.
     */
    @java.lang.Override
    public double getValue() {
      return value_;
    }
    /**
     * <code>double value = 2;</code>
     * @param value The value to set.
     * @return This builder for chaining.
     */
    public Builder setValue(double value) {

      value_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>double value = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearValue() {
      bitField0_ = (bitField0_ & ~0x00000002);
      value_ = 0D;
      onChanged();
      return this;
    }

    private com.google.protobuf.Timestamp createdAt_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> createdAtBuilder_;
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     * @return Whether the createdAt field is set.
     */
    public boolean hasCreatedAt() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     * @return The createdAt.
     */
    public com.google.protobuf.Timestamp getCreatedAt() {
      if (createdAtBuilder_ == null) {
        return createdAt_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
      } else {
        return createdAtBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public Builder setCreatedAt(com.google.protobuf.Timestamp value) {
      if (createdAtBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        createdAt_ = value;
      } else {
        createdAtBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public Builder setCreatedAt(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (createdAtBuilder_ == null) {
        createdAt_ = builderForValue.build();
      } else {
        createdAtBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public Builder mergeCreatedAt(com.google.protobuf.Timestamp value) {
      if (createdAtBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          createdAt_ != null &&
          createdAt_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getCreatedAtBuilder().mergeFrom(value);
        } else {
          createdAt_ = value;
        }
      } else {
        createdAtBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public Builder clearCreatedAt() {
      bitField0_ = (bitField0_ & ~0x00000004);
      createdAt_ = null;
      if (createdAtBuilder_ != null) {
        createdAtBuilder_.dispose();
        createdAtBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public com.google.protobuf.Timestamp.Builder getCreatedAtBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getCreatedAtFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getCreatedAtOrBuilder() {
      if (createdAtBuilder_ != null) {
        return createdAtBuilder_.getMessageOrBuilder();
      } else {
        return createdAt_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : createdAt_;
      }
    }
    /**
     * <code>.google.protobuf.Timestamp created_at = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        getCreatedAtFieldBuilder() {
      if (createdAtBuilder_ == null) {
        createdAtBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getCreatedAt(),
                getParentForChildren(),
                isClean());
        createdAt_ = null;
      }
      return createdAtBuilder_;
    }

    private com.google.protobuf.MapField<
        java.lang.Integer, java.lang.Double> valuePerPartition_;
    private com.google.protobuf.MapField<java.lang.Integer, java.lang.Double>
        internalGetValuePerPartition() {
      if (valuePerPartition_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ValuePerPartitionDefaultEntryHolder.defaultEntry);
      }
      return valuePerPartition_;
    }
    private com.google.protobuf.MapField<java.lang.Integer, java.lang.Double>
        internalGetMutableValuePerPartition() {
      if (valuePerPartition_ == null) {
        valuePerPartition_ = com.google.protobuf.MapField.newMapField(
            ValuePerPartitionDefaultEntryHolder.defaultEntry);
      }
      if (!valuePerPartition_.isMutable()) {
        valuePerPartition_ = valuePerPartition_.copy();
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return valuePerPartition_;
    }
    public int getValuePerPartitionCount() {
      return internalGetValuePerPartition().getMap().size();
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    @java.lang.Override
    public boolean containsValuePerPartition(
        int key) {

      return internalGetValuePerPartition().getMap().containsKey(key);
    }
    /**
     * Use {@link #getValuePerPartitionMap()} instead.
     */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.Integer, java.lang.Double> getValuePerPartition() {
      return getValuePerPartitionMap();
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    @java.lang.Override
    public java.util.Map<java.lang.Integer, java.lang.Double> getValuePerPartitionMap() {
      return internalGetValuePerPartition().getMap();
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    @java.lang.Override
    public double getValuePerPartitionOrDefault(
        int key,
        double defaultValue) {

      java.util.Map<java.lang.Integer, java.lang.Double> map =
          internalGetValuePerPartition().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    @java.lang.Override
    public double getValuePerPartitionOrThrow(
        int key) {

      java.util.Map<java.lang.Integer, java.lang.Double> map =
          internalGetValuePerPartition().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }
    public Builder clearValuePerPartition() {
      bitField0_ = (bitField0_ & ~0x00000008);
      internalGetMutableValuePerPartition().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    public Builder removeValuePerPartition(
        int key) {

      internalGetMutableValuePerPartition().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.Integer, java.lang.Double>
        getMutableValuePerPartition() {
      bitField0_ |= 0x00000008;
      return internalGetMutableValuePerPartition().getMutableMap();
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    public Builder putValuePerPartition(
        int key,
        double value) {


      internalGetMutableValuePerPartition().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000008;
      return this;
    }
    /**
     * <code>map&lt;int32, double&gt; value_per_partition = 5;</code>
     */
    public Builder putAllValuePerPartition(
        java.util.Map<java.lang.Integer, java.lang.Double> values) {
      internalGetMutableValuePerPartition().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000008;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.MetricRun)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.MetricRun)
  private static final io.littlehorse.sdk.common.proto.MetricRun DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.MetricRun();
  }

  public static io.littlehorse.sdk.common.proto.MetricRun getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MetricRun>
      PARSER = new com.google.protobuf.AbstractParser<MetricRun>() {
    @java.lang.Override
    public MetricRun parsePartialFrom(
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

  public static com.google.protobuf.Parser<MetricRun> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MetricRun> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricRun getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


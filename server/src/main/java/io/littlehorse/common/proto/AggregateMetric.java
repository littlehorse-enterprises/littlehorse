// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.AggregateMetric}
 */
public final class AggregateMetric extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.AggregateMetric)
    AggregateMetricOrBuilder {
private static final long serialVersionUID = 0L;
  // Use AggregateMetric.newBuilder() to construct.
  private AggregateMetric(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private AggregateMetric() {
    statusChanged_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new AggregateMetric();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateMetric_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateMetric_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.AggregateMetric.class, io.littlehorse.common.proto.AggregateMetric.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.AggregatedMetricId id_;
  /**
   * <code>.littlehorse.AggregatedMetricId id = 1;</code>
   * @return Whether the id field is set.
   */
  @java.lang.Override
  public boolean hasId() {
    return id_ != null;
  }
  /**
   * <code>.littlehorse.AggregatedMetricId id = 1;</code>
   * @return The id.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.AggregatedMetricId getId() {
    return id_ == null ? io.littlehorse.sdk.common.proto.AggregatedMetricId.getDefaultInstance() : id_;
  }
  /**
   * <code>.littlehorse.AggregatedMetricId id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.AggregatedMetricIdOrBuilder getIdOrBuilder() {
    return id_ == null ? io.littlehorse.sdk.common.proto.AggregatedMetricId.getDefaultInstance() : id_;
  }

  public static final int STATUS_CHANGED_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.StatusChanged> statusChanged_;
  /**
   * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.StatusChanged> getStatusChangedList() {
    return statusChanged_;
  }
  /**
   * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.StatusChangedOrBuilder> 
      getStatusChangedOrBuilderList() {
    return statusChanged_;
  }
  /**
   * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
   */
  @java.lang.Override
  public int getStatusChangedCount() {
    return statusChanged_.size();
  }
  /**
   * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StatusChanged getStatusChanged(int index) {
    return statusChanged_.get(index);
  }
  /**
   * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.StatusChangedOrBuilder getStatusChangedOrBuilder(
      int index) {
    return statusChanged_.get(index);
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
    for (int i = 0; i < statusChanged_.size(); i++) {
      output.writeMessage(2, statusChanged_.get(i));
    }
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
    for (int i = 0; i < statusChanged_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, statusChanged_.get(i));
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
    if (!(obj instanceof io.littlehorse.common.proto.AggregateMetric)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.AggregateMetric other = (io.littlehorse.common.proto.AggregateMetric) obj;

    if (hasId() != other.hasId()) return false;
    if (hasId()) {
      if (!getId()
          .equals(other.getId())) return false;
    }
    if (!getStatusChangedList()
        .equals(other.getStatusChangedList())) return false;
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
    if (getStatusChangedCount() > 0) {
      hash = (37 * hash) + STATUS_CHANGED_FIELD_NUMBER;
      hash = (53 * hash) + getStatusChangedList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.AggregateMetric parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.AggregateMetric parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.AggregateMetric parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.AggregateMetric prototype) {
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
   * Protobuf type {@code littlehorse.AggregateMetric}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.AggregateMetric)
      io.littlehorse.common.proto.AggregateMetricOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateMetric_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateMetric_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.AggregateMetric.class, io.littlehorse.common.proto.AggregateMetric.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.AggregateMetric.newBuilder()
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
      if (statusChangedBuilder_ == null) {
        statusChanged_ = java.util.Collections.emptyList();
      } else {
        statusChanged_ = null;
        statusChangedBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateMetric_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateMetric getDefaultInstanceForType() {
      return io.littlehorse.common.proto.AggregateMetric.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateMetric build() {
      io.littlehorse.common.proto.AggregateMetric result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateMetric buildPartial() {
      io.littlehorse.common.proto.AggregateMetric result = new io.littlehorse.common.proto.AggregateMetric(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.AggregateMetric result) {
      if (statusChangedBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0)) {
          statusChanged_ = java.util.Collections.unmodifiableList(statusChanged_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.statusChanged_ = statusChanged_;
      } else {
        result.statusChanged_ = statusChangedBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.AggregateMetric result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.id_ = idBuilder_ == null
            ? id_
            : idBuilder_.build();
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
      if (other instanceof io.littlehorse.common.proto.AggregateMetric) {
        return mergeFrom((io.littlehorse.common.proto.AggregateMetric)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.AggregateMetric other) {
      if (other == io.littlehorse.common.proto.AggregateMetric.getDefaultInstance()) return this;
      if (other.hasId()) {
        mergeId(other.getId());
      }
      if (statusChangedBuilder_ == null) {
        if (!other.statusChanged_.isEmpty()) {
          if (statusChanged_.isEmpty()) {
            statusChanged_ = other.statusChanged_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureStatusChangedIsMutable();
            statusChanged_.addAll(other.statusChanged_);
          }
          onChanged();
        }
      } else {
        if (!other.statusChanged_.isEmpty()) {
          if (statusChangedBuilder_.isEmpty()) {
            statusChangedBuilder_.dispose();
            statusChangedBuilder_ = null;
            statusChanged_ = other.statusChanged_;
            bitField0_ = (bitField0_ & ~0x00000002);
            statusChangedBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getStatusChangedFieldBuilder() : null;
          } else {
            statusChangedBuilder_.addAllMessages(other.statusChanged_);
          }
        }
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
              input.readMessage(
                  getIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              io.littlehorse.common.proto.StatusChanged m =
                  input.readMessage(
                      io.littlehorse.common.proto.StatusChanged.parser(),
                      extensionRegistry);
              if (statusChangedBuilder_ == null) {
                ensureStatusChangedIsMutable();
                statusChanged_.add(m);
              } else {
                statusChangedBuilder_.addMessage(m);
              }
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

    private io.littlehorse.sdk.common.proto.AggregatedMetricId id_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.AggregatedMetricId, io.littlehorse.sdk.common.proto.AggregatedMetricId.Builder, io.littlehorse.sdk.common.proto.AggregatedMetricIdOrBuilder> idBuilder_;
    /**
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     * @return The id.
     */
    public io.littlehorse.sdk.common.proto.AggregatedMetricId getId() {
      if (idBuilder_ == null) {
        return id_ == null ? io.littlehorse.sdk.common.proto.AggregatedMetricId.getDefaultInstance() : id_;
      } else {
        return idBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    public Builder setId(io.littlehorse.sdk.common.proto.AggregatedMetricId value) {
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
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    public Builder setId(
        io.littlehorse.sdk.common.proto.AggregatedMetricId.Builder builderForValue) {
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
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    public Builder mergeId(io.littlehorse.sdk.common.proto.AggregatedMetricId value) {
      if (idBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          id_ != null &&
          id_ != io.littlehorse.sdk.common.proto.AggregatedMetricId.getDefaultInstance()) {
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
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
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
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.AggregatedMetricId.Builder getIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.AggregatedMetricIdOrBuilder getIdOrBuilder() {
      if (idBuilder_ != null) {
        return idBuilder_.getMessageOrBuilder();
      } else {
        return id_ == null ?
            io.littlehorse.sdk.common.proto.AggregatedMetricId.getDefaultInstance() : id_;
      }
    }
    /**
     * <code>.littlehorse.AggregatedMetricId id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.AggregatedMetricId, io.littlehorse.sdk.common.proto.AggregatedMetricId.Builder, io.littlehorse.sdk.common.proto.AggregatedMetricIdOrBuilder> 
        getIdFieldBuilder() {
      if (idBuilder_ == null) {
        idBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.AggregatedMetricId, io.littlehorse.sdk.common.proto.AggregatedMetricId.Builder, io.littlehorse.sdk.common.proto.AggregatedMetricIdOrBuilder>(
                getId(),
                getParentForChildren(),
                isClean());
        id_ = null;
      }
      return idBuilder_;
    }

    private java.util.List<io.littlehorse.common.proto.StatusChanged> statusChanged_ =
      java.util.Collections.emptyList();
    private void ensureStatusChangedIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        statusChanged_ = new java.util.ArrayList<io.littlehorse.common.proto.StatusChanged>(statusChanged_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder> statusChangedBuilder_;

    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StatusChanged> getStatusChangedList() {
      if (statusChangedBuilder_ == null) {
        return java.util.Collections.unmodifiableList(statusChanged_);
      } else {
        return statusChangedBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public int getStatusChangedCount() {
      if (statusChangedBuilder_ == null) {
        return statusChanged_.size();
      } else {
        return statusChangedBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public io.littlehorse.common.proto.StatusChanged getStatusChanged(int index) {
      if (statusChangedBuilder_ == null) {
        return statusChanged_.get(index);
      } else {
        return statusChangedBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder setStatusChanged(
        int index, io.littlehorse.common.proto.StatusChanged value) {
      if (statusChangedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStatusChangedIsMutable();
        statusChanged_.set(index, value);
        onChanged();
      } else {
        statusChangedBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder setStatusChanged(
        int index, io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (statusChangedBuilder_ == null) {
        ensureStatusChangedIsMutable();
        statusChanged_.set(index, builderForValue.build());
        onChanged();
      } else {
        statusChangedBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder addStatusChanged(io.littlehorse.common.proto.StatusChanged value) {
      if (statusChangedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStatusChangedIsMutable();
        statusChanged_.add(value);
        onChanged();
      } else {
        statusChangedBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder addStatusChanged(
        int index, io.littlehorse.common.proto.StatusChanged value) {
      if (statusChangedBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureStatusChangedIsMutable();
        statusChanged_.add(index, value);
        onChanged();
      } else {
        statusChangedBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder addStatusChanged(
        io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (statusChangedBuilder_ == null) {
        ensureStatusChangedIsMutable();
        statusChanged_.add(builderForValue.build());
        onChanged();
      } else {
        statusChangedBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder addStatusChanged(
        int index, io.littlehorse.common.proto.StatusChanged.Builder builderForValue) {
      if (statusChangedBuilder_ == null) {
        ensureStatusChangedIsMutable();
        statusChanged_.add(index, builderForValue.build());
        onChanged();
      } else {
        statusChangedBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder addAllStatusChanged(
        java.lang.Iterable<? extends io.littlehorse.common.proto.StatusChanged> values) {
      if (statusChangedBuilder_ == null) {
        ensureStatusChangedIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, statusChanged_);
        onChanged();
      } else {
        statusChangedBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder clearStatusChanged() {
      if (statusChangedBuilder_ == null) {
        statusChanged_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        statusChangedBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public Builder removeStatusChanged(int index) {
      if (statusChangedBuilder_ == null) {
        ensureStatusChangedIsMutable();
        statusChanged_.remove(index);
        onChanged();
      } else {
        statusChangedBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder getStatusChangedBuilder(
        int index) {
      return getStatusChangedFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public io.littlehorse.common.proto.StatusChangedOrBuilder getStatusChangedOrBuilder(
        int index) {
      if (statusChangedBuilder_ == null) {
        return statusChanged_.get(index);  } else {
        return statusChangedBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.StatusChangedOrBuilder> 
         getStatusChangedOrBuilderList() {
      if (statusChangedBuilder_ != null) {
        return statusChangedBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(statusChanged_);
      }
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder addStatusChangedBuilder() {
      return getStatusChangedFieldBuilder().addBuilder(
          io.littlehorse.common.proto.StatusChanged.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public io.littlehorse.common.proto.StatusChanged.Builder addStatusChangedBuilder(
        int index) {
      return getStatusChangedFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.StatusChanged.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.StatusChanged status_changed = 2;</code>
     */
    public java.util.List<io.littlehorse.common.proto.StatusChanged.Builder> 
         getStatusChangedBuilderList() {
      return getStatusChangedFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder> 
        getStatusChangedFieldBuilder() {
      if (statusChangedBuilder_ == null) {
        statusChangedBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.common.proto.StatusChanged, io.littlehorse.common.proto.StatusChanged.Builder, io.littlehorse.common.proto.StatusChangedOrBuilder>(
                statusChanged_,
                ((bitField0_ & 0x00000002) != 0),
                getParentForChildren(),
                isClean());
        statusChanged_ = null;
      }
      return statusChangedBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.AggregateMetric)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.AggregateMetric)
  private static final io.littlehorse.common.proto.AggregateMetric DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.AggregateMetric();
  }

  public static io.littlehorse.common.proto.AggregateMetric getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AggregateMetric>
      PARSER = new com.google.protobuf.AbstractParser<AggregateMetric>() {
    @java.lang.Override
    public AggregateMetric parsePartialFrom(
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

  public static com.google.protobuf.Parser<AggregateMetric> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AggregateMetric> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.AggregateMetric getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


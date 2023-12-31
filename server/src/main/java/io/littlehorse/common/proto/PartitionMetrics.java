// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: command.proto

package io.littlehorse.common.proto;

/**
 * Protobuf type {@code littlehorse.PartitionMetrics}
 */
public final class PartitionMetrics extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.PartitionMetrics)
    PartitionMetricsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PartitionMetrics.newBuilder() to construct.
  private PartitionMetrics(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PartitionMetrics() {
    metricsByTenant_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PartitionMetrics();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_PartitionMetrics_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_PartitionMetrics_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.PartitionMetrics.class, io.littlehorse.common.proto.PartitionMetrics.Builder.class);
  }

  public static final int METRICS_BY_TENANT_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.MetricsByTenant> metricsByTenant_;
  /**
   * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.MetricsByTenant> getMetricsByTenantList() {
    return metricsByTenant_;
  }
  /**
   * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.MetricsByTenantOrBuilder> 
      getMetricsByTenantOrBuilderList() {
    return metricsByTenant_;
  }
  /**
   * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
   */
  @java.lang.Override
  public int getMetricsByTenantCount() {
    return metricsByTenant_.size();
  }
  /**
   * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.MetricsByTenant getMetricsByTenant(int index) {
    return metricsByTenant_.get(index);
  }
  /**
   * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.MetricsByTenantOrBuilder getMetricsByTenantOrBuilder(
      int index) {
    return metricsByTenant_.get(index);
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
    for (int i = 0; i < metricsByTenant_.size(); i++) {
      output.writeMessage(1, metricsByTenant_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < metricsByTenant_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, metricsByTenant_.get(i));
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
    if (!(obj instanceof io.littlehorse.common.proto.PartitionMetrics)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.PartitionMetrics other = (io.littlehorse.common.proto.PartitionMetrics) obj;

    if (!getMetricsByTenantList()
        .equals(other.getMetricsByTenantList())) return false;
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
    if (getMetricsByTenantCount() > 0) {
      hash = (37 * hash) + METRICS_BY_TENANT_FIELD_NUMBER;
      hash = (53 * hash) + getMetricsByTenantList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.PartitionMetrics parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.PartitionMetrics parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.PartitionMetrics parseFrom(
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
  public static Builder newBuilder(io.littlehorse.common.proto.PartitionMetrics prototype) {
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
   * Protobuf type {@code littlehorse.PartitionMetrics}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.PartitionMetrics)
      io.littlehorse.common.proto.PartitionMetricsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_PartitionMetrics_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_PartitionMetrics_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.PartitionMetrics.class, io.littlehorse.common.proto.PartitionMetrics.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.PartitionMetrics.newBuilder()
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
      if (metricsByTenantBuilder_ == null) {
        metricsByTenant_ = java.util.Collections.emptyList();
      } else {
        metricsByTenant_ = null;
        metricsByTenantBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_PartitionMetrics_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionMetrics getDefaultInstanceForType() {
      return io.littlehorse.common.proto.PartitionMetrics.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionMetrics build() {
      io.littlehorse.common.proto.PartitionMetrics result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.PartitionMetrics buildPartial() {
      io.littlehorse.common.proto.PartitionMetrics result = new io.littlehorse.common.proto.PartitionMetrics(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.PartitionMetrics result) {
      if (metricsByTenantBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          metricsByTenant_ = java.util.Collections.unmodifiableList(metricsByTenant_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.metricsByTenant_ = metricsByTenant_;
      } else {
        result.metricsByTenant_ = metricsByTenantBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.PartitionMetrics result) {
      int from_bitField0_ = bitField0_;
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
      if (other instanceof io.littlehorse.common.proto.PartitionMetrics) {
        return mergeFrom((io.littlehorse.common.proto.PartitionMetrics)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.PartitionMetrics other) {
      if (other == io.littlehorse.common.proto.PartitionMetrics.getDefaultInstance()) return this;
      if (metricsByTenantBuilder_ == null) {
        if (!other.metricsByTenant_.isEmpty()) {
          if (metricsByTenant_.isEmpty()) {
            metricsByTenant_ = other.metricsByTenant_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureMetricsByTenantIsMutable();
            metricsByTenant_.addAll(other.metricsByTenant_);
          }
          onChanged();
        }
      } else {
        if (!other.metricsByTenant_.isEmpty()) {
          if (metricsByTenantBuilder_.isEmpty()) {
            metricsByTenantBuilder_.dispose();
            metricsByTenantBuilder_ = null;
            metricsByTenant_ = other.metricsByTenant_;
            bitField0_ = (bitField0_ & ~0x00000001);
            metricsByTenantBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getMetricsByTenantFieldBuilder() : null;
          } else {
            metricsByTenantBuilder_.addAllMessages(other.metricsByTenant_);
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
              io.littlehorse.common.proto.MetricsByTenant m =
                  input.readMessage(
                      io.littlehorse.common.proto.MetricsByTenant.parser(),
                      extensionRegistry);
              if (metricsByTenantBuilder_ == null) {
                ensureMetricsByTenantIsMutable();
                metricsByTenant_.add(m);
              } else {
                metricsByTenantBuilder_.addMessage(m);
              }
              break;
            } // case 10
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

    private java.util.List<io.littlehorse.common.proto.MetricsByTenant> metricsByTenant_ =
      java.util.Collections.emptyList();
    private void ensureMetricsByTenantIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        metricsByTenant_ = new java.util.ArrayList<io.littlehorse.common.proto.MetricsByTenant>(metricsByTenant_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.MetricsByTenant, io.littlehorse.common.proto.MetricsByTenant.Builder, io.littlehorse.common.proto.MetricsByTenantOrBuilder> metricsByTenantBuilder_;

    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.MetricsByTenant> getMetricsByTenantList() {
      if (metricsByTenantBuilder_ == null) {
        return java.util.Collections.unmodifiableList(metricsByTenant_);
      } else {
        return metricsByTenantBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public int getMetricsByTenantCount() {
      if (metricsByTenantBuilder_ == null) {
        return metricsByTenant_.size();
      } else {
        return metricsByTenantBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public io.littlehorse.common.proto.MetricsByTenant getMetricsByTenant(int index) {
      if (metricsByTenantBuilder_ == null) {
        return metricsByTenant_.get(index);
      } else {
        return metricsByTenantBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder setMetricsByTenant(
        int index, io.littlehorse.common.proto.MetricsByTenant value) {
      if (metricsByTenantBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.set(index, value);
        onChanged();
      } else {
        metricsByTenantBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder setMetricsByTenant(
        int index, io.littlehorse.common.proto.MetricsByTenant.Builder builderForValue) {
      if (metricsByTenantBuilder_ == null) {
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.set(index, builderForValue.build());
        onChanged();
      } else {
        metricsByTenantBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder addMetricsByTenant(io.littlehorse.common.proto.MetricsByTenant value) {
      if (metricsByTenantBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.add(value);
        onChanged();
      } else {
        metricsByTenantBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder addMetricsByTenant(
        int index, io.littlehorse.common.proto.MetricsByTenant value) {
      if (metricsByTenantBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.add(index, value);
        onChanged();
      } else {
        metricsByTenantBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder addMetricsByTenant(
        io.littlehorse.common.proto.MetricsByTenant.Builder builderForValue) {
      if (metricsByTenantBuilder_ == null) {
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.add(builderForValue.build());
        onChanged();
      } else {
        metricsByTenantBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder addMetricsByTenant(
        int index, io.littlehorse.common.proto.MetricsByTenant.Builder builderForValue) {
      if (metricsByTenantBuilder_ == null) {
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.add(index, builderForValue.build());
        onChanged();
      } else {
        metricsByTenantBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder addAllMetricsByTenant(
        java.lang.Iterable<? extends io.littlehorse.common.proto.MetricsByTenant> values) {
      if (metricsByTenantBuilder_ == null) {
        ensureMetricsByTenantIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, metricsByTenant_);
        onChanged();
      } else {
        metricsByTenantBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder clearMetricsByTenant() {
      if (metricsByTenantBuilder_ == null) {
        metricsByTenant_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        metricsByTenantBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public Builder removeMetricsByTenant(int index) {
      if (metricsByTenantBuilder_ == null) {
        ensureMetricsByTenantIsMutable();
        metricsByTenant_.remove(index);
        onChanged();
      } else {
        metricsByTenantBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public io.littlehorse.common.proto.MetricsByTenant.Builder getMetricsByTenantBuilder(
        int index) {
      return getMetricsByTenantFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public io.littlehorse.common.proto.MetricsByTenantOrBuilder getMetricsByTenantOrBuilder(
        int index) {
      if (metricsByTenantBuilder_ == null) {
        return metricsByTenant_.get(index);  } else {
        return metricsByTenantBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.MetricsByTenantOrBuilder> 
         getMetricsByTenantOrBuilderList() {
      if (metricsByTenantBuilder_ != null) {
        return metricsByTenantBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(metricsByTenant_);
      }
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public io.littlehorse.common.proto.MetricsByTenant.Builder addMetricsByTenantBuilder() {
      return getMetricsByTenantFieldBuilder().addBuilder(
          io.littlehorse.common.proto.MetricsByTenant.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public io.littlehorse.common.proto.MetricsByTenant.Builder addMetricsByTenantBuilder(
        int index) {
      return getMetricsByTenantFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.MetricsByTenant.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.MetricsByTenant metrics_by_tenant = 1;</code>
     */
    public java.util.List<io.littlehorse.common.proto.MetricsByTenant.Builder> 
         getMetricsByTenantBuilderList() {
      return getMetricsByTenantFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.littlehorse.common.proto.MetricsByTenant, io.littlehorse.common.proto.MetricsByTenant.Builder, io.littlehorse.common.proto.MetricsByTenantOrBuilder> 
        getMetricsByTenantFieldBuilder() {
      if (metricsByTenantBuilder_ == null) {
        metricsByTenantBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.littlehorse.common.proto.MetricsByTenant, io.littlehorse.common.proto.MetricsByTenant.Builder, io.littlehorse.common.proto.MetricsByTenantOrBuilder>(
                metricsByTenant_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        metricsByTenant_ = null;
      }
      return metricsByTenantBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.PartitionMetrics)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.PartitionMetrics)
  private static final io.littlehorse.common.proto.PartitionMetrics DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.PartitionMetrics();
  }

  public static io.littlehorse.common.proto.PartitionMetrics getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PartitionMetrics>
      PARSER = new com.google.protobuf.AbstractParser<PartitionMetrics>() {
    @java.lang.Override
    public PartitionMetrics parsePartialFrom(
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

  public static com.google.protobuf.Parser<PartitionMetrics> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PartitionMetrics> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.PartitionMetrics getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: command.proto
// Protobuf Java Version: 4.30.1

package io.littlehorse.common.proto;

/**
 * <pre>
 * Repartition subcommand
 * </pre>
 *
 * Protobuf type {@code littlehorse.AggregateWfMetrics}
 */
public final class AggregateWfMetrics extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:littlehorse.AggregateWfMetrics)
    AggregateWfMetricsOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 30,
      /* patch= */ 1,
      /* suffix= */ "",
      AggregateWfMetrics.class.getName());
  }
  // Use AggregateWfMetrics.newBuilder() to construct.
  private AggregateWfMetrics(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private AggregateWfMetrics() {
    metricUpdates_ = java.util.Collections.emptyList();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateWfMetrics_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateWfMetrics_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.common.proto.AggregateWfMetrics.class, io.littlehorse.common.proto.AggregateWfMetrics.Builder.class);
  }

  private int bitField0_;
  public static final int WF_SPEC_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WfSpecId wfSpecId_;
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return Whether the wfSpecId field is set.
   */
  @java.lang.Override
  public boolean hasWfSpecId() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return The wfSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }
  /**
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }

  public static final int TENANT_ID_FIELD_NUMBER = 2;
  private io.littlehorse.sdk.common.proto.TenantId tenantId_;
  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   * @return Whether the tenantId field is set.
   */
  @java.lang.Override
  public boolean hasTenantId() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   * @return The tenantId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TenantId getTenantId() {
    return tenantId_ == null ? io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance() : tenantId_;
  }
  /**
   * <code>.littlehorse.TenantId tenant_id = 2;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.TenantIdOrBuilder getTenantIdOrBuilder() {
    return tenantId_ == null ? io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance() : tenantId_;
  }

  public static final int METRIC_UPDATES_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private java.util.List<io.littlehorse.common.proto.WfMetricUpdate> metricUpdates_;
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  @java.lang.Override
  public java.util.List<io.littlehorse.common.proto.WfMetricUpdate> getMetricUpdatesList() {
    return metricUpdates_;
  }
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  @java.lang.Override
  public java.util.List<? extends io.littlehorse.common.proto.WfMetricUpdateOrBuilder> 
      getMetricUpdatesOrBuilderList() {
    return metricUpdates_;
  }
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  @java.lang.Override
  public int getMetricUpdatesCount() {
    return metricUpdates_.size();
  }
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.WfMetricUpdate getMetricUpdates(int index) {
    return metricUpdates_.get(index);
  }
  /**
   * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
   */
  @java.lang.Override
  public io.littlehorse.common.proto.WfMetricUpdateOrBuilder getMetricUpdatesOrBuilder(
      int index) {
    return metricUpdates_.get(index);
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
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(1, getWfSpecId());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeMessage(2, getTenantId());
    }
    for (int i = 0; i < metricUpdates_.size(); i++) {
      output.writeMessage(3, metricUpdates_.get(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getWfSpecId());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getTenantId());
    }
    for (int i = 0; i < metricUpdates_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, metricUpdates_.get(i));
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
    if (!(obj instanceof io.littlehorse.common.proto.AggregateWfMetrics)) {
      return super.equals(obj);
    }
    io.littlehorse.common.proto.AggregateWfMetrics other = (io.littlehorse.common.proto.AggregateWfMetrics) obj;

    if (hasWfSpecId() != other.hasWfSpecId()) return false;
    if (hasWfSpecId()) {
      if (!getWfSpecId()
          .equals(other.getWfSpecId())) return false;
    }
    if (hasTenantId() != other.hasTenantId()) return false;
    if (hasTenantId()) {
      if (!getTenantId()
          .equals(other.getTenantId())) return false;
    }
    if (!getMetricUpdatesList()
        .equals(other.getMetricUpdatesList())) return false;
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
    if (hasWfSpecId()) {
      hash = (37 * hash) + WF_SPEC_ID_FIELD_NUMBER;
      hash = (53 * hash) + getWfSpecId().hashCode();
    }
    if (hasTenantId()) {
      hash = (37 * hash) + TENANT_ID_FIELD_NUMBER;
      hash = (53 * hash) + getTenantId().hashCode();
    }
    if (getMetricUpdatesCount() > 0) {
      hash = (37 * hash) + METRIC_UPDATES_FIELD_NUMBER;
      hash = (53 * hash) + getMetricUpdatesList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.common.proto.AggregateWfMetrics parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.common.proto.AggregateWfMetrics parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.common.proto.AggregateWfMetrics parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.littlehorse.common.proto.AggregateWfMetrics prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * Repartition subcommand
   * </pre>
   *
   * Protobuf type {@code littlehorse.AggregateWfMetrics}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.AggregateWfMetrics)
      io.littlehorse.common.proto.AggregateWfMetricsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateWfMetrics_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateWfMetrics_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.common.proto.AggregateWfMetrics.class, io.littlehorse.common.proto.AggregateWfMetrics.Builder.class);
    }

    // Construct using io.littlehorse.common.proto.AggregateWfMetrics.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessage
              .alwaysUseFieldBuilders) {
        internalGetWfSpecIdFieldBuilder();
        internalGetTenantIdFieldBuilder();
        internalGetMetricUpdatesFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      wfSpecId_ = null;
      if (wfSpecIdBuilder_ != null) {
        wfSpecIdBuilder_.dispose();
        wfSpecIdBuilder_ = null;
      }
      tenantId_ = null;
      if (tenantIdBuilder_ != null) {
        tenantIdBuilder_.dispose();
        tenantIdBuilder_ = null;
      }
      if (metricUpdatesBuilder_ == null) {
        metricUpdates_ = java.util.Collections.emptyList();
      } else {
        metricUpdates_ = null;
        metricUpdatesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000004);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.common.proto.CommandOuterClass.internal_static_littlehorse_AggregateWfMetrics_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateWfMetrics getDefaultInstanceForType() {
      return io.littlehorse.common.proto.AggregateWfMetrics.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateWfMetrics build() {
      io.littlehorse.common.proto.AggregateWfMetrics result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.common.proto.AggregateWfMetrics buildPartial() {
      io.littlehorse.common.proto.AggregateWfMetrics result = new io.littlehorse.common.proto.AggregateWfMetrics(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(io.littlehorse.common.proto.AggregateWfMetrics result) {
      if (metricUpdatesBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0)) {
          metricUpdates_ = java.util.Collections.unmodifiableList(metricUpdates_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.metricUpdates_ = metricUpdates_;
      } else {
        result.metricUpdates_ = metricUpdatesBuilder_.build();
      }
    }

    private void buildPartial0(io.littlehorse.common.proto.AggregateWfMetrics result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfSpecId_ = wfSpecIdBuilder_ == null
            ? wfSpecId_
            : wfSpecIdBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.tenantId_ = tenantIdBuilder_ == null
            ? tenantId_
            : tenantIdBuilder_.build();
        to_bitField0_ |= 0x00000002;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.littlehorse.common.proto.AggregateWfMetrics) {
        return mergeFrom((io.littlehorse.common.proto.AggregateWfMetrics)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.common.proto.AggregateWfMetrics other) {
      if (other == io.littlehorse.common.proto.AggregateWfMetrics.getDefaultInstance()) return this;
      if (other.hasWfSpecId()) {
        mergeWfSpecId(other.getWfSpecId());
      }
      if (other.hasTenantId()) {
        mergeTenantId(other.getTenantId());
      }
      if (metricUpdatesBuilder_ == null) {
        if (!other.metricUpdates_.isEmpty()) {
          if (metricUpdates_.isEmpty()) {
            metricUpdates_ = other.metricUpdates_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureMetricUpdatesIsMutable();
            metricUpdates_.addAll(other.metricUpdates_);
          }
          onChanged();
        }
      } else {
        if (!other.metricUpdates_.isEmpty()) {
          if (metricUpdatesBuilder_.isEmpty()) {
            metricUpdatesBuilder_.dispose();
            metricUpdatesBuilder_ = null;
            metricUpdates_ = other.metricUpdates_;
            bitField0_ = (bitField0_ & ~0x00000004);
            metricUpdatesBuilder_ = 
              com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                 internalGetMetricUpdatesFieldBuilder() : null;
          } else {
            metricUpdatesBuilder_.addAllMessages(other.metricUpdates_);
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
                  internalGetWfSpecIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  internalGetTenantIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              io.littlehorse.common.proto.WfMetricUpdate m =
                  input.readMessage(
                      io.littlehorse.common.proto.WfMetricUpdate.parser(),
                      extensionRegistry);
              if (metricUpdatesBuilder_ == null) {
                ensureMetricUpdatesIsMutable();
                metricUpdates_.add(m);
              } else {
                metricUpdatesBuilder_.addMessage(m);
              }
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

    private io.littlehorse.sdk.common.proto.WfSpecId wfSpecId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> wfSpecIdBuilder_;
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     * @return Whether the wfSpecId field is set.
     */
    public boolean hasWfSpecId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     * @return The wfSpecId.
     */
    public io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId() {
      if (wfSpecIdBuilder_ == null) {
        return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
      } else {
        return wfSpecIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public Builder setWfSpecId(io.littlehorse.sdk.common.proto.WfSpecId value) {
      if (wfSpecIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        wfSpecId_ = value;
      } else {
        wfSpecIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public Builder setWfSpecId(
        io.littlehorse.sdk.common.proto.WfSpecId.Builder builderForValue) {
      if (wfSpecIdBuilder_ == null) {
        wfSpecId_ = builderForValue.build();
      } else {
        wfSpecIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public Builder mergeWfSpecId(io.littlehorse.sdk.common.proto.WfSpecId value) {
      if (wfSpecIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          wfSpecId_ != null &&
          wfSpecId_ != io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance()) {
          getWfSpecIdBuilder().mergeFrom(value);
        } else {
          wfSpecId_ = value;
        }
      } else {
        wfSpecIdBuilder_.mergeFrom(value);
      }
      if (wfSpecId_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public Builder clearWfSpecId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      wfSpecId_ = null;
      if (wfSpecIdBuilder_ != null) {
        wfSpecIdBuilder_.dispose();
        wfSpecIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecId.Builder getWfSpecIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return internalGetWfSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder() {
      if (wfSpecIdBuilder_ != null) {
        return wfSpecIdBuilder_.getMessageOrBuilder();
      } else {
        return wfSpecId_ == null ?
            io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
      }
    }
    /**
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> 
        internalGetWfSpecIdFieldBuilder() {
      if (wfSpecIdBuilder_ == null) {
        wfSpecIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder>(
                getWfSpecId(),
                getParentForChildren(),
                isClean());
        wfSpecId_ = null;
      }
      return wfSpecIdBuilder_;
    }

    private io.littlehorse.sdk.common.proto.TenantId tenantId_;
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.TenantId, io.littlehorse.sdk.common.proto.TenantId.Builder, io.littlehorse.sdk.common.proto.TenantIdOrBuilder> tenantIdBuilder_;
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     * @return Whether the tenantId field is set.
     */
    public boolean hasTenantId() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     * @return The tenantId.
     */
    public io.littlehorse.sdk.common.proto.TenantId getTenantId() {
      if (tenantIdBuilder_ == null) {
        return tenantId_ == null ? io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance() : tenantId_;
      } else {
        return tenantIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public Builder setTenantId(io.littlehorse.sdk.common.proto.TenantId value) {
      if (tenantIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        tenantId_ = value;
      } else {
        tenantIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public Builder setTenantId(
        io.littlehorse.sdk.common.proto.TenantId.Builder builderForValue) {
      if (tenantIdBuilder_ == null) {
        tenantId_ = builderForValue.build();
      } else {
        tenantIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public Builder mergeTenantId(io.littlehorse.sdk.common.proto.TenantId value) {
      if (tenantIdBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          tenantId_ != null &&
          tenantId_ != io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance()) {
          getTenantIdBuilder().mergeFrom(value);
        } else {
          tenantId_ = value;
        }
      } else {
        tenantIdBuilder_.mergeFrom(value);
      }
      if (tenantId_ != null) {
        bitField0_ |= 0x00000002;
        onChanged();
      }
      return this;
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public Builder clearTenantId() {
      bitField0_ = (bitField0_ & ~0x00000002);
      tenantId_ = null;
      if (tenantIdBuilder_ != null) {
        tenantIdBuilder_.dispose();
        tenantIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.TenantId.Builder getTenantIdBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return internalGetTenantIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    public io.littlehorse.sdk.common.proto.TenantIdOrBuilder getTenantIdOrBuilder() {
      if (tenantIdBuilder_ != null) {
        return tenantIdBuilder_.getMessageOrBuilder();
      } else {
        return tenantId_ == null ?
            io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance() : tenantId_;
      }
    }
    /**
     * <code>.littlehorse.TenantId tenant_id = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilder<
        io.littlehorse.sdk.common.proto.TenantId, io.littlehorse.sdk.common.proto.TenantId.Builder, io.littlehorse.sdk.common.proto.TenantIdOrBuilder> 
        internalGetTenantIdFieldBuilder() {
      if (tenantIdBuilder_ == null) {
        tenantIdBuilder_ = new com.google.protobuf.SingleFieldBuilder<
            io.littlehorse.sdk.common.proto.TenantId, io.littlehorse.sdk.common.proto.TenantId.Builder, io.littlehorse.sdk.common.proto.TenantIdOrBuilder>(
                getTenantId(),
                getParentForChildren(),
                isClean());
        tenantId_ = null;
      }
      return tenantIdBuilder_;
    }

    private java.util.List<io.littlehorse.common.proto.WfMetricUpdate> metricUpdates_ =
      java.util.Collections.emptyList();
    private void ensureMetricUpdatesIsMutable() {
      if (!((bitField0_ & 0x00000004) != 0)) {
        metricUpdates_ = new java.util.ArrayList<io.littlehorse.common.proto.WfMetricUpdate>(metricUpdates_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilder<
        io.littlehorse.common.proto.WfMetricUpdate, io.littlehorse.common.proto.WfMetricUpdate.Builder, io.littlehorse.common.proto.WfMetricUpdateOrBuilder> metricUpdatesBuilder_;

    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public java.util.List<io.littlehorse.common.proto.WfMetricUpdate> getMetricUpdatesList() {
      if (metricUpdatesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(metricUpdates_);
      } else {
        return metricUpdatesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public int getMetricUpdatesCount() {
      if (metricUpdatesBuilder_ == null) {
        return metricUpdates_.size();
      } else {
        return metricUpdatesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public io.littlehorse.common.proto.WfMetricUpdate getMetricUpdates(int index) {
      if (metricUpdatesBuilder_ == null) {
        return metricUpdates_.get(index);
      } else {
        return metricUpdatesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder setMetricUpdates(
        int index, io.littlehorse.common.proto.WfMetricUpdate value) {
      if (metricUpdatesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricUpdatesIsMutable();
        metricUpdates_.set(index, value);
        onChanged();
      } else {
        metricUpdatesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder setMetricUpdates(
        int index, io.littlehorse.common.proto.WfMetricUpdate.Builder builderForValue) {
      if (metricUpdatesBuilder_ == null) {
        ensureMetricUpdatesIsMutable();
        metricUpdates_.set(index, builderForValue.build());
        onChanged();
      } else {
        metricUpdatesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder addMetricUpdates(io.littlehorse.common.proto.WfMetricUpdate value) {
      if (metricUpdatesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricUpdatesIsMutable();
        metricUpdates_.add(value);
        onChanged();
      } else {
        metricUpdatesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder addMetricUpdates(
        int index, io.littlehorse.common.proto.WfMetricUpdate value) {
      if (metricUpdatesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureMetricUpdatesIsMutable();
        metricUpdates_.add(index, value);
        onChanged();
      } else {
        metricUpdatesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder addMetricUpdates(
        io.littlehorse.common.proto.WfMetricUpdate.Builder builderForValue) {
      if (metricUpdatesBuilder_ == null) {
        ensureMetricUpdatesIsMutable();
        metricUpdates_.add(builderForValue.build());
        onChanged();
      } else {
        metricUpdatesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder addMetricUpdates(
        int index, io.littlehorse.common.proto.WfMetricUpdate.Builder builderForValue) {
      if (metricUpdatesBuilder_ == null) {
        ensureMetricUpdatesIsMutable();
        metricUpdates_.add(index, builderForValue.build());
        onChanged();
      } else {
        metricUpdatesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder addAllMetricUpdates(
        java.lang.Iterable<? extends io.littlehorse.common.proto.WfMetricUpdate> values) {
      if (metricUpdatesBuilder_ == null) {
        ensureMetricUpdatesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, metricUpdates_);
        onChanged();
      } else {
        metricUpdatesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder clearMetricUpdates() {
      if (metricUpdatesBuilder_ == null) {
        metricUpdates_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        metricUpdatesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public Builder removeMetricUpdates(int index) {
      if (metricUpdatesBuilder_ == null) {
        ensureMetricUpdatesIsMutable();
        metricUpdates_.remove(index);
        onChanged();
      } else {
        metricUpdatesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public io.littlehorse.common.proto.WfMetricUpdate.Builder getMetricUpdatesBuilder(
        int index) {
      return internalGetMetricUpdatesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public io.littlehorse.common.proto.WfMetricUpdateOrBuilder getMetricUpdatesOrBuilder(
        int index) {
      if (metricUpdatesBuilder_ == null) {
        return metricUpdates_.get(index);  } else {
        return metricUpdatesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public java.util.List<? extends io.littlehorse.common.proto.WfMetricUpdateOrBuilder> 
         getMetricUpdatesOrBuilderList() {
      if (metricUpdatesBuilder_ != null) {
        return metricUpdatesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(metricUpdates_);
      }
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public io.littlehorse.common.proto.WfMetricUpdate.Builder addMetricUpdatesBuilder() {
      return internalGetMetricUpdatesFieldBuilder().addBuilder(
          io.littlehorse.common.proto.WfMetricUpdate.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public io.littlehorse.common.proto.WfMetricUpdate.Builder addMetricUpdatesBuilder(
        int index) {
      return internalGetMetricUpdatesFieldBuilder().addBuilder(
          index, io.littlehorse.common.proto.WfMetricUpdate.getDefaultInstance());
    }
    /**
     * <code>repeated .littlehorse.WfMetricUpdate metric_updates = 3;</code>
     */
    public java.util.List<io.littlehorse.common.proto.WfMetricUpdate.Builder> 
         getMetricUpdatesBuilderList() {
      return internalGetMetricUpdatesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilder<
        io.littlehorse.common.proto.WfMetricUpdate, io.littlehorse.common.proto.WfMetricUpdate.Builder, io.littlehorse.common.proto.WfMetricUpdateOrBuilder> 
        internalGetMetricUpdatesFieldBuilder() {
      if (metricUpdatesBuilder_ == null) {
        metricUpdatesBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
            io.littlehorse.common.proto.WfMetricUpdate, io.littlehorse.common.proto.WfMetricUpdate.Builder, io.littlehorse.common.proto.WfMetricUpdateOrBuilder>(
                metricUpdates_,
                ((bitField0_ & 0x00000004) != 0),
                getParentForChildren(),
                isClean());
        metricUpdates_ = null;
      }
      return metricUpdatesBuilder_;
    }

    // @@protoc_insertion_point(builder_scope:littlehorse.AggregateWfMetrics)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.AggregateWfMetrics)
  private static final io.littlehorse.common.proto.AggregateWfMetrics DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.common.proto.AggregateWfMetrics();
  }

  public static io.littlehorse.common.proto.AggregateWfMetrics getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AggregateWfMetrics>
      PARSER = new com.google.protobuf.AbstractParser<AggregateWfMetrics>() {
    @java.lang.Override
    public AggregateWfMetrics parsePartialFrom(
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

  public static com.google.protobuf.Parser<AggregateWfMetrics> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AggregateWfMetrics> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.common.proto.AggregateWfMetrics getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


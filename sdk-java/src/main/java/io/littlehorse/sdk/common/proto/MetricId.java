// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: object_id.proto

package io.littlehorse.sdk.common.proto;

/**
 * Protobuf type {@code littlehorse.MetricId}
 */
public final class MetricId extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.MetricId)
    MetricIdOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MetricId.newBuilder() to construct.
  private MetricId(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MetricId() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MetricId();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_MetricId_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_MetricId_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.MetricId.class, io.littlehorse.sdk.common.proto.MetricId.Builder.class);
  }

  public static final int METRIC_SPEC_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.MetricSpecId metricSpecId_;
  /**
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   * @return Whether the metricSpecId field is set.
   */
  @java.lang.Override
  public boolean hasMetricSpecId() {
    return metricSpecId_ != null;
  }
  /**
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   * @return The metricSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricSpecId getMetricSpecId() {
    return metricSpecId_ == null ? io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
  }
  /**
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder getMetricSpecIdOrBuilder() {
    return metricSpecId_ == null ? io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
  }

  public static final int WINDOW_LENGTH_FIELD_NUMBER = 2;
  private com.google.protobuf.Duration windowLength_;
  /**
   * <code>.google.protobuf.Duration window_length = 2;</code>
   * @return Whether the windowLength field is set.
   */
  @java.lang.Override
  public boolean hasWindowLength() {
    return windowLength_ != null;
  }
  /**
   * <code>.google.protobuf.Duration window_length = 2;</code>
   * @return The windowLength.
   */
  @java.lang.Override
  public com.google.protobuf.Duration getWindowLength() {
    return windowLength_ == null ? com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
  }
  /**
   * <code>.google.protobuf.Duration window_length = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.DurationOrBuilder getWindowLengthOrBuilder() {
    return windowLength_ == null ? com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
  }

  public static final int WINDOW_START_FIELD_NUMBER = 3;
  private com.google.protobuf.Timestamp windowStart_;
  /**
   * <code>.google.protobuf.Timestamp window_start = 3;</code>
   * @return Whether the windowStart field is set.
   */
  @java.lang.Override
  public boolean hasWindowStart() {
    return windowStart_ != null;
  }
  /**
   * <code>.google.protobuf.Timestamp window_start = 3;</code>
   * @return The windowStart.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getWindowStart() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }
  /**
   * <code>.google.protobuf.Timestamp window_start = 3;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
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
    if (metricSpecId_ != null) {
      output.writeMessage(1, getMetricSpecId());
    }
    if (windowLength_ != null) {
      output.writeMessage(2, getWindowLength());
    }
    if (windowStart_ != null) {
      output.writeMessage(3, getWindowStart());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (metricSpecId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMetricSpecId());
    }
    if (windowLength_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getWindowLength());
    }
    if (windowStart_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getWindowStart());
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.MetricId)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.MetricId other = (io.littlehorse.sdk.common.proto.MetricId) obj;

    if (hasMetricSpecId() != other.hasMetricSpecId()) return false;
    if (hasMetricSpecId()) {
      if (!getMetricSpecId()
          .equals(other.getMetricSpecId())) return false;
    }
    if (hasWindowLength() != other.hasWindowLength()) return false;
    if (hasWindowLength()) {
      if (!getWindowLength()
          .equals(other.getWindowLength())) return false;
    }
    if (hasWindowStart() != other.hasWindowStart()) return false;
    if (hasWindowStart()) {
      if (!getWindowStart()
          .equals(other.getWindowStart())) return false;
    }
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
    if (hasMetricSpecId()) {
      hash = (37 * hash) + METRIC_SPEC_ID_FIELD_NUMBER;
      hash = (53 * hash) + getMetricSpecId().hashCode();
    }
    if (hasWindowLength()) {
      hash = (37 * hash) + WINDOW_LENGTH_FIELD_NUMBER;
      hash = (53 * hash) + getWindowLength().hashCode();
    }
    if (hasWindowStart()) {
      hash = (37 * hash) + WINDOW_START_FIELD_NUMBER;
      hash = (53 * hash) + getWindowStart().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.MetricId parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.MetricId parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.MetricId parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.MetricId prototype) {
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
   * Protobuf type {@code littlehorse.MetricId}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.MetricId)
      io.littlehorse.sdk.common.proto.MetricIdOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_MetricId_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_MetricId_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.MetricId.class, io.littlehorse.sdk.common.proto.MetricId.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.MetricId.newBuilder()
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
      metricSpecId_ = null;
      if (metricSpecIdBuilder_ != null) {
        metricSpecIdBuilder_.dispose();
        metricSpecIdBuilder_ = null;
      }
      windowLength_ = null;
      if (windowLengthBuilder_ != null) {
        windowLengthBuilder_.dispose();
        windowLengthBuilder_ = null;
      }
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.ObjectId.internal_static_littlehorse_MetricId_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricId getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.MetricId.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricId build() {
      io.littlehorse.sdk.common.proto.MetricId result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricId buildPartial() {
      io.littlehorse.sdk.common.proto.MetricId result = new io.littlehorse.sdk.common.proto.MetricId(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.MetricId result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.metricSpecId_ = metricSpecIdBuilder_ == null
            ? metricSpecId_
            : metricSpecIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.windowLength_ = windowLengthBuilder_ == null
            ? windowLength_
            : windowLengthBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.windowStart_ = windowStartBuilder_ == null
            ? windowStart_
            : windowStartBuilder_.build();
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
      if (other instanceof io.littlehorse.sdk.common.proto.MetricId) {
        return mergeFrom((io.littlehorse.sdk.common.proto.MetricId)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.MetricId other) {
      if (other == io.littlehorse.sdk.common.proto.MetricId.getDefaultInstance()) return this;
      if (other.hasMetricSpecId()) {
        mergeMetricSpecId(other.getMetricSpecId());
      }
      if (other.hasWindowLength()) {
        mergeWindowLength(other.getWindowLength());
      }
      if (other.hasWindowStart()) {
        mergeWindowStart(other.getWindowStart());
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
                  getMetricSpecIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getWindowLengthFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              input.readMessage(
                  getWindowStartFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000004;
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

    private io.littlehorse.sdk.common.proto.MetricSpecId metricSpecId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.MetricSpecId, io.littlehorse.sdk.common.proto.MetricSpecId.Builder, io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder> metricSpecIdBuilder_;
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     * @return Whether the metricSpecId field is set.
     */
    public boolean hasMetricSpecId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     * @return The metricSpecId.
     */
    public io.littlehorse.sdk.common.proto.MetricSpecId getMetricSpecId() {
      if (metricSpecIdBuilder_ == null) {
        return metricSpecId_ == null ? io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
      } else {
        return metricSpecIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public Builder setMetricSpecId(io.littlehorse.sdk.common.proto.MetricSpecId value) {
      if (metricSpecIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        metricSpecId_ = value;
      } else {
        metricSpecIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public Builder setMetricSpecId(
        io.littlehorse.sdk.common.proto.MetricSpecId.Builder builderForValue) {
      if (metricSpecIdBuilder_ == null) {
        metricSpecId_ = builderForValue.build();
      } else {
        metricSpecIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public Builder mergeMetricSpecId(io.littlehorse.sdk.common.proto.MetricSpecId value) {
      if (metricSpecIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          metricSpecId_ != null &&
          metricSpecId_ != io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance()) {
          getMetricSpecIdBuilder().mergeFrom(value);
        } else {
          metricSpecId_ = value;
        }
      } else {
        metricSpecIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public Builder clearMetricSpecId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      metricSpecId_ = null;
      if (metricSpecIdBuilder_ != null) {
        metricSpecIdBuilder_.dispose();
        metricSpecIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.MetricSpecId.Builder getMetricSpecIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getMetricSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder getMetricSpecIdOrBuilder() {
      if (metricSpecIdBuilder_ != null) {
        return metricSpecIdBuilder_.getMessageOrBuilder();
      } else {
        return metricSpecId_ == null ?
            io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
      }
    }
    /**
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.MetricSpecId, io.littlehorse.sdk.common.proto.MetricSpecId.Builder, io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder> 
        getMetricSpecIdFieldBuilder() {
      if (metricSpecIdBuilder_ == null) {
        metricSpecIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.MetricSpecId, io.littlehorse.sdk.common.proto.MetricSpecId.Builder, io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder>(
                getMetricSpecId(),
                getParentForChildren(),
                isClean());
        metricSpecId_ = null;
      }
      return metricSpecIdBuilder_;
    }

    private com.google.protobuf.Duration windowLength_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Duration, com.google.protobuf.Duration.Builder, com.google.protobuf.DurationOrBuilder> windowLengthBuilder_;
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     * @return Whether the windowLength field is set.
     */
    public boolean hasWindowLength() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     * @return The windowLength.
     */
    public com.google.protobuf.Duration getWindowLength() {
      if (windowLengthBuilder_ == null) {
        return windowLength_ == null ? com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
      } else {
        return windowLengthBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public Builder setWindowLength(com.google.protobuf.Duration value) {
      if (windowLengthBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        windowLength_ = value;
      } else {
        windowLengthBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public Builder setWindowLength(
        com.google.protobuf.Duration.Builder builderForValue) {
      if (windowLengthBuilder_ == null) {
        windowLength_ = builderForValue.build();
      } else {
        windowLengthBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public Builder mergeWindowLength(com.google.protobuf.Duration value) {
      if (windowLengthBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          windowLength_ != null &&
          windowLength_ != com.google.protobuf.Duration.getDefaultInstance()) {
          getWindowLengthBuilder().mergeFrom(value);
        } else {
          windowLength_ = value;
        }
      } else {
        windowLengthBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public Builder clearWindowLength() {
      bitField0_ = (bitField0_ & ~0x00000002);
      windowLength_ = null;
      if (windowLengthBuilder_ != null) {
        windowLengthBuilder_.dispose();
        windowLengthBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public com.google.protobuf.Duration.Builder getWindowLengthBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getWindowLengthFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public com.google.protobuf.DurationOrBuilder getWindowLengthOrBuilder() {
      if (windowLengthBuilder_ != null) {
        return windowLengthBuilder_.getMessageOrBuilder();
      } else {
        return windowLength_ == null ?
            com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
      }
    }
    /**
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Duration, com.google.protobuf.Duration.Builder, com.google.protobuf.DurationOrBuilder> 
        getWindowLengthFieldBuilder() {
      if (windowLengthBuilder_ == null) {
        windowLengthBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Duration, com.google.protobuf.Duration.Builder, com.google.protobuf.DurationOrBuilder>(
                getWindowLength(),
                getParentForChildren(),
                isClean());
        windowLength_ = null;
      }
      return windowLengthBuilder_;
    }

    private com.google.protobuf.Timestamp windowStart_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> windowStartBuilder_;
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     * @return Whether the windowStart field is set.
     */
    public boolean hasWindowStart() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     * @return The windowStart.
     */
    public com.google.protobuf.Timestamp getWindowStart() {
      if (windowStartBuilder_ == null) {
        return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      } else {
        return windowStartBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public Builder setWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        windowStart_ = value;
      } else {
        windowStartBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public Builder setWindowStart(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (windowStartBuilder_ == null) {
        windowStart_ = builderForValue.build();
      } else {
        windowStartBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public Builder mergeWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (((bitField0_ & 0x00000004) != 0) &&
          windowStart_ != null &&
          windowStart_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getWindowStartBuilder().mergeFrom(value);
        } else {
          windowStart_ = value;
        }
      } else {
        windowStartBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public Builder clearWindowStart() {
      bitField0_ = (bitField0_ & ~0x00000004);
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public com.google.protobuf.Timestamp.Builder getWindowStartBuilder() {
      bitField0_ |= 0x00000004;
      onChanged();
      return getWindowStartFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
      if (windowStartBuilder_ != null) {
        return windowStartBuilder_.getMessageOrBuilder();
      } else {
        return windowStart_ == null ?
            com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
      }
    }
    /**
     * <code>.google.protobuf.Timestamp window_start = 3;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> 
        getWindowStartFieldBuilder() {
      if (windowStartBuilder_ == null) {
        windowStartBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder>(
                getWindowStart(),
                getParentForChildren(),
                isClean());
        windowStart_ = null;
      }
      return windowStartBuilder_;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.MetricId)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.MetricId)
  private static final io.littlehorse.sdk.common.proto.MetricId DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.MetricId();
  }

  public static io.littlehorse.sdk.common.proto.MetricId getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MetricId>
      PARSER = new com.google.protobuf.AbstractParser<MetricId>() {
    @java.lang.Override
    public MetricId parsePartialFrom(
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

  public static com.google.protobuf.Parser<MetricId> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MetricId> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricId getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


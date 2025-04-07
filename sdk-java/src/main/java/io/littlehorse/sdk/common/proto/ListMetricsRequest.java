// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * List the latest metrics for a given MetricSpecId
 * </pre>
 *
 * Protobuf type {@code littlehorse.ListMetricsRequest}
 */
public final class ListMetricsRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.ListMetricsRequest)
    ListMetricsRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ListMetricsRequest.newBuilder() to construct.
  private ListMetricsRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ListMetricsRequest() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ListMetricsRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListMetricsRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListMetricsRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.ListMetricsRequest.class, io.littlehorse.sdk.common.proto.ListMetricsRequest.Builder.class);
  }

  public static final int METRIC_SPEC_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.MetricSpecId metricSpecId_;
  /**
   * <pre>
   * Filters by metric id
   * </pre>
   *
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   * @return Whether the metricSpecId field is set.
   */
  @java.lang.Override
  public boolean hasMetricSpecId() {
    return metricSpecId_ != null;
  }
  /**
   * <pre>
   * Filters by metric id
   * </pre>
   *
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   * @return The metricSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricSpecId getMetricSpecId() {
    return metricSpecId_ == null ? io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
  }
  /**
   * <pre>
   * Filters by metric id
   * </pre>
   *
   * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.MetricSpecIdOrBuilder getMetricSpecIdOrBuilder() {
    return metricSpecId_ == null ? io.littlehorse.sdk.common.proto.MetricSpecId.getDefaultInstance() : metricSpecId_;
  }

  public static final int WINDOW_LENGTH_FIELD_NUMBER = 2;
  private com.google.protobuf.Duration windowLength_;
  /**
   * <pre>
   * Filters by window length
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 2;</code>
   * @return Whether the windowLength field is set.
   */
  @java.lang.Override
  public boolean hasWindowLength() {
    return windowLength_ != null;
  }
  /**
   * <pre>
   * Filters by window length
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 2;</code>
   * @return The windowLength.
   */
  @java.lang.Override
  public com.google.protobuf.Duration getWindowLength() {
    return windowLength_ == null ? com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
  }
  /**
   * <pre>
   * Filters by window length
   * </pre>
   *
   * <code>.google.protobuf.Duration window_length = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.DurationOrBuilder getWindowLengthOrBuilder() {
    return windowLength_ == null ? com.google.protobuf.Duration.getDefaultInstance() : windowLength_;
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
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.littlehorse.sdk.common.proto.ListMetricsRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.ListMetricsRequest other = (io.littlehorse.sdk.common.proto.ListMetricsRequest) obj;

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
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.ListMetricsRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.ListMetricsRequest prototype) {
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
   * List the latest metrics for a given MetricSpecId
   * </pre>
   *
   * Protobuf type {@code littlehorse.ListMetricsRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.ListMetricsRequest)
      io.littlehorse.sdk.common.proto.ListMetricsRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListMetricsRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListMetricsRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.ListMetricsRequest.class, io.littlehorse.sdk.common.proto.ListMetricsRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.ListMetricsRequest.newBuilder()
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
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_ListMetricsRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListMetricsRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.ListMetricsRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListMetricsRequest build() {
      io.littlehorse.sdk.common.proto.ListMetricsRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.ListMetricsRequest buildPartial() {
      io.littlehorse.sdk.common.proto.ListMetricsRequest result = new io.littlehorse.sdk.common.proto.ListMetricsRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.ListMetricsRequest result) {
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
      if (other instanceof io.littlehorse.sdk.common.proto.ListMetricsRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.ListMetricsRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.ListMetricsRequest other) {
      if (other == io.littlehorse.sdk.common.proto.ListMetricsRequest.getDefaultInstance()) return this;
      if (other.hasMetricSpecId()) {
        mergeMetricSpecId(other.getMetricSpecId());
      }
      if (other.hasWindowLength()) {
        mergeWindowLength(other.getWindowLength());
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     * @return Whether the metricSpecId field is set.
     */
    public boolean hasMetricSpecId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
     * <code>.littlehorse.MetricSpecId metric_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.MetricSpecId.Builder getMetricSpecIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getMetricSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by metric id
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
     * <code>.google.protobuf.Duration window_length = 2;</code>
     * @return Whether the windowLength field is set.
     */
    public boolean hasWindowLength() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
     * <code>.google.protobuf.Duration window_length = 2;</code>
     */
    public com.google.protobuf.Duration.Builder getWindowLengthBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getWindowLengthFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Filters by window length
     * </pre>
     *
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
     * <pre>
     * Filters by window length
     * </pre>
     *
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


    // @@protoc_insertion_point(builder_scope:littlehorse.ListMetricsRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.ListMetricsRequest)
  private static final io.littlehorse.sdk.common.proto.ListMetricsRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.ListMetricsRequest();
  }

  public static io.littlehorse.sdk.common.proto.ListMetricsRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListMetricsRequest>
      PARSER = new com.google.protobuf.AbstractParser<ListMetricsRequest>() {
    @java.lang.Override
    public ListMetricsRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<ListMetricsRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListMetricsRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.ListMetricsRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


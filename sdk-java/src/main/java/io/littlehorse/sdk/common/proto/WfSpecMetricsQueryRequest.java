// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package io.littlehorse.sdk.common.proto;

/**
 * <pre>
 * Query to retrieve a specific WfSpec Metrics Window.
 * </pre>
 *
 * Protobuf type {@code littlehorse.WfSpecMetricsQueryRequest}
 */
public final class WfSpecMetricsQueryRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:littlehorse.WfSpecMetricsQueryRequest)
    WfSpecMetricsQueryRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use WfSpecMetricsQueryRequest.newBuilder() to construct.
  private WfSpecMetricsQueryRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private WfSpecMetricsQueryRequest() {
    windowLength_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new WfSpecMetricsQueryRequest();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.class, io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.Builder.class);
  }

  public static final int WF_SPEC_ID_FIELD_NUMBER = 1;
  private io.littlehorse.sdk.common.proto.WfSpecId wfSpecId_;
  /**
   * <pre>
   * WfSpecId of metrics to get.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return Whether the wfSpecId field is set.
   */
  @java.lang.Override
  public boolean hasWfSpecId() {
    return wfSpecId_ != null;
  }
  /**
   * <pre>
   * WfSpecId of metrics to get.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   * @return The wfSpecId.
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecId getWfSpecId() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }
  /**
   * <pre>
   * WfSpecId of metrics to get.
   * </pre>
   *
   * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
   */
  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder getWfSpecIdOrBuilder() {
    return wfSpecId_ == null ? io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance() : wfSpecId_;
  }

  public static final int WINDOW_START_FIELD_NUMBER = 2;
  private com.google.protobuf.Timestamp windowStart_;
  /**
   * <pre>
   * Return the window *containing* this timestamp. The window start is not guaranteed to
   * align perfectly with the request.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 2;</code>
   * @return Whether the windowStart field is set.
   */
  @java.lang.Override
  public boolean hasWindowStart() {
    return windowStart_ != null;
  }
  /**
   * <pre>
   * Return the window *containing* this timestamp. The window start is not guaranteed to
   * align perfectly with the request.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 2;</code>
   * @return The windowStart.
   */
  @java.lang.Override
  public com.google.protobuf.Timestamp getWindowStart() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }
  /**
   * <pre>
   * Return the window *containing* this timestamp. The window start is not guaranteed to
   * align perfectly with the request.
   * </pre>
   *
   * <code>.google.protobuf.Timestamp window_start = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.TimestampOrBuilder getWindowStartOrBuilder() {
    return windowStart_ == null ? com.google.protobuf.Timestamp.getDefaultInstance() : windowStart_;
  }

  public static final int WINDOW_LENGTH_FIELD_NUMBER = 3;
  private int windowLength_ = 0;
  /**
   * <pre>
   * The window size
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
   * @return The enum numeric value on the wire for windowLength.
   */
  @java.lang.Override public int getWindowLengthValue() {
    return windowLength_;
  }
  /**
   * <pre>
   * The window size
   * </pre>
   *
   * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
   * @return The windowLength.
   */
  @java.lang.Override public io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowLength() {
    io.littlehorse.sdk.common.proto.MetricsWindowLength result = io.littlehorse.sdk.common.proto.MetricsWindowLength.forNumber(windowLength_);
    return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLength.UNRECOGNIZED : result;
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
    if (wfSpecId_ != null) {
      output.writeMessage(1, getWfSpecId());
    }
    if (windowStart_ != null) {
      output.writeMessage(2, getWindowStart());
    }
    if (windowLength_ != io.littlehorse.sdk.common.proto.MetricsWindowLength.MINUTES_5.getNumber()) {
      output.writeEnum(3, windowLength_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (wfSpecId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getWfSpecId());
    }
    if (windowStart_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getWindowStart());
    }
    if (windowLength_ != io.littlehorse.sdk.common.proto.MetricsWindowLength.MINUTES_5.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(3, windowLength_);
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
    if (!(obj instanceof io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest)) {
      return super.equals(obj);
    }
    io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest other = (io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest) obj;

    if (hasWfSpecId() != other.hasWfSpecId()) return false;
    if (hasWfSpecId()) {
      if (!getWfSpecId()
          .equals(other.getWfSpecId())) return false;
    }
    if (hasWindowStart() != other.hasWindowStart()) return false;
    if (hasWindowStart()) {
      if (!getWindowStart()
          .equals(other.getWindowStart())) return false;
    }
    if (windowLength_ != other.windowLength_) return false;
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
    if (hasWindowStart()) {
      hash = (37 * hash) + WINDOW_START_FIELD_NUMBER;
      hash = (53 * hash) + getWindowStart().hashCode();
    }
    hash = (37 * hash) + WINDOW_LENGTH_FIELD_NUMBER;
    hash = (53 * hash) + windowLength_;
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest parseFrom(
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
  public static Builder newBuilder(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest prototype) {
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
   * Query to retrieve a specific WfSpec Metrics Window.
   * </pre>
   *
   * Protobuf type {@code littlehorse.WfSpecMetricsQueryRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:littlehorse.WfSpecMetricsQueryRequest)
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.class, io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.Builder.class);
    }

    // Construct using io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.newBuilder()
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
      wfSpecId_ = null;
      if (wfSpecIdBuilder_ != null) {
        wfSpecIdBuilder_.dispose();
        wfSpecIdBuilder_ = null;
      }
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      windowLength_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.littlehorse.sdk.common.proto.Service.internal_static_littlehorse_WfSpecMetricsQueryRequest_descriptor;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest getDefaultInstanceForType() {
      return io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.getDefaultInstance();
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest build() {
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest buildPartial() {
      io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest result = new io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.wfSpecId_ = wfSpecIdBuilder_ == null
            ? wfSpecId_
            : wfSpecIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.windowStart_ = windowStartBuilder_ == null
            ? windowStart_
            : windowStartBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.windowLength_ = windowLength_;
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
      if (other instanceof io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest) {
        return mergeFrom((io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest other) {
      if (other == io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.getDefaultInstance()) return this;
      if (other.hasWfSpecId()) {
        mergeWfSpecId(other.getWfSpecId());
      }
      if (other.hasWindowStart()) {
        mergeWindowStart(other.getWindowStart());
      }
      if (other.windowLength_ != 0) {
        setWindowLengthValue(other.getWindowLengthValue());
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
                  getWfSpecIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getWindowStartFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              windowLength_ = input.readEnum();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
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
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> wfSpecIdBuilder_;
    /**
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     * @return Whether the wfSpecId field is set.
     */
    public boolean hasWfSpecId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    public io.littlehorse.sdk.common.proto.WfSpecId.Builder getWfSpecIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getWfSpecIdFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
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
     * <pre>
     * WfSpecId of metrics to get.
     * </pre>
     *
     * <code>.littlehorse.WfSpecId wf_spec_id = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder> 
        getWfSpecIdFieldBuilder() {
      if (wfSpecIdBuilder_ == null) {
        wfSpecIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpecId.Builder, io.littlehorse.sdk.common.proto.WfSpecIdOrBuilder>(
                getWfSpecId(),
                getParentForChildren(),
                isClean());
        wfSpecId_ = null;
      }
      return wfSpecIdBuilder_;
    }

    private com.google.protobuf.Timestamp windowStart_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Timestamp, com.google.protobuf.Timestamp.Builder, com.google.protobuf.TimestampOrBuilder> windowStartBuilder_;
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
     * @return Whether the windowStart field is set.
     */
    public boolean hasWindowStart() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
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
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
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
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
     */
    public Builder setWindowStart(
        com.google.protobuf.Timestamp.Builder builderForValue) {
      if (windowStartBuilder_ == null) {
        windowStart_ = builderForValue.build();
      } else {
        windowStartBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
     */
    public Builder mergeWindowStart(com.google.protobuf.Timestamp value) {
      if (windowStartBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          windowStart_ != null &&
          windowStart_ != com.google.protobuf.Timestamp.getDefaultInstance()) {
          getWindowStartBuilder().mergeFrom(value);
        } else {
          windowStart_ = value;
        }
      } else {
        windowStartBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
     */
    public Builder clearWindowStart() {
      bitField0_ = (bitField0_ & ~0x00000002);
      windowStart_ = null;
      if (windowStartBuilder_ != null) {
        windowStartBuilder_.dispose();
        windowStartBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
     */
    public com.google.protobuf.Timestamp.Builder getWindowStartBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getWindowStartFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
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
     * <pre>
     * Return the window *containing* this timestamp. The window start is not guaranteed to
     * align perfectly with the request.
     * </pre>
     *
     * <code>.google.protobuf.Timestamp window_start = 2;</code>
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

    private int windowLength_ = 0;
    /**
     * <pre>
     * The window size
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
     * @return The enum numeric value on the wire for windowLength.
     */
    @java.lang.Override public int getWindowLengthValue() {
      return windowLength_;
    }
    /**
     * <pre>
     * The window size
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
     * @param value The enum numeric value on the wire for windowLength to set.
     * @return This builder for chaining.
     */
    public Builder setWindowLengthValue(int value) {
      windowLength_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The window size
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
     * @return The windowLength.
     */
    @java.lang.Override
    public io.littlehorse.sdk.common.proto.MetricsWindowLength getWindowLength() {
      io.littlehorse.sdk.common.proto.MetricsWindowLength result = io.littlehorse.sdk.common.proto.MetricsWindowLength.forNumber(windowLength_);
      return result == null ? io.littlehorse.sdk.common.proto.MetricsWindowLength.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * The window size
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
     * @param value The windowLength to set.
     * @return This builder for chaining.
     */
    public Builder setWindowLength(io.littlehorse.sdk.common.proto.MetricsWindowLength value) {
      if (value == null) {
        throw new NullPointerException();
      }
      bitField0_ |= 0x00000004;
      windowLength_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The window size
     * </pre>
     *
     * <code>.littlehorse.MetricsWindowLength window_length = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearWindowLength() {
      bitField0_ = (bitField0_ & ~0x00000004);
      windowLength_ = 0;
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


    // @@protoc_insertion_point(builder_scope:littlehorse.WfSpecMetricsQueryRequest)
  }

  // @@protoc_insertion_point(class_scope:littlehorse.WfSpecMetricsQueryRequest)
  private static final io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest();
  }

  public static io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<WfSpecMetricsQueryRequest>
      PARSER = new com.google.protobuf.AbstractParser<WfSpecMetricsQueryRequest>() {
    @java.lang.Override
    public WfSpecMetricsQueryRequest parsePartialFrom(
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

  public static com.google.protobuf.Parser<WfSpecMetricsQueryRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<WfSpecMetricsQueryRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

